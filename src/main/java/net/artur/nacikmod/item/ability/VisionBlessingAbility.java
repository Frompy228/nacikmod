package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.network.AbilityStateManager;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class VisionBlessingAbility {
    public static final Set<UUID> activeKodaiPlayers = new HashSet<>();
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("464326ce-9581-4d13-8910-883042df6504");
    private static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("464326ce-9581-4d13-8910-883042df6505");
    private static final UUID MOVEMENT_SPEED_MODIFIER_UUID = UUID.fromString("464326ce-9581-4d13-8910-883042df6506");
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("464326ce-9581-4d13-8910-883042df6507");

    public static void startKodai(Player player) {
        if (!(player instanceof ServerPlayer)) return;
        
        // Проверяем, есть ли у игрока Vision Blessing статус
        if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.hasVisionBlessing()).orElse(false)) {
            return;
        }
        
        // Проверяем, не активен ли уже Кодайган
        if (activeKodaiPlayers.contains(player.getUUID())) {
            return; // Уже активен, не делаем ничего
        }
        
        // Добавляем игрока в список активных
        activeKodaiPlayers.add(player.getUUID());
        
        // Добавляем модификаторы атрибутов
        // +2 к здоровью
        AttributeModifier healthModifier = new AttributeModifier(
            HEALTH_MODIFIER_UUID,
            "kodai_health_bonus",
            2.0,
            AttributeModifier.Operation.ADDITION
        );
        player.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(healthModifier);
        
        // +0.05 к скорости атаки
        AttributeModifier attackSpeedModifier = new AttributeModifier(
            ATTACK_SPEED_MODIFIER_UUID,
            "kodai_attack_speed_bonus",
            0.05,
            AttributeModifier.Operation.ADDITION
        );
        player.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(attackSpeedModifier);
        
        // +0.2 к скорости передвижения
        AttributeModifier movementSpeedModifier = new AttributeModifier(
            MOVEMENT_SPEED_MODIFIER_UUID,
            "kodai_movement_speed_bonus",
            0.02,
            AttributeModifier.Operation.ADDITION
        );
        player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(movementSpeedModifier);
        
        // +1 к урону
        AttributeModifier damageModifier = new AttributeModifier(
            DAMAGE_MODIFIER_UUID,
            "kodai_damage_bonus",
            1.0,
            AttributeModifier.Operation.ADDITION
        );
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(damageModifier);
        
        // Отправляем сообщение в чат
        player.sendSystemMessage(Component.literal("Kodaigan activated")
                .withStyle(style -> style.withColor(0x280800)));
        
        // Отправляем пакет всем игрокам поблизости
        if (player instanceof ServerPlayer serverPlayer) {
            AbilityStateManager.syncAbilityState(serverPlayer, "kodai", true);
        }
    }

    public static void stopKodai(Player player) {
        if (!(player instanceof ServerPlayer)) return;

        // Проверяем, активен ли Кодайган
        if (!activeKodaiPlayers.contains(player.getUUID())) {
            return; // Не активен, не делаем ничего
        }

        // Удаляем игрока из списка активных
        activeKodaiPlayers.remove(player.getUUID());

        // Удаляем модификаторы
        removeModifiers(player);
        
        // Отправляем сообщение в чат
        player.sendSystemMessage(Component.literal("Kodaigan deactivated")
                .withStyle(ChatFormatting.GRAY));
        
        // Отправляем пакет всем игрокам поблизости
        if (player instanceof ServerPlayer serverPlayer) {
            AbilityStateManager.syncAbilityState(serverPlayer, "kodai", false);
        }
    }

    private static void removeModifiers(Player player) {
        player.getAttribute(Attributes.MAX_HEALTH).removeModifier(HEALTH_MODIFIER_UUID);
        player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ATTACK_SPEED_MODIFIER_UUID);
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(MOVEMENT_SPEED_MODIFIER_UUID);
        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(DAMAGE_MODIFIER_UUID);
        
        // Корректируем текущее здоровье, если оно превышает новый максимум
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    public static boolean isKodaiActive(Player player) {
        return activeKodaiPlayers.contains(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.phase != TickEvent.Phase.START) return;

        // Проверяем, активен ли Кодайган
        if (activeKodaiPlayers.contains(player.getUUID())) {
            // Проверяем, есть ли у игрока Vision Blessing статус
            boolean hasVisionBlessing = player.getCapability(ManaProvider.MANA_CAPABILITY)
                .map(mana -> mana.hasVisionBlessing())
                .orElse(false);
            
            if (!hasVisionBlessing) {
                // Если статус потерян, деактивируем Кодайган
                stopKodai(player);
                return;
            }
            
            // Частицы генерируются на клиенте через KodaiganParticleHandler
            // Серверная часть только отслеживает активность
        }
    }
}

