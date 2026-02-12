package net.artur.nacikmod.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Сущность «цепи» — это Entity, а не эффект (MobEffect).
 *
 * Как у тебя устроены сущности:
 * - Мобы (Knight, Inquisitor…): Model + Renderer + LayerDefinitions, рендерятся как модель.
 * - Проектайлы с моделью (SuppressingGate, FireWall…): Model + Renderer + texture, те же слои.
 * - «Логические» сущности без модели (FIRE_WALL_DAMAGE_ZONE, CHAIN_ENTITY): NoopRenderer —
 *   не рисуют ничего, но есть в мире, тикают, спавнят частицы и т.д.
 *
 * ChainEntity: логическая сущность. Каждый тик обнуляет движение цели и телепортирует её
 * в якорную точку (жёсткая фиксация). Частицы — на сервере. Рендер: NoopRenderer.
 */
public class ChainEntity extends Entity {
    private static final int MAX_LIFETIME = 120; // 6 секунд

    private int life;
    private int targetId = -1;
    private Vec3 anchorPos = Vec3.ZERO;

    public ChainEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public ChainEntity(Level level, EntityType<?> type, LivingEntity target, int lifetimeTicks) {
        this(type, level);
        this.targetId = target.getId();
        this.anchorPos = target.position();
        this.life = Math.max(1, lifetimeTicks);
        this.setPos(anchorPos.x, anchorPos.y, anchorPos.z);
    }
    
    public int getTargetId() {
        return this.targetId;
    }

    @Override
    protected void defineSynchedData() {
        // нет синх-данных, достаточно NBT для сервера
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (--life <= 0) {
            this.discard();
            return;
        }

        Entity e = this.level().getEntity(targetId);
        if (!(e instanceof LivingEntity target) || !target.isAlive()) {
            this.discard();
            return;
        }

        // Жесткая фиксация цели
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
        target.teleportTo(anchorPos.x, anchorPos.y, anchorPos.z);
        target.setYRot(target.getYRot());

        // Частицы цепей вокруг цели
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 8; i++) {
                double px = anchorPos.x + (this.random.nextDouble() - 0.5D) * 2.0D;
                double py = anchorPos.y + this.random.nextDouble() * 1.8D;
                double pz = anchorPos.z + (this.random.nextDouble() - 0.5D) * 2.0D;
                serverLevel.sendParticles(ParticleTypes.CRIT, px, py, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.life = tag.getInt("Life");
        this.targetId = tag.getInt("TargetId");
        if (tag.contains("Ax")) {
            this.anchorPos = new Vec3(tag.getDouble("Ax"), tag.getDouble("Ay"), tag.getDouble("Az"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Life", this.life);
        tag.putInt("TargetId", this.targetId);
        tag.putDouble("Ax", this.anchorPos.x);
        tag.putDouble("Ay", this.anchorPos.y);
        tag.putDouble("Az", this.anchorPos.z);
    }
}

