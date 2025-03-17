package net.artur.nacikmod.network;

import net.artur.nacikmod.item.RingOfTime;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class CooldownSyncPacket {
    private final UUID playerUUID;
    private final int cooldown;

    public CooldownSyncPacket(UUID playerUUID, int cooldown) {
        this.playerUUID = playerUUID;
        this.cooldown = cooldown;
    }

    public CooldownSyncPacket(FriendlyByteBuf buf) {
        this.playerUUID = buf.readUUID();
        this.cooldown = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
        buf.writeInt(cooldown);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                Player player = Minecraft.getInstance().level.getPlayerByUUID(playerUUID);
                if (player != null) {
                    RingOfTime.setCooldown(player, cooldown);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
