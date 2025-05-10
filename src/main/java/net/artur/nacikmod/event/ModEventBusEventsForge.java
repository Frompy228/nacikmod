package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.MagicCharm;
import net.artur.nacikmod.registry.ModAttributes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
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
            double reductionPercentage = Math.min(bonusArmor * 0.025, 0.9);
            float reducedDamage = (float) (event.getAmount() * (1 - reductionPercentage));
            event.setAmount(reducedDamage);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            // Проверяем, держит ли игрок ManaSword
            ItemStack heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof ManaSword) {
                // Получаем дополнительный урон из NBT
                if (heldItem.hasTag() && heldItem.getTag().contains("ManaDamage")) {
                    int manaDamage = heldItem.getTag().getInt("ManaDamage");
                    // Добавляем дополнительный урон к базовому
                    event.setAmount(event.getAmount() + manaDamage);
                }
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