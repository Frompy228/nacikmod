package net.artur.nacikmod.entity.projectiles;

import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

public class ManaArrowProjectile extends AbstractArrow {
    // ЭКСТРЕМАЛЬНЫЙ БАЗОВЫЙ УРОН:
    // На полном заряде (скорость 5.0 * база 7.0) = 35 урона без чар.
    private static final double EXTREME_BASE_FACTOR = 7.0D;
    private boolean flame = false;

    public ManaArrowProjectile(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.pickup = Pickup.DISALLOWED;
    }

    public ManaArrowProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.MANA_ARROW.get(), shooter, level);
        this.pickup = Pickup.DISALLOWED;
    }

    /**
     * @param power значение натяжения от 0.0 до 1.0
     */
    public void applyEnchantments(ItemStack bowStack, float power) {
        // Базовый урон теперь напрямую зависит от силы натяжения.
        // Если прокликать быстро, power будет ~0.05 -> урон будет ничтожным.
        double dynamicBase = EXTREME_BASE_FACTOR * power;

        int powerLevel = bowStack.getEnchantmentLevel(Enchantments.POWER_ARROWS);
        if (powerLevel > 0) {
            // Зачарование "Сила" добавляет бонус, который также масштабируется от натяжения
            dynamicBase += ((double)powerLevel * 0.8D + 0.5D) * power;
        }

        this.setBaseDamage(dynamicBase);

        // Откидывание
        int punchLevel = bowStack.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
        if (punchLevel > 0) {
            this.setKnockback(punchLevel);
        }

        // Огонь
        if (bowStack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0) {
            this.flame = true;
            this.setSecondsOnFire(100);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(ModItems.MANA_CRYSTAL.get());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            // Замедление на 1.5 сек
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1));
            if (this.flame) target.setSecondsOnFire(5);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}