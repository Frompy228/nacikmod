package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.artur.nacikmod.entity.projectiles.IceSpikeProjectile;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.capability.mana.ManaProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IcePrison extends Item {
    private static final int COOLDOWN = 125;
    private static final int MANA_COST = 250;

    public IcePrison(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            // Проверяем наличие маны
            if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                player.sendSystemMessage(Component.literal("Not enough mana!").withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }
            // Тратим ману
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));
            IceSpikeProjectile projectile = new IceSpikeProjectile(level, player, 0);
            projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2F, 0.0F);
            level.addFreshEntity(projectile);
            player.getCooldowns().addCooldown(this, COOLDOWN);
        }
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("item.nacikmod.ice_prison.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.ice_prison.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));
    }
}
