package net.artur.nacikmod.item;

import net.artur.nacikmod.entity.projectiles.FirePillarEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class FirePillarItem extends Item {
    private static final int RANGE = 150;
    
    public FirePillarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            double targetX, targetY, targetZ;
            
            // Сначала проверяем, есть ли цель (сущность)
            LivingEntity targetEntity = getTargetedEntity(player);
            
            if (targetEntity != null) {
                // Если есть цель - используем позицию сущности
                targetX = targetEntity.getX();
                targetY = targetEntity.getY();
                targetZ = targetEntity.getZ();
            } else {
                // Если цели нет - спавним на блоке, на который смотрим
                HitResult hitResult = player.pick(RANGE, 0.0F, false);
                
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHit = (BlockHitResult) hitResult;
                    Vec3 blockPos = blockHit.getLocation();
                    targetX = blockPos.x;
                    targetY = blockPos.y;
                    targetZ = blockPos.z;
                } else {
                    // Если не попали в блок, используем точку впереди
                    Vec3 targetPos = player.getLookAngle().scale(50).add(player.getEyePosition());
                    targetX = targetPos.x;
                    targetY = targetPos.y;
                    targetZ = targetPos.z;
                }
            }
            
            // Находим уровень земли под целью
            double groundLevel = targetY;
            net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos((int) targetX, (int) targetY, (int) targetZ);
            while (pos.getY() > level.getMinBuildHeight() && level.getBlockState(pos).isAir()) {
                pos = pos.below();
            }
            if (pos.getY() > level.getMinBuildHeight()) {
                groundLevel = pos.getY() + 1.0; // Верхняя поверхность блока
            }
            
            // Спавним столб по центру на уровне земли (getY() будет центром, столб наполовину под землей)
            FirePillarEntity pillar = new FirePillarEntity(level, targetX, groundLevel, targetZ, player);
            level.addFreshEntity(pillar);
        }

        return InteractionResultHolder.success(itemStack);
    }
    
    @Nullable
    private static LivingEntity getTargetedEntity(Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle().scale(RANGE);
        Vec3 endPos = eyePos.add(lookVec);
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec).inflate(1.0D);
        LivingEntity bestTarget = null;
        double closestDist = RANGE;

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

        tooltipComponents.add(Component.translatable("item.nacikmod.fire_pillar.desc1")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.nacikmod.fire_pillar.desc2")
                .withStyle(ChatFormatting.RED));
    }
}

