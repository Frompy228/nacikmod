package net.artur.nacikmod.network;

import net.artur.nacikmod.capability.mana.ManaCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ManaSyncPacket {
    private final UUID playerUUID;
    private final int currentMana;
    private final int maxMana;

    // Конструктор для создания пакета
    public ManaSyncPacket(UUID playerUUID, int currentMana, int maxMana) {
        this.playerUUID = playerUUID;
        this.currentMana = currentMana;
        this.maxMana = maxMana;
    }

    // Метод для кодирования пакета в байты
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeInt(currentMana);
        buffer.writeInt(maxMana);
    }

    // Метод для декодирования пакета из байтов
    public ManaSyncPacket(FriendlyByteBuf buffer) {
        this.playerUUID = buffer.readUUID();
        this.currentMana = buffer.readInt();
        this.maxMana = buffer.readInt();
    }

    // Метод для обработки пакета на стороне клиента
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Получаем клиентского игрока
            if (Minecraft.getInstance().level != null) {
                Player player = Minecraft.getInstance().level.getPlayerByUUID(playerUUID);
                if (player != null) {
                    // Обновляем данные маны на клиенте
                    player.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(mana -> {
                        mana.setMana(currentMana);
                        mana.setMaxMana(maxMana);
                    });
                }
            }
        });
        context.setPacketHandled(true);
    }
}