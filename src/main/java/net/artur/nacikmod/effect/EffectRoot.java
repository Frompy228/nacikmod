package net.artur.nacikmod.effect;

import net.artur.nacikmod.capability.root.IRootData;
import net.artur.nacikmod.capability.root.RootProvider;
import net.artur.nacikmod.network.ModMessages;
import net.artur.nacikmod.network.PacketSyncEffect;
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
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class EffectRoot extends MobEffect {
    private static final int MAX_DISTANCE = 5;
    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("7f0b6d12-1b7a-4f3f-a9d3-3f0e9a7a5b1f");

    public EffectRoot() {
        super(MobEffectCategory.HARMFUL, 0x964B00);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_ID.toString(),
                -0.5D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public boolean isBeneficial() {
        return false;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;

        entity.getCapability(RootProvider.ROOT_CAPABILITY).ifPresent(data -> {
            // Проверка и фиксация данных
            if (!data.isInitialized() && data.getPendingPosition() != null) {
                data.commitData();
            }

            // Используем только зафиксированные данные
            BlockPos targetPos = data.getCommittedPosition();
            ResourceKey<Level> targetDim = data.getCommittedDimension();

            if (targetPos == null || targetDim == null) return;

            // Проверка измерения
            if (!entity.level().dimension().equals(targetDim)) {
                teleportToHomeDimension(entity, targetDim, targetPos);
                return;
            }

            // Проверка расстояния
            if (entity.blockPosition().distSqr(targetPos) > MAX_DISTANCE * MAX_DISTANCE) {
                teleportWithinDimension(entity, targetPos);
            }
        });
    }

    private void teleportWithinDimension(LivingEntity entity, BlockPos targetPos) {
        entity.teleportTo(
                targetPos.getX() + 0.5,
                targetPos.getY(),
                targetPos.getZ() + 0.5
        );
    }

    private void teleportToHomeDimension(LivingEntity entity,
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
        return true;
    }
    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        if (!entity.level().isClientSide) {
            // Очистка позиции в Capability
            entity.getCapability(RootProvider.ROOT_CAPABILITY).ifPresent(IRootData::clear);
        }
        // Когда эффект заканчивается, отправляем пакет всем клиентам, чтобы скрыть модель
        if (!entity.level().isClientSide) {
            ModMessages.INSTANCE.send(PacketDistributor.ALL.noArg(), new PacketSyncEffect(entity.getId(), false));
        }
    }
}