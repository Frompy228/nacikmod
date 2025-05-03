package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.item.MagicHealing;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class HealingOfShallowWounds {
    private static final Set<UUID> activeHealingPlayers = new HashSet<>();
    private static final int MANA_COST_PER_SECOND = 10;
    private static final int BASE_HEALING = 1;
    private static final int MANA_THRESHOLD = 750;
    private static final String ACTIVE_TAG = "active";
    private static final int HEALING_EFFECT_DURATION = 20; // 1 second

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Проверяем все предметы MagicHealing в инвентаре
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof MagicHealing) {
                    // Если предмет помечен как активный, но игрок не в списке активных
                    if (stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG) && !activeHealingPlayers.contains(player.getUUID())) {
                        stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                    }
                }
            }
        }
    }

    public static void startHealing(Player player) {
        // Сначала добавляем игрока в список активных
        activeHealingPlayers.add(player.getUUID());
        
        // Находим предмет в инвентаре
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof MagicHealing) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                break;
            }
        }

        // Применяем эффект регенерации
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, HEALING_EFFECT_DURATION, 0, true, false));
    }

    public static void stopHealing(Player player) {
        // Сначала удаляем игрока из списка активных
        activeHealingPlayers.remove(player.getUUID());
        
        // Находим предмет в инвентаре
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof MagicHealing) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            }
        }

        // Удаляем эффект регенерации
        player.removeEffect(MobEffects.REGENERATION);
    }

    public static boolean isHealingActive(Player player) {
        return activeHealingPlayers.contains(player.getUUID()) && 
               player.hasEffect(MobEffects.REGENERATION);
    }

    public static float calculateHealingAmount(Player player) {
        return player.getCapability(ManaProvider.MANA_CAPABILITY)
                .map(mana -> {
                    int maxMana = mana.getMaxMana();
                    int additionalHealing = maxMana / MANA_THRESHOLD;
                    return BASE_HEALING + additionalHealing;
                })
                .orElse(BASE_HEALING);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (player.tickCount % 20 != 0) return;

        // Проверяем все предметы MagicHealing в инвентаре
        boolean hasActiveItem = false;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof MagicHealing) {
                if (stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG)) {
                    hasActiveItem = true;
                } else if (activeHealingPlayers.contains(player.getUUID())) {
                    // Если предмет помечен как неактивный, но игрок в списке активных
                    stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                }
            }
        }

        // Если нет активного предмета или эффекта, но игрок в списке активных
        if ((!hasActiveItem || !player.hasEffect(MobEffects.REGENERATION)) && 
            activeHealingPlayers.contains(player.getUUID())) {
            stopHealing(player);
            return;
        }

        // Если эффект активен и предмет есть в инвентаре
        if (activeHealingPlayers.contains(player.getUUID()) && hasActiveItem) {
            // Check if player has enough mana
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                if (mana.getMana() >= MANA_COST_PER_SECOND) {
                    mana.removeMana(MANA_COST_PER_SECOND);
                    
                    // Обновляем эффект регенерации
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, HEALING_EFFECT_DURATION, 0, true, false));
                } else {
                    stopHealing(player);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        ItemStack pickedStack = event.getItem().getItem();
        if (pickedStack.getItem() instanceof MagicHealing) {
            // Если игрок не в списке активных, но предмет помечен как активный
            if (!activeHealingPlayers.contains(player.getUUID()) && 
                pickedStack.hasTag() && 
                pickedStack.getTag().getBoolean(ACTIVE_TAG)) {
                pickedStack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            }
        }
    }
}
