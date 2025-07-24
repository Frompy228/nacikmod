package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.reward.PlayerRewardsProvider;
import net.artur.nacikmod.item.MagicCharm;
import net.artur.nacikmod.item.CursedSword;
import net.artur.nacikmod.registry.ModAttributes;
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
import net.artur.nacikmod.item.ManaSword;
import net.artur.nacikmod.effect.EffectBloodExplosion;

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
            // Проверяем, держит ли игрок ManaSword
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
            
            AttackHandler.onAttack(event.getEntity(), player);
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
        }
    }

}