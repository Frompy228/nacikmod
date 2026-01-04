package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.InquisitorEntity;
import net.artur.nacikmod.item.Gravity;
import net.artur.nacikmod.item.ability.IntangibilityAbility;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.registry.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import net.artur.nacikmod.effect.EffectManablessing;


@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEventBusEventsForge {

    @Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (KeyBindings.ABILITY_KEY.isDown()) {
                KeyBindings.ABILITY_KEY.consumeClick();
            }
            
            // Обработка кнопки активации Кодайгана (Z)
            if (KeyBindings.KODAI_KEY.consumeClick()) {
                // Проверяем, есть ли у игрока Vision Blessing статус
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                        if (mana.hasVisionBlessing()) {
                            // Отправляем пакет для переключения Кодайгана (только один раз при нажатии)
                            ModMessages.sendToServer(new net.artur.nacikmod.network.KodaiTogglePacket());
                        }
                    });
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onMouseInput(InputEvent.InteractionKeyMappingTriggered event) {

            // 1. Проверяем, нажал ли игрок кнопку атаки (Left Click)
            if (event.isAttack()) {
                Minecraft mc = Minecraft.getInstance();

                // 2. Проверяем, находится ли игрок в состоянии "Неосязаемость"
                // Важно: Мы используем статический метод из IntangibilityAbility
                if (mc.player != null && IntangibilityAbility.isIntangible(mc.player)) {

                    // 3. Полностью отменяем ввод клавиши атаки.
                    // Это останавливает анимацию замаха и предотвращает срабатывание
                    // неотменяемого события LeftClickEmpty.
                    event.setCanceled(true);
                    event.setSwingHand(false); // Запрещаем анимацию замаха руки
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity entity = event.getEntity();
        AttributeInstance attribute = entity.getAttribute(ModAttributes.BONUS_ARMOR.get());

        if (attribute != null) {
            double bonusArmor = attribute.getValue();
            double reductionPercentage = Math.min(bonusArmor * 0.022, 0.8);
            float reducedDamage = (float) (event.getAmount() * (1 - reductionPercentage));
            event.setAmount(reducedDamage);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {

            // Проверяем наличие эффекта ManaLastMagic у игрока
            if (player.hasEffect(ModEffects.MANA_LAST_MAGIC.get())) {
                LivingEntity target = event.getEntity();
                
                // Наносим урон по области в радиусе 1.5 блока от атакуемой цели
                double areaRadius = 1.5;
                AABB area = new AABB(
                        target.getX() - areaRadius, target.getY() - areaRadius, target.getZ() - areaRadius,
                        target.getX() + areaRadius, target.getY() + areaRadius, target.getZ() + areaRadius
                );

                // Ищем все сущности в радиусе 1.5 блока от цели
                List<LivingEntity> nearbyEntities = target.level().getEntitiesOfClass(
                        LivingEntity.class,
                        area,
                        entity -> entity != target && entity != player && entity.isAlive()
                );

                // Наносим урон 5 единиц всем найденным сущностям
                for (LivingEntity nearbyEntity : nearbyEntities) {
                    nearbyEntity.hurt(player.damageSources().playerAttack(player), 5.0f);
                }
            }
            
            AttackHandler.onAttack(event.getEntity(), player);
        }
    }

    @SubscribeEvent
    public static void onMobEffectRemove(MobEffectEvent.Remove event) {
        // Предотвращаем снятие эффектов EffectManablessing и EffectMageMark
        if (event.getEffect() instanceof EffectManablessing) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Check for Blood Explosion effect
        if (entity.hasEffect(ModEffects.BLOOD_EXPLOSION.get())) {
            // Trigger the effect's remove method to cause explosion
            entity.removeEffect(ModEffects.BLOOD_EXPLOSION.get());
        }
        // Existing mana check code
        if (entity instanceof Player player) {
            if (player.hasEffect(ModEffects.MANA_LAST_MAGIC.get())) {
                player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                    mana.setMaxMana(0);
                    mana.setMana(0);
                });
            }

            // --- GOD HAND LOGIC ---
            boolean saved = false;
            int godHandLevel = 0;
            int godHandDuration = 500;
            // 1. Если есть предмет GodHand в инвентаре
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() == ModItems.GOD_HAND.get()) {
                    // Спасаем игрока
                    event.setCanceled(true);
                    player.setHealth(10.0F);
                    stack.shrink(1);
                    // Накладываем эффект GodHand 0 уровня (amplifier = 0)
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        ModEffects.GOD_HAND.get(), godHandDuration, 0, false, true, true));
                    saved = true;
                    godHandLevel = 0;
                    break;
                }
            }
            // 2. Если есть эффект GodHand (до 5 уровня)
            if (!saved && player.hasEffect(ModEffects.GOD_HAND.get())) {
                net.minecraft.world.effect.MobEffectInstance effect = player.getEffect(ModEffects.GOD_HAND.get());
                int amplifier = effect.getAmplifier();
                if (amplifier < 4) { // максимум 5 уровень (amplifier = 4)
                    event.setCanceled(true);
                    player.setHealth(10.0F);
                    // Накладываем эффект с уровнем выше и обновляем длительность
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        ModEffects.GOD_HAND.get(), godHandDuration, amplifier + 1, false, true, true));
                    godHandLevel = amplifier + 1;
                    saved = true;
                }
            }
            // 3. Если спасли, спавним частицы и проигрываем звук
            if (saved && !player.level().isClientSide) {
                // Спавним золотые частицы вокруг игрока
                if (player.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 40; i++) {
                        double dx = (player.getRandom().nextDouble() - 0.5) * player.getBbWidth() * 2.5;
                        double dy = player.getRandom().nextDouble() * player.getBbHeight();
                        double dz = (player.getRandom().nextDouble() - 0.5) * player.getBbWidth() * 2.5;
                        serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.GLOW,
                            player.getX() + dx,
                            player.getY() + dy + 0.5,
                            player.getZ() + dz,
                            1, 0, 0, 0, 0.15
                        );
                    }
                    // Проигрываем звук god_hand
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.artur.nacikmod.registry.ModSounds.GOD_HAND.get(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
        LightningBolt lightning = event.getLightning();
        if (!lightning.getPersistentData().getBoolean(InquisitorEntity.ANTI_FLIGHT_LIGHTNING_TAG)) {
            return;
        }

        Entity struck = event.getEntity();
        if (struck instanceof Player player) {
            if (Gravity.isFlyingActive(player)) {
                Gravity.stopFlying(player);
            } else if (player.getAbilities().flying) {
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }

        if (struck instanceof LivingEntity livingEntity) {
            if (livingEntity.isNoGravity()) {
                livingEntity.setNoGravity(false);
            }
            Vec3 motion = livingEntity.getDeltaMovement();
            livingEntity.setDeltaMovement(motion.x, Math.min(motion.y, 0.0D) - 0.2D, motion.z);
        }
    }

}