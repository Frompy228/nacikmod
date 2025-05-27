package net.artur.nacikmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.artur.nacikmod.item.ability.ManaRelease;
import net.artur.nacikmod.item.ability.ManaLastMagic;

import java.util.function.Supplier;

public class AbilityStatePacket {
    private final boolean isReleaseActive;
    private final boolean isLastMagicActive;
    private final int releaseLevel;

    public AbilityStatePacket(boolean isReleaseActive, boolean isLastMagicActive, int releaseLevel) {
        this.isReleaseActive = isReleaseActive;
        this.isLastMagicActive = isLastMagicActive;
        this.releaseLevel = releaseLevel;
    }

    public static void encode(AbilityStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.isReleaseActive);
        buffer.writeBoolean(packet.isLastMagicActive);
        buffer.writeInt(packet.releaseLevel);
    }

    public static AbilityStatePacket decode(FriendlyByteBuf buffer) {
        return new AbilityStatePacket(
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readInt()
        );
    }

    public static void handle(AbilityStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Запускаем обработку только на клиенте
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet));
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(AbilityStatePacket packet) {
        if (Minecraft.getInstance().player == null) return;

        // Обновляем состояние Release
        if (packet.isReleaseActive) {
            ManaRelease.activeReleasePlayers.add(Minecraft.getInstance().player.getUUID());
        } else {
            ManaRelease.activeReleasePlayers.remove(Minecraft.getInstance().player.getUUID());
        }

        // Обновляем состояние LastMagic
        if (packet.isLastMagicActive) {
            ManaLastMagic.activeLastMagicPlayers.add(Minecraft.getInstance().player.getUUID());
        } else {
            ManaLastMagic.activeLastMagicPlayers.remove(Minecraft.getInstance().player.getUUID());
        }
    }
} 