package net.artur.nacikmod.entity.projectiles;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.artur.nacikmod.util.KnightUtils;
import net.artur.nacikmod.util.SafeFakePlayer;

import java.lang.reflect.Field;
import java.util.UUID;

public class WeaponProjectile extends Arrow {
    private static final EntityDataAccessor<ItemStack> STACK =
            SynchedEntityData.defineId(WeaponProjectile.class, EntityDataSerializers.ITEM_STACK);

    private int life = 0;
    // UUID из Mahou Tsukai для модификатора урона
    protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    
    // Рефлексия для доступа к protected полю attackStrengthTicker
    private static Field attackStrengthTickerField = null;
    
    static {
        try {
            attackStrengthTickerField = LivingEntity.class.getDeclaredField("attackStrengthTicker");
            attackStrengthTickerField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // Поле не найдено, будет использован альтернативный метод
        }
    }

    public WeaponProjectile(EntityType<? extends Arrow> type, Level world) {
        super(type, world);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STACK, ItemStack.EMPTY);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity shooter = this.getOwner();

        if (target == shooter) return;
        if (target instanceof LivingEntity living && KnightUtils.isKnight(living)) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            SafeFakePlayer fakePlayer = new SafeFakePlayer(serverLevel, "faker");
            ItemStack stack = this.getStack();

            if (!stack.isEmpty() && !(target instanceof AbstractArrow)) {
                // 1. Даем меч в руку
                fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, stack);

                // 2. ПРИМЕНЯЕМ АТРИБУТЫ ОТ ПРЕДМЕТА (как в примере)
                // Используем addTransientAttributeModifiers для применения ВСЕХ модификаторов от стека
                Multimap<Attribute, AttributeModifier> stackModifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
                fakePlayer.getAttributes().addTransientAttributeModifiers(stackModifiers);

                // 3. СОЗДАЕМ ДОПОЛНИТЕЛЬНЫЕ МОДИФИКАТОРЫ (+10 урона от кастера)
                HashMultimap<Attribute, AttributeModifier> additionalModifiers = HashMultimap.create();
                // Добавляем +10 к базовому урону меча
                additionalModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                    BASE_ATTACK_DAMAGE_UUID, "caster_sword_bonus", 10.0, AttributeModifier.Operation.ADDITION
                ));
                
                // Применяем дополнительные модификаторы
                if (!additionalModifiers.isEmpty()) {
                    fakePlayer.getAttributes().addTransientAttributeModifiers(additionalModifiers);
                }

                // 4. УСТАНАВЛИВАЕМ ПОЛНЫЙ ЗАМАХ (как в примере: attackStrengthTicker = 1000)
                // Это критически важно для правильного урона!
                // Используем рефлексию для доступа к protected полю
                try {
                    if (attackStrengthTickerField != null) {
                        attackStrengthTickerField.setInt(fakePlayer, 1000);
                    } else {
                        // Альтернатива: используем resetAttackStrengthTicker (может быть менее эффективно)
                        fakePlayer.resetAttackStrengthTicker();
                    }
                } catch (IllegalAccessException e) {
                    // Если рефлексия не работает, используем альтернативный метод
                    fakePlayer.resetAttackStrengthTicker();
                }



                // 6. НАНОСИМ УРОН
                fakePlayer.attack(target);

                // 7. ОЧИСТКА (Важно для стабильности сервера)
                // Удаляем все модификаторы, которые мы добавили
                fakePlayer.getAttributes().removeAttributeModifiers(stackModifiers);
                if (!additionalModifiers.isEmpty()) {
                    fakePlayer.getAttributes().removeAttributeModifiers(additionalModifiers);
                }
                fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }

            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        // Ограничиваем inGround до 4 (как в примере), чтобы предотвратить преждевременное застревание
        if (this.inGroundTime > 4) {
            this.inGroundTime = 4;
        }
        
        // Логика вращения для рендера
        if (!this.inGround) {
            Vec3 motion = this.getDeltaMovement();
            if (motion.lengthSqr() > 0.001D) {
                this.setYRot((float) (Mth.atan2(motion.x, motion.z) * (180D / Math.PI)));
                this.setXRot((float) (Mth.atan2(motion.y, motion.horizontalDistance()) * (180D / Math.PI)));
            }
        }
        
        this.life++;
        if (this.life > 360) {
            this.discard();
        }
    }

    // Сохранение и синхронизация
    public void setStack(ItemStack stack) { 
        this.entityData.set(STACK, stack.copy()); 
    }
    
    public ItemStack getStack() { 
        return this.entityData.get(STACK); 
    }
    
    @Override 
    public void addAdditionalSaveData(CompoundTag tag) { 
        super.addAdditionalSaveData(tag); 
        if (!this.getStack().isEmpty()) {
            tag.put("stackNBT", this.getStack().save(new CompoundTag()));
        }
        tag.putInt("wpe_life", this.life);
    }
    
    @Override 
    public void readAdditionalSaveData(CompoundTag tag) { 
        super.readAdditionalSaveData(tag); 
        if (tag.contains("stackNBT")) {
            ItemStack stack = ItemStack.of(tag.getCompound("stackNBT"));
            stack.setCount(1); // Убеждаемся, что количество = 1
            this.setStack(stack);
        }
        if (tag.contains("wpe_life")) {
            this.life = tag.getInt("wpe_life");
        }
    }
}