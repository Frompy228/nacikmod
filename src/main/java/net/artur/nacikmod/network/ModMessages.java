package net.artur.nacikmod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.artur.nacikmod.NacikMod;

import java.util.Optional;

public class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NacikMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        // Регистрация CooldownSyncPacket
        INSTANCE.registerMessage(packetId++,
                CooldownSyncPacket.class,
                CooldownSyncPacket::toBytes,
                CooldownSyncPacket::new,
                CooldownSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(packetId++,
                TimeStopPacket.class,
                TimeStopPacket::toBytes,
                TimeStopPacket::new,
                TimeStopPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        INSTANCE.registerMessage(packetId++,
                ManaSyncPacket.class,
                ManaSyncPacket::toBytes,
                ManaSyncPacket::new,
                ManaSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(packetId++,
                PacketSyncEffect.class,
                PacketSyncEffect::toBytes,
                PacketSyncEffect::new,
                PacketSyncEffect::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        INSTANCE.registerMessage(packetId++,
                AbilityStatePacket.class,
                AbilityStatePacket::encode,
                AbilityStatePacket::decode,
                AbilityStatePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void sendToClient(ServerPlayer player, CooldownSyncPacket packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendManaToClient(ServerPlayer player, int mana, int maxMana) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ManaSyncPacket(mana, maxMana));
    }

    public static void sendToServer(TimeStopPacket packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendAbilityStateToClient(ServerPlayer player, boolean isReleaseActive, boolean isLastMagicActive, int releaseLevel) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), 
            new AbilityStatePacket(isReleaseActive, isLastMagicActive, releaseLevel));
    }

    public static void sendAbilityStateToAllPlayers(ServerPlayer sourcePlayer, boolean isReleaseActive, boolean isLastMagicActive, int releaseLevel) {
        if (sourcePlayer.level() != null) {
            INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> sourcePlayer),
                new AbilityStatePacket(isReleaseActive, isLastMagicActive, releaseLevel));
        }
    }
}