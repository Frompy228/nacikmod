package net.artur.nacikmod.block.entity;

import net.artur.nacikmod.capability.root.RootProvider;
import net.artur.nacikmod.registry.ModBlocks;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * BlockEntity для блока барьера.
 * Отвечает за:
 * - хранение владельца;
 * - активность барьера от BarrierSeal (оповещение о входе/выходе сущностей);
 * - активность барьера от BarrierWall (эффекты замедления и EffectBaseDomain).
 */
public class BarrierBlockEntity extends BlockEntity {

    private static final double RADIUS = 20.0;
    private static final double RADIUS_SQ = RADIUS * RADIUS;

    // UUID владельца барьера (совпадает с OWNER_TAG в предметах)
    private UUID owner;

    // Активность барьера от BarrierSeal (слоты предмета)
    private boolean sealActive;
    private int sealSlot = -1;

    // Активность барьера от BarrierWall
    private boolean wallActive;
    private int wallSlot = -1;

    // Кэш сущностей, которые сейчас внутри радиуса
    private final Set<UUID> insideEntities = new HashSet<>();

    // Локальный счетчик тиков (чтобы тяжелую логику выполнять ~раз в секунду)
    private int tickCounter = 0;

    public BarrierBlockEntity(BlockPos pos, BlockState state) {
        super(net.artur.nacikmod.registry.ModBlockEntities.BARRIER_BLOCK_ENTITY.get(), pos, state);
    }

    // -------- Публичное API для предметов --------

    public void updateSealState(UUID ownerUuid, boolean active, int slot) {
        this.owner = ownerUuid;
        this.sealActive = active;
        this.sealSlot = slot;
        setChanged();
    }

    public void updateWallState(UUID ownerUuid, boolean active, int slot) {
        this.owner = ownerUuid;
        this.wallActive = active;
        this.wallSlot = slot;
        setChanged();
    }

    public UUID getOwner() {
        return owner;
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level) {
        if (level.isClientSide) return null;
        return (lvl, pos, state, be) -> {
            if (be instanceof BarrierBlockEntity barrierBe) {
                barrierBe.serverTick(lvl, pos, state);
            }
        };
    }

    private void serverTick(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!state.is(ModBlocks.BARRIER.get())) return;
        if (owner == null) return;

        // Если ни один тип барьера не активен – ничего не делаем
        if (!sealActive && !wallActive) {
            insideEntities.clear();
            return;
        }

        tickCounter++;
        // Тяжелую логику выполняем раз в 20 тиков (~раз в секунду)
        if (tickCounter % 20 != 0) return;

        AABB box = getSearchBox();
        // Ищем всех живых существ (игроки + мобы)
        java.util.List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, box, Entity::isAlive);

        Set<UUID> current = new HashSet<>();
        Map<UUID, LivingEntity> entityMap = new HashMap<>();

        for (LivingEntity e : entities) {
            if (!isInRange(e)) continue;
            UUID id = e.getUUID();
            current.add(id);
            entityMap.put(id, e);
        }

        // Вычисляем вошедших и вышедших
        Set<UUID> entered = new HashSet<>(current);
        entered.removeAll(insideEntities);

        Set<UUID> exited = new HashSet<>(insideEntities);
        exited.removeAll(current);

        // Оповещение владельца о вошедших/вышедших – только если активен BarrierSeal
        if (sealActive && sealSlot >= 0) {
            for (UUID id : entered) {
                LivingEntity e = entityMap.get(id);
                if (e != null) notifyOwner(serverLevel, e, true, sealSlot);
            }
            for (UUID id : exited) {
                LivingEntity e = entityMap.get(id);
                if (e != null) notifyOwner(serverLevel, e, false, sealSlot);
            }
        }

        insideEntities.clear();
        insideEntities.addAll(current);

        // Эффекты от BarrierWall
        if (wallActive) {
            applyWallEffects(serverLevel, entityMap.values());
        }
    }

    // -------- Эффекты BarrierWall --------

    private void applyWallEffects(ServerLevel level, Iterable<LivingEntity> entities) {
        for (LivingEntity entity : entities) {
            // Пропускаем владельца
            if (owner != null && owner.equals(entity.getUUID())) continue;

            // Эффект медлительности
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false, true));

            // Эффект EffectBaseDomain (анти‑телепорт и "заякоривание" к центру барьера)
            double distanceSq = entity.blockPosition().distSqr(worldPosition);
            if (distanceSq <= RADIUS_SQ) {
                boolean hadEffect = entity.hasEffect(ModEffects.EFFECT_BASE_DOMAIN.get());

                entity.addEffect(new MobEffectInstance(
                        ModEffects.EFFECT_BASE_DOMAIN.get(),
                        40, // 2 секунды, будет продлеваться
                        0,
                        true,
                        true,
                        true
                ));

                entity.getCapability(RootProvider.ROOT_CAPABILITY).ifPresent(data -> {
                    if (hadEffect) {
                        data.setPendingData(worldPosition, level.dimension());
                        data.commitData();
                    } else {
                        data.setPendingData(worldPosition, level.dimension());
                        data.forceCommitData();
                    }
                });
            }
        }
    }

    // -------- Уведомления владельцу --------

    private void notifyOwner(ServerLevel level, LivingEntity entity, boolean entered, int slot) {
        if (owner == null) return;
        ServerPlayer ownerPlayer = level.getServer().getPlayerList().getPlayer(owner);
        if (ownerPlayer == null) return;

        String name = entity.getName().getString();
        String msg = String.format("%s %s barrier in slot %d at (%d, %d, %d)",
                name,
                entered ? "entered" : "exited",
                slot + 1,
                (int) entity.getX(),
                (int) entity.getY(),
                (int) entity.getZ());

        ownerPlayer.sendSystemMessage(Component.literal(msg)
                .withStyle(entered ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
    }

    // -------- Сериализация --------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
        tag.putBoolean("SealActive", sealActive);
        tag.putInt("SealSlot", sealSlot);
        tag.putBoolean("WallActive", wallActive);
        tag.putInt("WallSlot", wallSlot);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("Owner")) {
            owner = tag.getUUID("Owner");
        } else {
            owner = null;
        }
        sealActive = tag.getBoolean("SealActive");
        sealSlot = tag.getInt("SealSlot");
        wallActive = tag.getBoolean("WallActive");
        wallSlot = tag.getInt("WallSlot");
    }

    // -------- Вспомогательные методы --------

    private boolean isInRange(Entity e) {
        double dx = e.getX() - (worldPosition.getX() + 0.5);
        double dy = e.getY() - (worldPosition.getY() + 0.5);
        double dz = e.getZ() - (worldPosition.getZ() + 0.5);
        return dx * dx + dy * dy + dz * dz <= RADIUS_SQ;
    }

    private AABB getSearchBox() {
        return new AABB(
                worldPosition.getX() - RADIUS,
                worldPosition.getY() - RADIUS,
                worldPosition.getZ() - RADIUS,
                worldPosition.getX() + RADIUS,
                worldPosition.getY() + RADIUS,
                worldPosition.getZ() + RADIUS
        );
    }
}
