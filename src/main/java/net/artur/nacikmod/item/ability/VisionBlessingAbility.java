package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModMessages;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class VisionBlessingAbility {
    // УДАЛЯЕМ: public static final Set<UUID> activeKodaiPlayers = new HashSet<>();

    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("464326ce-9581-4d13-8910-883042df6504");
    private static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("464326ce-9581-4d13-8910-883042df6505");
    private static final UUID MOVEMENT_SPEED_MODIFIER_UUID = UUID.fromString("464326ce-9581-4d13-8910-883042df6506");
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("464326ce-9581-4d13-8910-883042df6507");

    public static void startKodai(Player player) {
        if (!(player instanceof ServerPlayer)) return;

        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            // Проверяем благословение
            if (!mana.hasVisionBlessing()) {
                return;
            }

            // Проверяем, не активировано ли уже
            if (mana.isKodaiActive()) {
                return;
            }

            // Устанавливаем состояние в капабилити
            mana.setKodaiActive(true);

            // Применяем атрибуты
            applyModifiers(player);

            // Сообщение
            player.sendSystemMessage(Component.literal("Kodaigan activated")
                    .withStyle(style -> style.withColor(0x280800)));

            // Синхронизация происходит автоматически через капабилити
            // Но если нужно принудительно:
            syncManaToClient((ServerPlayer) player);
        });
    }

    public static void stopKodai(Player player) {
        if (!(player instanceof ServerPlayer)) return;

        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            // Проверяем, активировано ли
            if (!mana.isKodaiActive()) {
                return;
            }

            // Снимаем состояние
            mana.setKodaiActive(false);

            // Убираем атрибуты
            removeModifiers(player);

            // Снимаем ночное зрение
            player.removeEffect(MobEffects.NIGHT_VISION);

            // Сообщение
            player.sendSystemMessage(Component.literal("Kodaigan deactivated")
                    .withStyle(ChatFormatting.GRAY));

            // Синхронизация
            syncManaToClient((ServerPlayer) player);
        });
    }

    public static void toggleKodai(Player player) {
        if (!(player instanceof ServerPlayer)) return;

        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            if (!mana.hasVisionBlessing()) {
                return;
            }

            if (mana.isKodaiActive()) {
                stopKodai(player);
            } else {
                startKodai(player);
            }
        });
    }

    private static void applyModifiers(Player player) {
        player.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(
                new AttributeModifier(HEALTH_MODIFIER_UUID, "kodai_health_bonus", 2.0, AttributeModifier.Operation.ADDITION));
        player.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(
                new AttributeModifier(ATTACK_SPEED_MODIFIER_UUID, "kodai_attack_speed_bonus", 0.05, AttributeModifier.Operation.ADDITION));
        player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
                new AttributeModifier(MOVEMENT_SPEED_MODIFIER_UUID, "kodai_movement_speed_bonus", 0.02, AttributeModifier.Operation.ADDITION));
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
                new AttributeModifier(DAMAGE_MODIFIER_UUID, "kodai_damage_bonus", 1.0, AttributeModifier.Operation.ADDITION));
    }

    private static void removeModifiers(Player player) {
        player.getAttribute(Attributes.MAX_HEALTH).removeModifier(HEALTH_MODIFIER_UUID);
        player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ATTACK_SPEED_MODIFIER_UUID);
        player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(MOVEMENT_SPEED_MODIFIER_UUID);
        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(DAMAGE_MODIFIER_UUID);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    // Теперь проверяем через капабилити
    public static boolean isKodaiActive(Player player) {
        return player.getCapability(ManaProvider.MANA_CAPABILITY)
                .map(mana -> mana.isKodaiActive() && mana.hasVisionBlessing())
                .orElse(false);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.phase != TickEvent.Phase.START) return;

        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            // Если Kodai активен
            if (mana.isKodaiActive()) {
                // Проверяем, есть ли еще благословение
                if (!mana.hasVisionBlessing()) {
                    stopKodai(player);
                    return;
                }

                // Обновляем ночное зрение каждые 55 тиков
                if (player.tickCount % 55 == 0) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.NIGHT_VISION,
                            500, // 25 секунд
                            0,
                            false,
                            false,
                            false
                    ));
                }
            }
        });
    }

    // Вспомогательный метод для синхронизации
    // ... остальной код класса ...

    private static void syncManaToClient(ServerPlayer player) {
        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            // 1. Собираем текущие состояния
            Map<String, Boolean> states = new HashMap<>();

            // Важно: добавляем KODAI, так как мы сейчас работаем с ним
            states.put("kodai", mana.isKodaiActive());

            // Если нужно синхронизировать сразу всё (чтобы не было рассинхрона),
            // можно добавить и остальные флаги, если они есть в капабилити:
            // states.put("vision_blessing", mana.hasVisionBlessing());

            // 2. Собираем уровни (если нужно, пока пустая карта)
            Map<String, Integer> levels = new HashMap<>();
            // levels.put("release", mana.getReleaseLevel());

            // 3. Отправляем через твой готовый метод в ModMessages
            ModMessages.sendAbilityStateToNearbyPlayers(player, states, levels);
        });
    }
}