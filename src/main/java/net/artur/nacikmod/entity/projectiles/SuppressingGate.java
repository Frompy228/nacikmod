package net.artur.nacikmod.entity.projectiles;

import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

public class SuppressingGate extends Entity {
    private static final int MAX_LIFETIME = 220;
    private int lifetime = 0;
    private UUID ownerUUID;

    public SuppressingGate(EntityType<?> type, Level level) {
        super(type, level);
    }

    // Упрощенный конструктор для вызова из предмета
    public SuppressingGate(Level level, double x, double y, double z, float yaw, UUID ownerUUID) {
        this(net.artur.nacikmod.registry.ModEntities.SUPPRESSING_GATE.get(), level);
        this.setPos(x, y, z);
        this.setYRot(yaw);
        this.ownerUUID = ownerUUID;
    }

    @Override
    public void tick() {
        super.tick();

        // 1. Физика падения
        if (!this.isNoGravity()) {
            // Если мы не на земле, ускоряемся вниз (0.08 - стандартная гравитация)
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.08, 0));
        }

        // Двигаем сущность с учетом столкновений с блоками
        this.move(MoverType.SELF, this.getDeltaMovement());

        // Трение (чтобы печать не улетала бесконечно по инерции)
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));

        if (!this.level().isClientSide) {
            // 2. Время жизни
            if (++lifetime >= MAX_LIFETIME) {
                this.discard();
                return;
            }

            // 3. Логика эффекта (на тех, кто внутри коробки)
            applyEffects();
        }
    }

    private void applyEffects() {
        this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox(), entity ->
                ownerUUID == null || !entity.getUUID().equals(ownerUUID)
        ).forEach(target -> {
            target.addEffect(new MobEffectInstance(
                    ModEffects.SUPPRESSING_GATE.get(),
                    40, // 2 секунды, чтобы эффект не моргал
                    0,
                    false, false, true
            ));
        });
    }

    @Override protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.lifetime = nbt.getInt("Lifetime");
        if (nbt.hasUUID("OwnerUUID")) this.ownerUUID = nbt.getUUID("OwnerUUID");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Lifetime", this.lifetime);
        if (this.ownerUUID != null) nbt.putUUID("OwnerUUID", this.ownerUUID);
    }

    @Override public boolean isPushable() { return false; }
    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean fireImmune() { return true; }
}