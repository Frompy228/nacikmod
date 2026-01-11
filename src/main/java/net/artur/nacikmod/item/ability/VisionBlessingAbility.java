package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.network.AbilityStateManager;
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

        if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.hasVisionBlessing()).orElse(false)) {
            return;
        }

        if (activeKodaiPlayers.contains(player.getUUID())) return;

        activeKodaiPlayers.add(player.getUUID());

        // Атрибуты
        player.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(
                new AttributeModifier(HEALTH_MODIFIER_UUID, "kodai_health_bonus", 2.0, AttributeModifier.Operation.ADDITION));
        player.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(
                new AttributeModifier(ATTACK_SPEED_MODIFIER_UUID, "kodai_attack_speed_bonus", 0.05, AttributeModifier.Operation.ADDITION));
        player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
                new AttributeModifier(MOVEMENT_SPEED_MODIFIER_UUID, "kodai_movement_speed_bonus", 0.02, AttributeModifier.Operation.ADDITION));
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
                new AttributeModifier(DAMAGE_MODIFIER_UUID, "kodai_damage_bonus", 1.0, AttributeModifier.Operation.ADDITION));

        // Сообщение
        player.sendSystemMessage(Component.literal("Kodaigan activated")
                .withStyle(style -> style.withColor(0x280800)));

        if (player instanceof ServerPlayer serverPlayer) {
            AbilityStateManager.syncAbilityState(serverPlayer, "kodai", true);
        }
    }

    public static void stopKodai(Player player) {
        if (!(player instanceof ServerPlayer)) return;
        if (!activeKodaiPlayers.contains(player.getUUID())) return;

        activeKodaiPlayers.remove(player.getUUID());

        removeModifiers(player);

        // Снимаем ночное зрение
        player.removeEffect(MobEffects.NIGHT_VISION);

        player.sendSystemMessage(Component.literal("Kodaigan deactivated")
                .withStyle(ChatFormatting.GRAY));

        if (player instanceof ServerPlayer serverPlayer) {
            AbilityStateManager.syncAbilityState(serverPlayer, "kodai", false);
        }
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

    public static boolean isKodaiActive(Player player) {
        return activeKodaiPlayers.contains(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.phase != TickEvent.Phase.START) return;

        if (activeKodaiPlayers.contains(player.getUUID())) {
            boolean hasVisionBlessing = player.getCapability(ManaProvider.MANA_CAPABILITY)
                    .map(mana -> mana.hasVisionBlessing())
                    .orElse(false);

            if (!hasVisionBlessing) {
                stopKodai(player);
                return;
            }

            // Обновляем ночное зрение каждые 20 тиков (1 сек)
            if (player.tickCount % 55 == 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.NIGHT_VISION,
                        500, // 25 секунд
                        0,
                        false, // без частиц (ambient)
                        false, // без иконки
                        false  // невидимый эффект в GUI
                ));
            }

            // Частицы генерируются на клиенте через KodaiganParticleHandler
        }
    }
}
