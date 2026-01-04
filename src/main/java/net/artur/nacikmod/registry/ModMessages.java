package net.artur.nacikmod.registry;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.network.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Map;
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
        INSTANCE.registerMessage(packetId++, CooldownSyncPacket.class, CooldownSyncPacket::toBytes, CooldownSyncPacket::new, CooldownSyncPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(packetId++, TimeStopPacket.class, TimeStopPacket::toBytes, TimeStopPacket::new, TimeStopPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetId++, ManaSyncPacket.class, ManaSyncPacket::toBytes, ManaSyncPacket::new, ManaSyncPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(packetId++, TrueMageStatusPacket.class, TrueMageStatusPacket::toBytes, TrueMageStatusPacket::new, TrueMageStatusPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(packetId++, PacketSyncEffect.class, PacketSyncEffect::toBytes, PacketSyncEffect::new, PacketSyncEffect::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(packetId++, AbilityStatePacket.class, AbilityStatePacket::encode, AbilityStatePacket::decode, AbilityStatePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(packetId++, CustomMoonPacket.class, CustomMoonPacket::encode, CustomMoonPacket::decode, CustomMoonPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(packetId++, EnchantmentSelectionPacket.class, EnchantmentSelectionPacket::write, EnchantmentSelectionPacket::new, EnchantmentSelectionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetId++, KodaiTogglePacket.class, KodaiTogglePacket::toBytes, KodaiTogglePacket::new, KodaiTogglePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetId++, VisionBlessingStatusPacket.class, VisionBlessingStatusPacket::toBytes, VisionBlessingStatusPacket::new, VisionBlessingStatusPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        // Пакет для синхронизации кэша маны всех сущностей (для рендера)
        INSTANCE.registerMessage(packetId++, PlayerManaSyncPacket.class, PlayerManaSyncPacket::toBytes, PlayerManaSyncPacket::new, PlayerManaSyncPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    /**
     * Отправляет данные о мане ЛЮБОЙ сущности всем игрокам поблизости.
     * Именно этот метод наполняет PlayerManaCache на стороне клиента.
     */
    public static void sendEntityManaToNearbyPlayers(LivingEntity entity, int mana, int maxMana) {
        if (entity == null || entity.level().isClientSide) return;

        // Создаем пакет синхронизации кэша
        PlayerManaSyncPacket packet = new PlayerManaSyncPacket(entity.getUUID(), mana, maxMana);

        // Отправляем всем, кто "видит" сущность (включая самого себя, если сущность - игрок)
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
    }

    /**
     * Личная синхронизация маны игрока (для его собственного GUI).
     */
    public static void sendManaToClient(ServerPlayer player, int mana, int maxMana) {
        if (player == null) return;
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ManaSyncPacket(mana, maxMana, false));

        // Дополнительно обновляем кэш самого игрока, чтобы он видел свою ману в Wallhack
        sendEntityManaToNearbyPlayers(player, mana, maxMana);
    }

    // --- Остальные методы синхронизации ---

    public static void sendToClient(ServerPlayer player, CooldownSyncPacket packet) {
        if (player == null) return;
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendTrueMageStatusToClient(ServerPlayer player, boolean isTrueMage) {
        if (player == null) return;
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new TrueMageStatusPacket(isTrueMage));
    }

    public static void sendVisionBlessingStatusToClient(ServerPlayer player, boolean hasVisionBlessing) {
        if (player == null) return;
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new VisionBlessingStatusPacket(hasVisionBlessing));
    }

    public static void sendAbilityStateToNearbyPlayers(ServerPlayer sourcePlayer, Map<String, Boolean> abilityStates, Map<String, Integer> abilityLevels) {
        if (sourcePlayer == null || sourcePlayer.level() == null) return;
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sourcePlayer), new AbilityStatePacket(abilityStates, abilityLevels, sourcePlayer.getId()));
    }

    public static void sendCustomMoonToAll() {
        INSTANCE.send(PacketDistributor.ALL.noArg(), new CustomMoonPacket());
    }

    // Добавь это в ModMessages.java
    public static void sendPlayerManaToNearbyPlayers(ServerPlayer sourcePlayer, int mana, int maxMana) {
        sendEntityManaToNearbyPlayers(sourcePlayer, mana, maxMana);
    }

    public static void sendToServer(TimeStopPacket packet) { INSTANCE.sendToServer(packet); }
    public static void sendToServer(KodaiTogglePacket packet) { INSTANCE.sendToServer(packet); }
}