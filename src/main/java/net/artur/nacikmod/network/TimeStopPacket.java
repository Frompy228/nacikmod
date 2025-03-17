package net.artur.nacikmod.network;

import net.artur.nacikmod.item.RingOfTime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static net.artur.nacikmod.item.ability.TimeStop.activate;

public class TimeStopPacket {
    public TimeStopPacket() {}

    public TimeStopPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // Проверяем, есть ли у игрока перезарядка
                if (RingOfTime.getCooldown(player) == 0) {
                    // Запускаем способность
                    activate(player);

                    // Устанавливаем перезарядку
                    int cooldown = 200;
                    RingOfTime.setCooldown(player, cooldown);

                    // Синхронизируем перезарядку с клиентом
                    ModMessages.sendToClient(player, cooldown);
                }
            }
        });
        context.setPacketHandled(true);
    }


}
