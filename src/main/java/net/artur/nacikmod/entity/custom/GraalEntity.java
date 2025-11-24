package net.artur.nacikmod.entity.custom;

import net.artur.nacikmod.lib.ExplosionHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class GraalEntity extends Monster {
    
    public GraalEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0) // 500 HP
                .add(Attributes.MOVEMENT_SPEED, 0.0) // Неподвижна
                .add(Attributes.FOLLOW_RANGE, 0.0) // Не следует за игроком
                .add(Attributes.KNOCKBACK_RESISTANCE, 112121221.0); // Полная устойчивость к откидыванию
    }

    @Override
    protected void registerGoals() {
        // Только базовые цели без движения и без поворота головы
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Убрали LookAtPlayerGoal и RandomLookAroundGoal - сущность не должна поворачиваться
        // Нет целей атаки - сущность не атакует
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.setDeltaMovement(0, 0, 0); // Обнуляем скорость
        this.setNoGravity(true);        // Отключаем гравитацию
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            // Запускаем кастомный ExplosionHelper с радиусом 300
            ExplosionHelper explosion = new ExplosionHelper(serverLevel, this.blockPosition(), 125);
            explosion.start();

            // Звуковой эффект
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 10.0F, 0.8F + this.random.nextFloat() * 0.4F);
        }
    }


    @Override
    public boolean isPushable() {
        return false; // Нельзя толкать
    }

    @Override
    public boolean canBeCollidedWith() {
        return true; // Можно атаковать
    }
}
