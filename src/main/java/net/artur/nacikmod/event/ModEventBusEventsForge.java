package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.reward.PlayerRewardsProvider;
import net.artur.nacikmod.item.MagicCharm;
import net.artur.nacikmod.item.CursedSword;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.effect.EffectManaLastMagic;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
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
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity entity = event.getEntity();
        AttributeInstance attribute = entity.getAttribute(ModAttributes.BONUS_ARMOR.get());

        if (attribute != null) {
            double bonusArmor = attribute.getValue();
            double reductionPercentage = Math.min(bonusArmor * 0.02, 0.9);
            float reducedDamage = (float) (event.getAmount() * (1 - reductionPercentage));
            event.setAmount(reducedDamage);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {

            ItemStack heldItem = player.getMainHandItem();

            
            // Проверяем, держит ли игрок CursedSword
            if (heldItem.getItem() instanceof CursedSword) {
                // Получаем значение бонусной брони у цели
                AttributeInstance bonusArmor = event.getEntity().getAttribute(ModAttributes.BONUS_ARMOR.get());
                double bonus = bonusArmor != null ? bonusArmor.getValue() : 0.0;
                float extraDamage = (float) (bonus * 0.45); // 0.45 урона за 1 бонусной брони
                
                if (extraDamage > 0) {
                    // Добавляем дополнительный урон к базовому
                    event.setAmount(event.getAmount() + extraDamage);
                }
                
                // Сжигаем ману у цели (у всех существ с способностью маны)
                LivingEntity target = event.getEntity();
                float damageDealt = event.getAmount();
                int manaToBurn = (int) (damageDealt * 5); // 5 маны за 1 урон
                
                target.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                    int currentMana = mana.getMana();
                    int burnedMana = Math.min(currentMana, manaToBurn);
                    mana.removeMana(burnedMana);
                });
            }
            
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
}