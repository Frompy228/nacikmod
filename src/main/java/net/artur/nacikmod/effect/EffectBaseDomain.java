package net.artur.nacikmod.effect;

import net.artur.nacikmod.capability.root.IRootData;
import net.artur.nacikmod.capability.root.RootProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ITeleporter;

import java.util.UUID;
import java.util.function.Function;

/**
 * Эффект Base Domain - предотвращает телепортацию из области барьера.
 * Если сущность пытается выйти за границу барьера, её телепортирует обратно.
 */
public class EffectBaseDomain extends MobEffect {
    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("8f1c7e23-2c8b-5a4b-b0e4-4a1f0b8b6c2a");
    private static final double BARRIER_RADIUS = 20.0;
    private static final double BARRIER_RADIUS_SQ = BARRIER_RADIUS * BARRIER_RADIUS;

    public EffectBaseDomain() {
        super(MobEffectCategory.HARMFUL, 0x4B0082); // Темно-фиолетовый цвет
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_ID.toString(),
                -0.05D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;

        entity.getCapability(RootProvider.ROOT_CAPABILITY).ifPresent(data -> {
            // Проверяем и обновляем координаты при каждом тике
            BlockPos currentPos = entity.blockPosition();
            ResourceKey<Level> currentDim = entity.level().dimension();

            // Если координаты не установлены, устанавливаем их (позиция внутри барьера)
            if (data.getCommittedPosition() == null || data.getCommittedDimension() == null) {
                data.setPendingData(currentPos, currentDim);
                data.forceCommitData();
                return;
            }

            BlockPos barrierCenter = data.getCommittedPosition();
            ResourceKey<Level> barrierDim = data.getCommittedDimension();

            // Проверка измерения
            if (!currentDim.equals(barrierDim)) {
                // Сущность в другом измерении - телепортируем обратно
                teleportToBarrierDimension(entity, barrierDim, barrierCenter);
                return;
            }

            // Проверяем, находится ли сущность внутри барьера (радиус 20 блоков)
            double distanceSq = currentPos.distSqr(barrierCenter);
            if (distanceSq > BARRIER_RADIUS_SQ) {
                // Сущность вышла за границу барьера - телепортируем обратно
                teleportBackToBarrier(entity, barrierCenter);
            } else {
                // Сущность внутри барьера - обновляем committed позицию для отслеживания
                // Это позволяет отслеживать движение внутри барьера
                data.setPendingData(currentPos, currentDim);
                data.commitData();
            }
        });
    }

    /**
     * Телепортирует сущность обратно в центр барьера, если она вышла за границу
     */
    private void teleportBackToBarrier(LivingEntity entity, BlockPos barrierCenter) {
        // Находим ближайшую точку внутри барьера от текущей позиции
        BlockPos currentPos = entity.blockPosition();
        double dx = currentPos.getX() - barrierCenter.getX();
        double dy = currentPos.getY() - barrierCenter.getY();
        double dz = currentPos.getZ() - barrierCenter.getZ();
        
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance > BARRIER_RADIUS) {
            // Нормализуем вектор и умножаем на радиус
            double scale = (BARRIER_RADIUS - 0.5) / distance; // -0.5 чтобы быть точно внутри
            double newX = barrierCenter.getX() + dx * scale;
            double newY = barrierCenter.getY() + dy * scale;
            double newZ = barrierCenter.getZ() + dz * scale;
            
            entity.teleportTo(newX, newY, newZ);
        }
    }

    /**
     * Телепортирует сущность обратно в измерение барьера
     */
    private void teleportToBarrierDimension(LivingEntity entity,
                                             ResourceKey<Level> dimension,
                                             BlockPos pos) {
        if (!(entity.level() instanceof ServerLevel currentLevel)) return;

        ServerLevel targetLevel = currentLevel.getServer().getLevel(dimension);
        if (targetLevel == null) return;

        if (entity instanceof ServerPlayer player) {
            teleportPlayer(player, targetLevel, pos);
        } else {
            teleportEntity(entity, targetLevel, pos);
        }
    }

    private void teleportPlayer(ServerPlayer player, ServerLevel targetLevel, BlockPos pos) {
        player.teleportTo(
                targetLevel,
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5,
                player.getYRot(),
                player.getXRot()
        );
    }

    private void teleportEntity(LivingEntity entity, ServerLevel targetLevel, BlockPos pos) {
        entity.changeDimension(targetLevel, new ITeleporter() {
            @Override
            public Entity placeEntity(Entity entity, ServerLevel currentLevel, ServerLevel destLevel, float yaw, Function<Boolean, Entity> repositionEntity) {
                Entity newEntity = repositionEntity.apply(false);
                if (newEntity != null) {
                    newEntity.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    newEntity.setYRot(yaw);
                    newEntity.setXRot(entity.getXRot());
                }
                return newEntity;
            }
        });
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Проверяем каждый тик
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        if (!entity.level().isClientSide) {
            // Очистка позиции в Capability при удалении эффекта
            entity.getCapability(RootProvider.ROOT_CAPABILITY).ifPresent(IRootData::clear);
        }
    }
}


