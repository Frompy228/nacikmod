package net.artur.nacikmod.network;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.item.RingOfTime;
import net.artur.nacikmod.registry.ModMessages;
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
                if (RingOfTime.getCooldown(player) == 0) {
                    player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                        if (mana.getMana() >= 500) {
                            mana.setMana(mana.getMana() - 500);
                            ModMessages.sendManaToClient(player, mana.getMana(), mana.getMaxMana());

                            activate(player);

                            int cooldown = 200;
                            RingOfTime.setCooldown(player, cooldown);
                            // Исправленный вызов:
                            ModMessages.sendToClient(player, new CooldownSyncPacket(player.getUUID(), cooldown));
                        }
                    });
                }
            }
        });
        context.setPacketHandled(true);
    }
}