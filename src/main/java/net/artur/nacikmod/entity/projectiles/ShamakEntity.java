package net.artur.nacikmod.entity.projectiles;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.artur.nacikmod.registry.ModEntities;

import java.util.List;
import java.util.WeakHashMap;

public class ShamakEntity extends Projectile {
    private static final int LIFETIME = 200;
    private static final double CLOUD_RADIUS_XZ = 4.5;
    private static final double CLOUD_RADIUS_Y_UP = 2.5; // 2.5 блока вверх
    private static final double CLOUD_RADIUS_Y_DOWN = 1.0; // 1 блок вниз (было 2.5)
    private static final int PARTICLES_PER_TICK = 100;
    private static final int EFFECT_DURATION = 80;

    private int age = 0;
    private final WeakHashMap<LivingEntity, Boolean> affectedEntities = new WeakHashMap<>();

    public ShamakEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public ShamakEntity(Level level, LivingEntity owner, double x, double y, double z) {
        super(ModEntities.SHAMAK.get(), level);
        this.setOwner(owner);
        this.setPos(x, y, z);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        // Не нужно синхронизировать данные
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            age++;

            // Проверяем, не истекло ли время жизни
            if (age >= LIFETIME) {
                this.discard();
                return;
            }

            // Создаем частицы черного дыма
            createSmokeParticles();

            // Применяем эффекты к сущностям в облаке
            applyEffectsToEntitiesInCloud();
        }
    }

    private void createSmokeParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < PARTICLES_PER_TICK; i++) {
                double angle = this.level().getRandom().nextDouble() * 2 * Math.PI;
                double radiusXZ = this.level().getRandom().nextDouble() * CLOUD_RADIUS_XZ;

                // Новое распределение по Y: от -1.0 до +2.5 блоков от центра
                double particleY = this.getY() +
                        (this.level().getRandom().nextDouble() * (CLOUD_RADIUS_Y_UP + CLOUD_RADIUS_Y_DOWN) - CLOUD_RADIUS_Y_DOWN);

                double particleX = this.getX() + radiusXZ * Math.cos(angle);
                double particleZ = this.getZ() + radiusXZ * Math.sin(angle);

                serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        particleX, particleY, particleZ,
                        3,
                        0.1, 0.1, 0.1,
                        0.01
                );

                if (i % 2 == 0) {
                    serverLevel.sendParticles(
                            ParticleTypes.LARGE_SMOKE,
                            particleX, particleY, particleZ,
                            1,
                            0.15, 0.15, 0.15,
                            0.01
                    );
                }

                if (i % 3 == 0) {
                    serverLevel.sendParticles(
                            ParticleTypes.SMOKE,
                            particleX + (this.level().getRandom().nextDouble() - 0.5) * 0.8,
                            particleY + (this.level().getRandom().nextDouble() - 0.5) * 0.8,
                            particleZ + (this.level().getRandom().nextDouble() - 0.5) * 0.8,
                            2,
                            0.08, 0.08, 0.08,
                            0.008
                    );
                }
            }

            for (int i = 0; i < 20; i++) {
                double edgeAngle = this.level().getRandom().nextDouble() * 2 * Math.PI;
                double edgeX = this.getX() + CLOUD_RADIUS_XZ * Math.cos(edgeAngle);
                double edgeZ = this.getZ() + CLOUD_RADIUS_XZ * Math.sin(edgeAngle);
                double edgeY = this.getY() +
                        (this.level().getRandom().nextDouble() * (CLOUD_RADIUS_Y_UP + CLOUD_RADIUS_Y_DOWN) - CLOUD_RADIUS_Y_DOWN);

                serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        edgeX, edgeY, edgeZ,
                        2,
                        0.05, 0.05, 0.05,
                        0.005
                );
            }
        }
    }

    private void applyEffectsToEntitiesInCloud() {
        // Обновляем AABB с новыми значениями высоты
        AABB cloudArea = new AABB(
                this.getX() - CLOUD_RADIUS_XZ, this.getY() - CLOUD_RADIUS_Y_DOWN, this.getZ() - CLOUD_RADIUS_XZ,
                this.getX() + CLOUD_RADIUS_XZ, this.getY() + CLOUD_RADIUS_Y_UP, this.getZ() + CLOUD_RADIUS_XZ
        );

        List<Entity> entities = this.level().getEntities(this, cloudArea);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity && entity != this.getOwner()) {
                double distanceXZ = Math.sqrt(
                        Math.pow(this.getX() - entity.getX(), 2) +
                                Math.pow(this.getZ() - entity.getZ(), 2)
                );
                double distanceY = entity.getY() - this.getY();

                // Проверяем новые границы по Y
                if (distanceXZ <= CLOUD_RADIUS_XZ &&
                        distanceY >= -CLOUD_RADIUS_Y_DOWN &&
                        distanceY <= CLOUD_RADIUS_Y_UP) {

                    if (!affectedEntities.containsKey(livingEntity)) {
                        applyShamakEffects(livingEntity);
                        affectedEntities.put(livingEntity, true);
                    }
                }
            }
        }
    }

    private void applyShamakEffects(LivingEntity target) {
        // Слепота на 3 секунды (однократное применение)
        target.addEffect(new MobEffectInstance(
                MobEffects.BLINDNESS,
                EFFECT_DURATION,
                0,
                false, // Не амбиентный
                true,  // Показывать частицы
                true   // Показывать иконку
        ));

        // Медлительность на 3 секунды (однократное применение)
        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                EFFECT_DURATION,
                0,
                false, // Не амбиентный
                true,  // Показывать частицы
                true   // Показывать иконку
        ));
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        // Не вызываем стандартную обработку
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        // Не вызываем стандартную обработку
    }

    @Override
    public boolean isInWater() {
        return false; // Облако не должно исчезать в воде
    }
}