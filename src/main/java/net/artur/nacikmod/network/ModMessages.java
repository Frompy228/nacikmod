package net.artur.nacikmod.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("nacikmod", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(
                id++,
                TimeStopPacket.class,
                TimeStopPacket::toBytes,
                TimeStopPacket::new,
                TimeStopPacket::handle
        );

        INSTANCE.registerMessage(
                id++,
                CooldownSyncPacket.class,
                CooldownSyncPacket::toBytes,
                CooldownSyncPacket::new,
                CooldownSyncPacket::handle
        );

        INSTANCE.registerMessage(
                id++,
                ManaSyncPacket.class,
                ManaSyncPacket::toBytes,
                ManaSyncPacket::new,
                ManaSyncPacket::handle
        );

    }

    // Синхронизация перезарядки с клиентом
    public static void sendToClient(ServerPlayer player, int cooldown) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new CooldownSyncPacket(player.getUUID(), cooldown));
    }
    public static void sendManaToClient(ServerPlayer player, int currentMana, int maxMana) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ManaSyncPacket(player.getUUID(), currentMana, maxMana));
    }

    public static void sendToServer(TimeStopPacket packet) {
        INSTANCE.sendToServer(packet);
    }
}
