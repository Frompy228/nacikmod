package net.artur.nacikmod.entity.projectiles;

import net.artur.nacikmod.registry.ModEffects;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class ManaArrowProjectile extends AbstractArrow {
    private static final float BASE_DAMAGE = 5F;
    private int powerLevel = 0;
    private int punchLevel = 0;
    private boolean flame = false;

    public ManaArrowProjectile(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.setBaseDamage(BASE_DAMAGE);
        this.setKnockback(0); // Стандартный откидывающий эффект
        this.pickup = Pickup.DISALLOWED; // Нельзя подобрать
    }

    public ManaArrowProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.MANA_ARROW.get(), shooter, level);
        this.setBaseDamage(BASE_DAMAGE);
        this.setKnockback(0);
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(ModItems.MANA_CRYSTAL.get());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide()) {
            if (result.getEntity() instanceof LivingEntity target) {
                // Наложение эффекта замедления на 1 сек (20 тиков)
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0));
                // Если есть Flame, поджигаем цель
                if (flame) {
                    target.setSecondsOnFire(5);
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.discard(); // Удаляем стрелу после попадания
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Удаляем стрелу через 10 секунд
        if (!this.level().isClientSide() && this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        // Для корректного отображения клиенту
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void setPowerLevel(int powerLevel) {
        this.powerLevel = powerLevel;
        // Vanilla: Power увеличивает урон на 1.5 за уровень
        this.setBaseDamage(BASE_DAMAGE + powerLevel * 1.5F);
    }

    public void setPunchLevel(int punchLevel) {
        this.punchLevel = punchLevel;
        this.setKnockback(punchLevel);
    }

    public void setFlame(boolean flame) {
        this.flame = flame;
        if (flame) this.setSecondsOnFire(100);
    }
}
