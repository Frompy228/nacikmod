package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.artur.nacikmod.entity.projectiles.SlashProjectile;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModEntities;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Slash extends Item {
    private static final int MANA_COST = 500;
    private static final int COOLDOWN_TICKS = 100; // 5 секунд (20 тиков * 5)

    public Slash(Properties properties) {
        super(properties.stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Check if player has enough mana
            if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                player.sendSystemMessage(Component.literal("Not enough mana!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Use mana
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));

            // Направление взгляда игрока
            var look = player.getLookAngle();
            // Создаём снаряд
            SlashProjectile projectile = new SlashProjectile(level, player);
            projectile.shoot(look.x, look.y, look.z, 2.7F, 0.0F);
            projectile.setPos(
                    player.getX() + look.x,
                    player.getEyeY() + look.y,
                    player.getZ() + look.z
            );
            level.addFreshEntity(projectile);

            // Set cooldown
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.slash.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.slash.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
