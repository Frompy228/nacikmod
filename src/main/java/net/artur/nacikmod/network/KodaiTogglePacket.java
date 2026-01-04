package net.artur.nacikmod.network;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.item.ability.VisionBlessingAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class KodaiTogglePacket {
    public KodaiTogglePacket() {}

    public KodaiTogglePacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // Проверяем, есть ли у игрока Vision Blessing статус
                player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                    if (mana.hasVisionBlessing()) {
                        // Переключаем состояние Кодайгана (только один раз)
                        boolean isActive = VisionBlessingAbility.isKodaiActive(player);
                        if (isActive) {
                            VisionBlessingAbility.stopKodai(player);
                        } else {
                            VisionBlessingAbility.startKodai(player);
                        }
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}

