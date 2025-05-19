package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.artur.nacikmod.entity.projectiles.BloodShootProjectile;
import net.artur.nacikmod.capability.mana.ManaProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BloodShoot extends Item {
    private static final int MANA_COST = 75;

    public BloodShoot(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        float healthCost = player.getHealth()/2;

        if (!level.isClientSide) {
            // Check if player has enough mana
            if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                player.sendSystemMessage(Component.literal("Not enough mana!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Check if player has enough health
            if (player.getHealth() <= healthCost) {
                player.sendSystemMessage(Component.literal("Not enough health!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Calculate damage based on current health before using health
            float damage = player.getHealth();

            // Use health and mana
            player.hurt(player.damageSources().generic(), healthCost);
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));

            // Create and launch projectile
            BloodShootProjectile projectile = new BloodShootProjectile(level, player, damage);
            projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 0.0F);
            level.addFreshEntity(projectile);

            // Set cooldown
            player.getCooldowns().addCooldown(this, 15);
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.blood_shoot.desc1"));

        if (level != null && level.isClientSide) {
            for (Player player : level.players()) {
                if (player.isLocalPlayer()) {
                    float damage = player.getHealth();
                    tooltipComponents.add(Component.translatable("item.nacikmod.blood_shoot.desc2", String.format("%.1f", damage))
                            .withStyle(ChatFormatting.DARK_RED));
                    break;
                }
            }
        }

        if (level != null && level.isClientSide) {
            for (Player player : level.players()) {
                if (player.isLocalPlayer()) {
                    float healthCost = player.getHealth()/2;
                    tooltipComponents.add(Component.translatable("item.nacikmod.blood_shoot.desc3", String.format("%.1f", healthCost))
                            .withStyle(ChatFormatting.DARK_RED));
                    break;
                }
            }
        }
        tooltipComponents.add(Component.translatable("item.nacikmod.blood_shoot.desc4", MANA_COST)
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

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Предмет всегда светится
    }
}
