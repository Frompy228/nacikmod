package net.artur.nacikmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.artur.nacikmod.item.ability.ManaRelease;
import net.artur.nacikmod.item.ability.ManaLastMagic;
import net.artur.nacikmod.item.ability.HundredSealAbility;
import net.artur.nacikmod.item.ability.SimpleDomainAbility;
import net.artur.nacikmod.item.ability.DomainAbility;
import net.artur.nacikmod.item.ability.VisionBlessingAbility;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AbilityStatePacket {
    private final Map<String, Boolean> abilityStates;
    private final Map<String, Integer> abilityLevels;
    private final int playerId; // ID игрока, чье состояние мы синхронизируем

    public AbilityStatePacket(Map<String, Boolean> abilityStates, Map<String, Integer> abilityLevels, int playerId) {
        this.abilityStates = new HashMap<>(abilityStates);
        this.abilityLevels = new HashMap<>(abilityLevels);
        this.playerId = playerId;
    }

    public static void encode(AbilityStatePacket packet, FriendlyByteBuf buffer) {
        // Записываем количество состояний
        buffer.writeInt(packet.abilityStates.size());
        for (Map.Entry<String, Boolean> entry : packet.abilityStates.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeBoolean(entry.getValue());
        }
        
        // Записываем количество уровней
        buffer.writeInt(packet.abilityLevels.size());
        for (Map.Entry<String, Integer> entry : packet.abilityLevels.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeInt(entry.getValue());
        }
        
        buffer.writeInt(packet.playerId);
    }

    public static AbilityStatePacket decode(FriendlyByteBuf buffer) {
        // Читаем состояния
        int statesSize = buffer.readInt();
        Map<String, Boolean> abilityStates = new HashMap<>();
        for (int i = 0; i < statesSize; i++) {
            String key = buffer.readUtf();
            boolean value = buffer.readBoolean();
            abilityStates.put(key, value);
        }
        
        // Читаем уровни
        int levelsSize = buffer.readInt();
        Map<String, Integer> abilityLevels = new HashMap<>();
        for (int i = 0; i < levelsSize; i++) {
            String key = buffer.readUtf();
            int value = buffer.readInt();
            abilityLevels.put(key, value);
        }
        
        int playerId = buffer.readInt();
        
        return new AbilityStatePacket(abilityStates, abilityLevels, playerId);
    }

    public static void handle(AbilityStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                handleClient(packet);
            }
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(AbilityStatePacket packet) {
        if (Minecraft.getInstance().level == null) return;

        // Получаем игрока по ID
        Player targetPlayer = Minecraft.getInstance().level.getEntity(packet.playerId) instanceof Player player ? player : null;
        if (targetPlayer == null) return;

        // Обновляем состояния для конкретного игрока
        updateAbilityState(targetPlayer, "release", packet.abilityStates.getOrDefault("release", false));
        updateAbilityState(targetPlayer, "last_magic", packet.abilityStates.getOrDefault("last_magic", false));
        updateAbilityState(targetPlayer, "hundred_seal", packet.abilityStates.getOrDefault("hundred_seal", false));
        updateAbilityState(targetPlayer, "simple_domain", packet.abilityStates.getOrDefault("simple_domain", false));
        updateAbilityState(targetPlayer, "domain", packet.abilityStates.getOrDefault("domain", false));
        updateAbilityState(targetPlayer, "kodai", packet.abilityStates.getOrDefault("kodai", false));
        
        // Обновляем уровни
        updateAbilityLevel(targetPlayer, "release", packet.abilityLevels.getOrDefault("release", 0));
    }
    
    private static void updateAbilityState(Player player, String ability, boolean isActive) {
        switch (ability) {
            case "release":
                if (isActive) {
                    if (!ManaRelease.activeReleasePlayers.contains(player.getUUID())) {
                        ManaRelease.activeReleasePlayers.add(player.getUUID());
                    }
                } else {
                    ManaRelease.activeReleasePlayers.remove(player.getUUID());
                }
                break;
            case "last_magic":
                if (isActive) {
                    if (!ManaLastMagic.activeLastMagicPlayers.contains(player.getUUID())) {
                        ManaLastMagic.activeLastMagicPlayers.add(player.getUUID());
                    }
                } else {
                    ManaLastMagic.activeLastMagicPlayers.remove(player.getUUID());
                }
                break;
            case "hundred_seal":
                if (isActive) {
                    if (!HundredSealAbility.activeHundredSealPlayers.contains(player.getUUID())) {
                        HundredSealAbility.activeHundredSealPlayers.add(player.getUUID());
                    }
                } else {
                    HundredSealAbility.activeHundredSealPlayers.remove(player.getUUID());
                }
                break;
            case "simple_domain":
                if (isActive) {
                    if (!SimpleDomainAbility.activeSimpleDomainPlayers.contains(player.getUUID())) {
                        SimpleDomainAbility.activeSimpleDomainPlayers.add(player.getUUID());
                    }
                } else {
                    SimpleDomainAbility.activeSimpleDomainPlayers.remove(player.getUUID());
                }
                break;
            case "domain":
                if (isActive) {
                    if (!DomainAbility.activeDomainPlayers.contains(player.getUUID())) {
                        DomainAbility.activeDomainPlayers.add(player.getUUID());
                    }
                } else {
                    DomainAbility.activeDomainPlayers.remove(player.getUUID());
                }
                break;
            case "kodai":
                if (isActive) {
                    if (!VisionBlessingAbility.activeKodaiPlayers.contains(player.getUUID())) {
                        VisionBlessingAbility.activeKodaiPlayers.add(player.getUUID());
                    }
                } else {
                    VisionBlessingAbility.activeKodaiPlayers.remove(player.getUUID());
                }
                break;
        }
    }
    
    private static void updateAbilityLevel(Player player, String ability, int level) {
        // Здесь можно добавить логику для обновления уровней способностей
        // Пока оставляем пустым, так как только Release использует уровни
    }
}