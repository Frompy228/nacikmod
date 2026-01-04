package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.IMana;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.item.ability.BurialAbility;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Burial extends Item {
    private static final int MANA_COST = 750;
    private static final int COOLDOWN_TICKS = 40; // 2 секунды = 2 * 20 тиков
    
    public Burial() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Проверка кулдауна
            if (player.getCooldowns().isOnCooldown(this)) {
                float cooldownPercent = player.getCooldowns().getCooldownPercent(this, 0);
                int cooldownSeconds = (int) (cooldownPercent * 2); // 2 секунды = 100%
                player.sendSystemMessage(Component.literal("Burial is on cooldown! (" + cooldownSeconds + "s left)")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Проверяем ману
            LazyOptional<IMana> manaCap = player.getCapability(ManaProvider.MANA_CAPABILITY);
            if (!manaCap.isPresent()) {
                player.sendSystemMessage(Component.literal("Mana capability not found!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            IMana mana = manaCap.orElseThrow(IllegalStateException::new);
            if (mana.getMana() < MANA_COST) {
                player.sendSystemMessage(Component.literal("Not enough mana! Need " + MANA_COST)
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Активация способности
            if (BurialAbility.activateAbility(player, level)) {
                // Тратим ману
                mana.removeMana(MANA_COST);
                
                // Установка кулдауна (2 секунды)
                player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
                player.sendSystemMessage(Component.literal("Burial activated!")
                        .withStyle(ChatFormatting.GREEN));
            } else {
                player.sendSystemMessage(Component.literal("No valid target found!")
                        .withStyle(ChatFormatting.RED));
            }
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("item.nacikmod.burial.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.burial.desc2", MANA_COST)
                .withStyle(style -> style.withColor(0x00FFFF)));
    }
}

