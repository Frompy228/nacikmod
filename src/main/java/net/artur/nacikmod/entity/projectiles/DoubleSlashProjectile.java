package net.artur.nacikmod.entity.projectiles;

import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DoubleSlashProjectile extends ThrowableItemProjectile {
    private static final float DAMAGE = 60.0F;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 60;

    public DoubleSlashProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.DOUBLE_SLASH_PROJECTILE.get(), shooter, level);
        this.setNoGravity(true);
    }

    public DoubleSlashProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DOUBLE_SLASH.get();
    }

    // Игнорируем базовую обработку столкновений, чтобы не удалять снаряд
    @Override
    protected void onHit(HitResult hitResult) {
        // Не вызываем super.onHit(), чтобы снаряд не исчезал
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        // Этот метод больше не будет вызван, так как мы отключили базовую обработку
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);

        if (!this.level().isClientSide) {
            checkEntityCollisions();

            lifetime++;
            if (lifetime >= MAX_LIFETIME) {
                this.discard();
            }
        }
    }

    // Обработка столкновений с сущностями вручную
    private void checkEntityCollisions() {
        double radius = 1.0D;
        List<LivingEntity> targets = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(radius),
                e -> e != this.getOwner() && e.isAlive() && !e.isInvulnerable()
        );

        for (LivingEntity target : targets) {
            target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), DAMAGE);
            spawnHitParticles(target.position());
        }
    }

    private void spawnHitParticles(Vec3 position) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SWEEP_ATTACK,
                    position.x, position.y, position.z,
                    8, 0.5, 0.5, 0.5, 0.0
            );
        }
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}