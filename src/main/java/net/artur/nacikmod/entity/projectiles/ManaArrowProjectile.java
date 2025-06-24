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
    private static final float BASE_DAMAGE = 6F;

    public ManaArrowProjectile(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.setBaseDamage(BASE_DAMAGE);
        this.setKnockback(1); // Стандартный откидывающий эффект
        this.pickup = Pickup.DISALLOWED; // Нельзя подобрать
    }

    public ManaArrowProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.MANA_ARROW.get(), shooter, level);
        this.setBaseDamage(BASE_DAMAGE);
        this.setKnockback(1);
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

                // При желании — другие кастомные эффекты:
                // target.addEffect(new MobEffectInstance(ModEffects.YOUR_EFFECT.get(), 60, 1));
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


}
