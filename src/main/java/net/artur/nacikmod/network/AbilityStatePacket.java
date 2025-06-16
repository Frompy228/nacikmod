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

import java.util.function.Supplier;

public class AbilityStatePacket {
    private final boolean isReleaseActive;
    private final boolean isLastMagicActive;
    private final int releaseLevel;
    private final int playerId; // ID игрока, чье состояние мы синхронизируем

    public AbilityStatePacket(boolean isReleaseActive, boolean isLastMagicActive, int releaseLevel, int playerId) {
        this.isReleaseActive = isReleaseActive;
        this.isLastMagicActive = isLastMagicActive;
        this.releaseLevel = releaseLevel;
        this.playerId = playerId;
    }

    public static void encode(AbilityStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.isReleaseActive);
        buffer.writeBoolean(packet.isLastMagicActive);
        buffer.writeInt(packet.releaseLevel);
        buffer.writeInt(packet.playerId);
    }

    public static AbilityStatePacket decode(FriendlyByteBuf buffer) {
        return new AbilityStatePacket(
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readInt(),
            buffer.readInt()
        );
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

        // Обновляем состояние для конкретного игрока
        if (packet.isReleaseActive) {
            if (!ManaRelease.activeReleasePlayers.contains(targetPlayer.getUUID())) {
                ManaRelease.activeReleasePlayers.add(targetPlayer.getUUID());
            }
        } else {
            ManaRelease.activeReleasePlayers.remove(targetPlayer.getUUID());
        }

        if (packet.isLastMagicActive) {
            if (!ManaLastMagic.activeLastMagicPlayers.contains(targetPlayer.getUUID())) {
                ManaLastMagic.activeLastMagicPlayers.add(targetPlayer.getUUID());
            }
        } else {
            ManaLastMagic.activeLastMagicPlayers.remove(targetPlayer.getUUID());
        }
    }
} 