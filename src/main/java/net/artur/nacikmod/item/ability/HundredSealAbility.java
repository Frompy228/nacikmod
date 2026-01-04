package net.artur.nacikmod.item.ability;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.HundredSeal;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.network.AbilityStateManager;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class HundredSealAbility {
    public static final Set<UUID> activeHundredSealPlayers = new HashSet<>();
    private static final String ACTIVE_TAG = "active";
    private static final String STORED_MANA_TAG = "stored_mana";
    private static final int MAX_STORAGE = 100000;
    private static final int MANA_COST_PER_SECOND = 250;
    private static final int HEALING_INTERVAL = 90;
    private static final int MANA_CHECK_INTERVAL = 20; // 1 секунда
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("464326ce-9581-4d13-8910-883042df6502");
    private static final UUID MAGIC_ARMOR_MODIFIER_UUID = UUID.fromString("464326ce-9581-4d13-8910-883042df6503");

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Проверяем все предметы HundredSeal в инвентаре
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof HundredSeal) {
                    // Если предмет помечен как активный, но игрок не в списке активных
                    if (stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG) && !activeHundredSealPlayers.contains(player.getUUID())) {
                        stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                    }
                }
            }
        }
    }

    public static void startHundredSeal(Player player) {
        // Сначала добавляем игрока в список активных
        activeHundredSealPlayers.add(player.getUUID());
        
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof HundredSeal) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                
                // Добавляем +20 к максимальному здоровью через атрибут
                net.minecraft.world.entity.ai.attributes.AttributeModifier healthModifier = 
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        HEALTH_MODIFIER_UUID,
                        "hundred_seal_health_bonus",
                        20.0,
                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION
                    );
                player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                    .addTransientModifier(healthModifier);
                
                // Добавляем +2 magic armor через кастомный атрибут
                net.minecraft.world.entity.ai.attributes.AttributeModifier magicArmorModifier = 
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        MAGIC_ARMOR_MODIFIER_UUID,
                        "hundred_seal_magic_armor_bonus",
                        2.0,
                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION
                    );
                player.getAttribute(ModAttributes.BONUS_ARMOR.get())
                    .addTransientModifier(magicArmorModifier);
                
                // Отправляем пакет всем игрокам поблизости (включая владельца)
                if (player instanceof ServerPlayer serverPlayer) {
                    AbilityStateManager.syncAbilityState(serverPlayer, "hundred_seal", true);
                }

                break;
            }
        }
    }

    public static void stopHundredSeal(Player player) {
        if (!(player instanceof ServerPlayer)) return;

        // Удаляем игрока из списка активных
        activeHundredSealPlayers.remove(player.getUUID());

        // Обновляем состояние всех предметов HundredSeal в инвентаре
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof HundredSeal) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            }
        }

        // Удаляем эффекты и модификаторы
        removeModifiers(player);
        
        // Отправляем пакет всем игрокам поблизости (включая владельца)
        if (player instanceof ServerPlayer serverPlayer) {
            AbilityStateManager.syncAbilityState(serverPlayer, "hundred_seal", false);
        }


    }

    private static void removeModifiers(Player player) {
        // Удаляем эффект регенерации
        player.removeEffect(net.minecraft.world.effect.MobEffects.REGENERATION);
        
        // Удаляем модификатор здоровья
        player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
            .removeModifier(HEALTH_MODIFIER_UUID);
        
        // Удаляем модификатор magic armor
        player.getAttribute(ModAttributes.BONUS_ARMOR.get())
            .removeModifier(MAGIC_ARMOR_MODIFIER_UUID);
        
        // Корректируем текущее здоровье, если оно превышает новый максимум
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    public static boolean isHundredSealActive(Player player) {
        return activeHundredSealPlayers.contains(player.getUUID());
    }

    public static int getStoredMana(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(STORED_MANA_TAG)) {
            return stack.getTag().getInt(STORED_MANA_TAG);
        }
        return 0;
    }

    public static void setStoredMana(ItemStack stack, int amount) {
        stack.getOrCreateTag().putInt(STORED_MANA_TAG, Math.min(amount, MAX_STORAGE));
    }

    public static void addManaToItem(ItemStack stack, int amount) {
        int currentMana = getStoredMana(stack);
        setStoredMana(stack, currentMana + amount);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.phase != TickEvent.Phase.END) return;

        // Проверяем все предметы HundredSeal в инвентаре
        boolean hasActiveItem = false;
        ItemStack hundredSealItem = null;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof HundredSeal) {
                if (stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG)) {
                    hasActiveItem = true;
                    hundredSealItem = stack;
                } else if (activeHundredSealPlayers.contains(player.getUUID())) {
                    // Если предмет помечен как неактивный, но игрок в списке активных
                    stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                }
            }
        }

        // Если нет активного предмета, но игрок в списке активных
        if (!hasActiveItem && activeHundredSealPlayers.contains(player.getUUID())) {
            stopHundredSeal(player);
            return;
        }

        // Если эффект активен и предмет есть в инвентаре
        if (activeHundredSealPlayers.contains(player.getUUID()) && hundredSealItem != null) {
            // Применяем эффект регенерации только если его нет или осталось мало времени
            // Это позволяет эффекту успевать тикать (применять исцеление)
            MobEffectInstance existingEffect = player.getEffect(MobEffects.REGENERATION);
            if (existingEffect == null || existingEffect.getDuration() < 10) {
                // Применяем эффект только если его нет или осталось меньше 2 секунд
            player.addEffect(new MobEffectInstance(
                    MobEffects.REGENERATION,
                        100,  // Длительность 5 секунд (100 тиков) - достаточно для нескольких тиков
                    4,   // Уровень 5 (Regeneration V)
                    false,
                    false,
                    true
            ));
            }

            // Тратим ману каждую секунду
            if (player.tickCount % MANA_CHECK_INTERVAL == 0) {
                int storedMana = getStoredMana(hundredSealItem);
                if (storedMana >= MANA_COST_PER_SECOND) {
                    setStoredMana(hundredSealItem, storedMana - MANA_COST_PER_SECOND);
                } else {
                    stopHundredSeal(player);
                    player.sendSystemMessage(Component.literal("Hundred Seal deactivated - not enough mana")
                            .withStyle(ChatFormatting.RED));
                    return;
                }
            }

            // Мгновенное исцеление каждые 5 секунд
            if (player.tickCount % HEALING_INTERVAL == 0) {
                player.heal(8.0f); // Исцеляем на 4 сердца (8 HP)
            }
        }
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        ItemStack pickedStack = event.getItem().getItem();
        if (pickedStack.getItem() instanceof HundredSeal) {
            // Если игрок не в списке активных, но предмет помечен как активный
            if (!activeHundredSealPlayers.contains(player.getUUID()) && 
                pickedStack.hasTag() && 
                pickedStack.getTag().getBoolean(ACTIVE_TAG)) {
                pickedStack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            }
        }
    }
} 