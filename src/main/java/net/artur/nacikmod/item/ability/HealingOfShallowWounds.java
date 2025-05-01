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

    public static void startHealing(Player player) {
        // Находим предмет в инвентаре
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof MagicHealing) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                activeHealingPlayers.add(player.getUUID());
                break;
            }
        }
    }

    public static void stopHealing(Player player) {
        // Находим предмет в инвентаре
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof MagicHealing) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                break;
            }
        }
        activeHealingPlayers.remove(player.getUUID());
    }

    public static boolean isHealingActive(Player player) {
        // Проверяем наличие активного предмета в инвентаре
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof MagicHealing && 
                stack.hasTag() && 
                stack.getTag().getBoolean(ACTIVE_TAG)) {
                return true;
            }
        }
        return false;
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

        // Проверяем наличие активного предмета в инвентаре
        boolean hasActiveItem = false;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof MagicHealing && 
                stack.hasTag() && 
                stack.getTag().getBoolean(ACTIVE_TAG)) {
                hasActiveItem = true;
                break;
            }
        }

        // Если предмета нет в инвентаре, отключаем эффект
        if (!hasActiveItem) {
            if (activeHealingPlayers.contains(player.getUUID())) {
                activeHealingPlayers.remove(player.getUUID());
            }
            return;
        }

        // Если эффект активен и предмет есть в инвентаре
        if (activeHealingPlayers.contains(player.getUUID())) {
            // Check if player has enough mana
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                if (mana.getMana() >= MANA_COST_PER_SECOND) {
                    mana.removeMana(MANA_COST_PER_SECOND);
                    
                    // Apply healing effect every 3 seconds
                    if (player.tickCount % 60 == 0) { // 60 ticks = 3 seconds
                        float healingAmount = calculateHealingAmount(player);
                        player.heal(healingAmount);
                    }
                } else {
                    stopHealing(player);
                }
            });
        }
    }
}
