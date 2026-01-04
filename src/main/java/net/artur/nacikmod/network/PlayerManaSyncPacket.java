package net.artur.nacikmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;

public class PlayerManaSyncPacket {
    private final UUID targetId;
    private final int mana;
    private final int maxMana;

    public PlayerManaSyncPacket(UUID targetId, int mana, int maxMana) {
        this.targetId = targetId;
        this.mana = mana;
        this.maxMana = maxMana;
    }

    public PlayerManaSyncPacket(FriendlyByteBuf buffer) {
        this.targetId = buffer.readUUID();
        this.mana = buffer.readInt();
        this.maxMana = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.targetId);
        buffer.writeInt(this.mana);
        buffer.writeInt(this.maxMana);
    }

    public static void handle(PlayerManaSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                handleClient(packet);
            }
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PlayerManaSyncPacket packet) {
        if (packet.targetId != null) {
            // Обновляем общий кэш по UUID (и для игроков, и для боссов)
            PlayerManaCache.updatePlayerMana(packet.targetId, packet.mana, packet.maxMana);
        }
    }
}