package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.network.ModMessages;
import net.artur.nacikmod.registry.ModEffects;
import net.artur.nacikmod.util.PlayerCooldowns;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ManaSeal extends Item {
    private static final int MANA_COST = 25;
    private static final int EFFECT_DURATION = 100; // 5 секунд (100 тиков)
    private static final int COOLDOWN_TICKS = 40; // 2 секунды для тестов

    public ManaSeal(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide && attacker instanceof Player player) {
            // Проверяем кулдаун
            if (PlayerCooldowns.isOnCooldown(player, this)) {
                int left = PlayerCooldowns.getCooldownLeft(player, this);
                player.sendSystemMessage(Component.literal("Item is on cooldown! (" + left / 20 + "s left)")
                        .withStyle(ChatFormatting.RED));
                return super.hurtEnemy(stack, target, attacker);
            }

            // Проверяем ману
            if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                player.sendSystemMessage(Component.literal("You Do Not Have Enough Mana")
                        .withStyle(ChatFormatting.RED));
                return super.hurtEnemy(stack, target, attacker);
            }

            // Накладываем эффект на цель
            target.addEffect(new MobEffectInstance(
                ModEffects.MANA_SEAL.get(),
                EFFECT_DURATION,
                0,
                false,
                true,
                true
            ));

            // Тратим ману и синхронизируем с клиентом
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                    mana.removeMana(MANA_COST);
                    ModMessages.sendManaToClient(serverPlayer, mana.getMana(), mana.getMaxMana());
                });
            }

            // Устанавливаем кулдаун
            PlayerCooldowns.setCooldown(player, this, COOLDOWN_TICKS);

        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

    }
}
