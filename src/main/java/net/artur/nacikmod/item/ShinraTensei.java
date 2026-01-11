package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.item.ability.ShinraTenseiExplosion;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.util.PlayerCooldowns;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.List;
import java.util.Optional;

public class ShinraTensei extends Item {
    private static final int MANA_COST = 15000;
    private static final int EXPLOSION_RADIUS = 300;
    private static final int COOLDOWN_TICKS = 10000;

    public ShinraTensei(Properties properties) {
        super(properties.fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (PlayerCooldowns.isOnCooldown(player, this)) {
            int left = PlayerCooldowns.getCooldownLeft(player, this);
            player.sendSystemMessage(Component.literal("Item is on cooldown! (" + left / 20 + "s left)")
                    .withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(itemStack);
        }

        if (!level.isClientSide) {
            // Проверяем наличие Dark Sphere в слотах Curios используя новый API
            boolean hasDarkSphere = CuriosApi.getCuriosInventory(player)
                    .map(handler -> {
                        for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                            for (int i = 0; i < stacksHandler.getSlots(); i++) {
                                ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                                if (stack.getItem() == ModItems.DARK_SPHERE.get()) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    })
                    .orElse(false);

            if (!hasDarkSphere) {
                player.sendSystemMessage(Component.literal("You need Dark Sphere to use this ability!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                player.sendSystemMessage(Component.literal("Not enough mana!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Create explosion at target location
            LivingEntity targetEntity = getTargetedEntity(player, 150.0D);
            Vec3 targetPos;

            if (targetEntity != null) {
                // Цель в поле зрения → используем её координаты
                targetPos = new Vec3(targetEntity.getX(), targetEntity.getY() + targetEntity.getBbHeight() / 2.0, targetEntity.getZ());
            } else {
                // Нет сущности → блок или точка перед глазами
                HitResult hitResult = player.pick(150.0D, 0.0F, false);
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    targetPos = ((BlockHitResult) hitResult).getLocation();
                } else {
                    targetPos = player.getLookAngle().scale(100).add(player.getEyePosition());
                }
            }


            // Используем наш кастомный взрыв
            ShinraTenseiExplosion explosion = new ShinraTenseiExplosion(
                level, player, targetPos.x, targetPos.y, targetPos.z, EXPLOSION_RADIUS
            );
            explosion.explode();

            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));
            PlayerCooldowns.setCooldown(player, this, COOLDOWN_TICKS);
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Nullable
    private static LivingEntity getTargetedEntity(Player player, double range) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle().scale(range);
        Vec3 endPos = eyePos.add(lookVec);
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec).inflate(1.0D);
        LivingEntity bestTarget = null;
        double closestDist = range;

        for (Entity entity : player.level().getEntities(player, searchBox, e -> e instanceof LivingEntity && e.isAlive() && e != player)) {
            AABB targetBox = entity.getBoundingBox().inflate(0.3);
            Optional<Vec3> hit = targetBox.clip(eyePos, endPos);
            if (hit.isPresent()) {
                double dist = eyePos.distanceTo(hit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    bestTarget = (LivingEntity) entity;
                }
            }
        }
        return bestTarget;
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.shinra_tensei.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.shinra_tensei.desc2")
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
