package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.active_abilities.ActiveAbilitiesProvider;
import net.artur.nacikmod.entity.custom.InquisitorEntity;
import net.artur.nacikmod.item.Gravity;
import net.artur.nacikmod.item.ability.BloodCircleManager;
import net.artur.nacikmod.item.ability.IntangibilityAbility;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.registry.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
            double reductionPercentage = Math.min(bonusArmor * 0.02, 0.8);
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

    private static final double MAX_DISTANCE = 100.0;
    private static final double CIRCLE_AOE_RANGE = 15.0;

    // Тэг для пометки урона, чтобы избежать рекурсии
    private static boolean isProcessingContract = false;

    /**
     * Улучшенная логика передачи урона
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBloodContractDamage(LivingHurtEvent event) {
        if (isProcessingContract) return;

        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;

        DamageSource originalSource = event.getSource();

        // СЛУЧАЙ 1: Игрок получает урон -> Передаем МОБАМ (Виноват Игрок)
        if (victim instanceof Player player) {
            player.getCapability(ActiveAbilitiesProvider.ACTIVE_ABILITIES_CAPABILITY).ifPresent(cap -> {
                if (cap.isBloodContractActive()) {
                    isProcessingContract = true;
                    try {
                        float amount = event.getAmount();
                        // Здесь 'player' выступает как источник (cause), так как это его контракт бьет мобов
                        DamageSource contractSource = copySourceWithNewCause(originalSource, player);

                        if (BloodCircleManager.isActive(player)) {
                            AABB area = player.getBoundingBox().inflate(CIRCLE_AOE_RANGE);
                            List<LivingEntity> nearby = player.level().getEntitiesOfClass(LivingEntity.class, area,
                                    e -> e != player && e.isAlive());

                            for (LivingEntity linked : nearby) {
                                linked.hurt(contractSource, amount);
                            }
                        } else if (cap.getContractTargetUUID() != null) {
                            Entity target = ((ServerLevel)player.level()).getEntity(cap.getContractTargetUUID());
                            if (target instanceof LivingEntity livingTarget && livingTarget.isAlive()) {
                                livingTarget.hurt(contractSource, amount);
                            }
                        }
                    } finally {
                        isProcessingContract = false;
                    }
                }
            });
        }

        // СЛУЧАЙ 2: Моба ударили -> Передаем Игроку (Виноват Моб)
        for (Player p : victim.level().players()) {
            if (p.distanceTo(victim) > MAX_DISTANCE) continue;

            p.getCapability(ActiveAbilitiesProvider.ACTIVE_ABILITIES_CAPABILITY).ifPresent(cap -> {
                if (cap.isBloodContractActive() && victim.getUUID().equals(cap.getContractTargetUUID())) {
                    // Если игрок сам ударил моба, не возвращаем урон игроку (анти-суицид)
                    if (originalSource.getEntity() != p) {
                        isProcessingContract = true;
                        try {
                            // Здесь 'victim' (моб) выступает как источник урона для игрока
                            p.hurt(copySourceWithNewCause(originalSource, victim), event.getAmount());
                        } finally {
                            isProcessingContract = false;
                        }
                    }
                }
            });
        }
    }

    /**
     * Создает копию источника урона, но заменяет того, кто нанес урон, на указанную сущность.
     * Это делает передачу урона "легальной" для ИИ других модов.
     */
    private static DamageSource copySourceWithNewCause(DamageSource original, Entity newCause) {
        // Используем конструктор, где:
        // 1. Holder<DamageType> оставляем оригинальный (стрела, огонь, падение)
        // 2. DirectEntity (снаряд) ставим null, так как это "эхо" урона, а не сама стрела
        // 3. CausingEntity (виновник) ставим нашего игрока или моба-посредника
        return new DamageSource(original.typeHolder(), null, newCause);
    }

}