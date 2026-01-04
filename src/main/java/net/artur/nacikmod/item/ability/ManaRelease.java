package net.artur.nacikmod.item.ability;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.Release;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.registry.ModEffects;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.artur.nacikmod.network.AbilityStateManager;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class ManaRelease {
    public static final Set<UUID> activeReleasePlayers = new HashSet<>();
    private static final String ACTIVE_TAG = "active";
    private static final String LEVEL_TAG = "level";
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e");
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f");
    private static final int MANA_CHECK_INTERVAL = 20;

    public static class Level {
        public final int damage;
        public final int armor;
        public final int manaCost;
        public final String name;
        public final double speedBonus;

        public Level(int damage, int armor, int manaCost, String name, double speedBonus) {
            this.damage = damage;
            this.armor = armor;
            this.manaCost = manaCost;
            this.name = name;
            this.speedBonus = speedBonus;
        }
    }

    public static final Level[] LEVELS = {
        new Level(1, 2, 10, "Level 1", 0.0),
        new Level(2, 3, 20, "Level 2", 0.02)
    };

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Проверяем все предметы Release в инвентаре
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof Release) {
                    // Если предмет помечен как активный, но игрок не в списке активных
                    if (stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG) && !activeReleasePlayers.contains(player.getUUID())) {
                        stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                    }
                }
            }
        }
    }

    public static void startRelease(Player player) {
        // Проверяем, не активен ли LastMagic
        if (player.hasEffect(ModEffects.MANA_LAST_MAGIC.get())) {
            if (player instanceof ServerPlayer) {
                player.sendSystemMessage(Component.literal("Cannot use Release while Last Magic is active")
                        .withStyle(ChatFormatting.AQUA));
            }
            // Важно: обновляем состояние всех предметов Release в инвентаре
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof Release) {
                    stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                }
            }
            return;
        }

        // Сначала добавляем игрока в список активных
        activeReleasePlayers.add(player.getUUID());
        
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof Release) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                if (!stack.hasTag() || !stack.getTag().contains(LEVEL_TAG)) {
                    stack.getOrCreateTag().putInt(LEVEL_TAG, 0);
                }
                applyModifiers(player, getCurrentLevel(stack));
                
                // Отправляем пакет всем игрокам поблизости (включая владельца)
                if (player instanceof ServerPlayer serverPlayer) {
                    AbilityStateManager.syncAbilityState(serverPlayer, "release", true);
                }
                break;
            }
        }
    }

    public static void stopRelease(Player player) {
        if (!(player instanceof ServerPlayer)) return;

        // Удаляем игрока из списка активных
        activeReleasePlayers.remove(player.getUUID());

        // Обновляем состояние всех предметов Release в инвентаре
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof Release) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            }
        }

        // Удаляем модификаторы
        removeModifiers(player);

        // Отправляем пакет всем игрокам поблизости (включая владельца)
        AbilityStateManager.syncAbilityState((ServerPlayer) player, "release", false);
    }

    public static void switchLevel(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof Release && stack.hasTag()) {
                int currentLevel = stack.getTag().getInt(LEVEL_TAG);
                int newLevel = (currentLevel + 1) % LEVELS.length;
                stack.getTag().putInt(LEVEL_TAG, newLevel);
                
                // Remove old modifiers and apply new ones if effect is active
                if (activeReleasePlayers.contains(player.getUUID())) {
                    removeModifiers(player);
                    applyModifiers(player, LEVELS[newLevel]);
                }
                
                // Send level switch message
                if (player instanceof ServerPlayer) {
                    player.sendSystemMessage(Component.literal("Switched to " + LEVELS[newLevel].name)
                            .withStyle(ChatFormatting.AQUA));
                }
                break;
            }
        }
    }

    private static void applyModifiers(Player player, Level level) {
        // Add damage modifier
        AttributeModifier damageModifier = new AttributeModifier(
            DAMAGE_MODIFIER_UUID,
            "mana_release_damage_bonus",
            level.damage,
            AttributeModifier.Operation.ADDITION
        );
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(damageModifier);

        // Add bonus armor modifier
        AttributeModifier armorModifier = new AttributeModifier(
            ARMOR_MODIFIER_UUID,
            "mana_release_armor_bonus",
            level.armor,
            AttributeModifier.Operation.ADDITION
        );
        player.getAttribute(ModAttributes.BONUS_ARMOR.get()).addTransientModifier(armorModifier);

        // Add speed modifier if level has speed bonus
        if (level.speedBonus > 0) {
            AttributeModifier speedModifier = new AttributeModifier(
                SPEED_MODIFIER_UUID,
                "mana_release_speed_bonus",
                level.speedBonus,
                AttributeModifier.Operation.ADDITION
            );
            player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(speedModifier);
        }
    }

    private static void removeModifiers(Player player) {
        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(DAMAGE_MODIFIER_UUID);
        player.getAttribute(ModAttributes.BONUS_ARMOR.get()).removeModifier(ARMOR_MODIFIER_UUID);
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_UUID);
    }

    public static Level getCurrentLevel(ItemStack stack) {
        int level = stack.hasTag() ? stack.getTag().getInt(LEVEL_TAG) : 0;
        return LEVELS[level];
    }

    public static Level getCurrentLevel(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof Release && 
                stack.hasTag() && 
                stack.getTag().getBoolean(ACTIVE_TAG)) {
                return getCurrentLevel(stack);
            }
        }
        return LEVELS[0];
    }

    public static boolean isReleaseActive(Player player) {
        return activeReleasePlayers.contains(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (player.tickCount % MANA_CHECK_INTERVAL != 0) return;

        // Проверяем наличие LastMagic эффекта
        if (player.hasEffect(ModEffects.MANA_LAST_MAGIC.get())) {
            // Если LastMagic активен, отключаем Release
            if (activeReleasePlayers.contains(player.getUUID())) {
                stopRelease(player);
            }
            // Обновляем состояние всех предметов Release
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof Release) {
                    stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                }
            }
            return;
        }

        // Проверяем все предметы Release в инвентаре
        boolean hasActiveItem = false;
        ItemStack releaseItem = null;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof Release) {
                if (stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG)) {
                    hasActiveItem = true;
                    releaseItem = stack;
                } else if (activeReleasePlayers.contains(player.getUUID())) {
                    // Если предмет помечен как неактивный, но игрок в списке активных
                    stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                }
            }
        }

        // Если нет активного предмета, но игрок в списке активных
        if (!hasActiveItem && activeReleasePlayers.contains(player.getUUID())) {
            stopRelease(player);
            return;
        }

        // Если эффект активен и предмет есть в инвентаре
        if (activeReleasePlayers.contains(player.getUUID()) && releaseItem != null) {
            Level currentLevel = getCurrentLevel(releaseItem);
            // Check if player has enough mana
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                if (mana.getMana() >= currentLevel.manaCost) {
                    mana.removeMana(currentLevel.manaCost);
                } else {
                    stopRelease(player);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        ItemStack pickedStack = event.getItem().getItem();
        if (pickedStack.getItem() instanceof Release) {
            // Если игрок не в списке активных, но предмет помечен как активный
            if (!activeReleasePlayers.contains(player.getUUID()) && 
                pickedStack.hasTag() && 
                pickedStack.getTag().getBoolean(ACTIVE_TAG)) {
                pickedStack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            }
        }
    }
}

