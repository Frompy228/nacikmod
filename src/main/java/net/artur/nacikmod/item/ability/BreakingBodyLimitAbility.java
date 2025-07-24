package net.artur.nacikmod.item.ability;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.BreakingBodyLimit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.artur.nacikmod.registry.ModDamageTypes;
import net.minecraft.server.level.ServerLevel;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class BreakingBodyLimitAbility {
    public static final Set<UUID> activePlayers = new HashSet<>();
    private static final String ACTIVE_TAG = "active";
    private static final String LEVEL_TAG = "level";
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("b1a2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private static final int HP_CHECK_INTERVAL = 25;

    public static class Level {
        public final int hpCost;
        public final int damageBonus;
        public final int strengthAmplifier;
        public final int speedAmplifier;
        public final int jumpAmplifier;
        public final String name;

        public Level(int hpCost, int damageBonus, int strengthAmplifier, int speedAmplifier, int jumpAmplifier, String name) {
            this.hpCost = hpCost;
            this.damageBonus = damageBonus;
            this.strengthAmplifier = strengthAmplifier;
            this.speedAmplifier = speedAmplifier;
            this.jumpAmplifier = jumpAmplifier;
            this.name = name;
        }
    }

    public static final Level[] LEVELS = {
        new Level(2, 2, 0, 1, 0, "Level 1"),   // +2 урона, скорость 1
        new Level(4, 3, 0, 2, 0, "Level 2"),  // +3 урона, сила 0, скорость 2
        new Level(6, 4, 0, 2, 1, "Level 3"),  // +4 урона, сила 0, скорость 2, прыгучесть 1
        new Level(8, 5, 1, 3, 2, "Level 4"),  // +5 урона, сила 1, скорость 3, прыгучесть 2
        new Level(10, 6, 2, 4, 3, "Level 5")   // +6 урона, сила 2, скорость 3, прыгучесть 3
    };

    public static void start(Player player) {
        activePlayers.add(player.getUUID());
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BreakingBodyLimit) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                if (!stack.hasTag() || !stack.getTag().contains(LEVEL_TAG)) {
                    stack.getOrCreateTag().putInt(LEVEL_TAG, 0);
                }
                applyModifiers(player, getCurrentLevel(stack));
                break;
            }
        }
    }

    public static void stop(Player player) {
        if (!(player instanceof ServerPlayer)) return;
        activePlayers.remove(player.getUUID());
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BreakingBodyLimit) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            }
        }
        removeModifiers(player);
    }

    public static void switchLevel(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BreakingBodyLimit && stack.hasTag()) {
                int currentLevel = stack.getTag().getInt(LEVEL_TAG);
                int newLevel = (currentLevel + 1) % LEVELS.length;
                stack.getTag().putInt(LEVEL_TAG, newLevel);
                if (activePlayers.contains(player.getUUID())) {
                    removeModifiers(player);
                    applyModifiers(player, LEVELS[newLevel]);
                }
                if (player instanceof ServerPlayer) {
                    player.sendSystemMessage(Component.literal("Switched to " + LEVELS[newLevel].name)
                            .withStyle(ChatFormatting.AQUA));
                }
                break;
            }
        }
    }

    private static void applyModifiers(Player player, Level level) {
        AttributeModifier damageModifier = new AttributeModifier(
            DAMAGE_MODIFIER_UUID,
            "breaking_body_limit_damage_bonus",
            level.damageBonus,
            AttributeModifier.Operation.ADDITION
        );
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(damageModifier);
    }

    private static void removeModifiers(Player player) {
        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(DAMAGE_MODIFIER_UUID);
    }

    public static Level getCurrentLevel(ItemStack stack) {
        int level = stack.hasTag() ? stack.getTag().getInt(LEVEL_TAG) : 0;
        return LEVELS[Math.max(0, Math.min(level, LEVELS.length - 1))];
    }

    public static Level getCurrentLevel(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BreakingBodyLimit &&
                stack.hasTag() &&
                stack.getTag().getBoolean(ACTIVE_TAG)) {
                return getCurrentLevel(stack);
            }
        }
        return LEVELS[0];
    }

    public static boolean isActive(Player player) {
        return activePlayers.contains(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (player.tickCount % HP_CHECK_INTERVAL != 0) return;

        boolean hasActiveItem = false;
        ItemStack activeItem = null;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BreakingBodyLimit) {
                if (stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG)) {
                    hasActiveItem = true;
                    activeItem = stack;
                } else if (activePlayers.contains(player.getUUID())) {
                    stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                }
            }
        }
        if (!hasActiveItem && activePlayers.contains(player.getUUID())) {
            stop(player);
            return;
        }
        if (activePlayers.contains(player.getUUID()) && activeItem != null) {
            Level currentLevel = getCurrentLevel(activeItem);
            if (player.level() instanceof ServerLevel serverLevel) {
                player.hurt(ModDamageTypes.breakingBodyLimit(serverLevel), currentLevel.hpCost);
            } else {
                player.hurt(player.level().damageSources().generic(), currentLevel.hpCost);
            }
            // Эффекты силы, скорости, прыгучести
            if (currentLevel.strengthAmplifier > 0)
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, currentLevel.strengthAmplifier - 1, false, false));
            if (currentLevel.speedAmplifier > 0)
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, currentLevel.speedAmplifier - 1, false, false));
            if (currentLevel.jumpAmplifier > 0)
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, currentLevel.jumpAmplifier - 1, false, false));
            // Партиклы начиная с 3 уровня
            if (activeItem.getTag().getInt(LEVEL_TAG) >= 2 && player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                for (int i = 0; i < 6; i++) {
                    double x = player.getX() + (player.level().getRandom().nextDouble() - 0.5) * 0.8;
                    double y = player.getY() + 1.0 + (player.level().getRandom().nextDouble() - 0.2) * 1.2; // от плеч до над головой
                    double z = player.getZ() + (player.level().getRandom().nextDouble() - 0.5) * 0.8;
                    double dx = (player.level().getRandom().nextDouble() - 0.5) * 0.2;
                    double dy = player.level().getRandom().nextDouble() * 0.2 + 0.05;
                    double dz = (player.level().getRandom().nextDouble() - 0.5) * 0.2;
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 1, dx, dy, dz, 0.05);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof BreakingBodyLimit) {
                    if (stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG) && !activePlayers.contains(player.getUUID())) {
                        stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack pickedStack = event.getItem().getItem();
        if (pickedStack.getItem() instanceof BreakingBodyLimit) {
            if (!activePlayers.contains(player.getUUID()) &&
                pickedStack.hasTag() &&
                pickedStack.getTag().getBoolean(ACTIVE_TAG)) {
                pickedStack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            }
        }
    }
} 