package net.artur.nacikmod.network;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TrueMageStatusPacket {
    private final boolean isTrueMage;

    public TrueMageStatusPacket(boolean isTrueMage) {
        this.isTrueMage = isTrueMage;
    }

    public TrueMageStatusPacket(FriendlyByteBuf buf) {
        this.isTrueMage = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isTrueMage);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Запуск только на клиенте
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient());
        });
        context.setPacketHandled(true);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                mana.setTrueMage(this.isTrueMage);
            });
        }
    }
}






