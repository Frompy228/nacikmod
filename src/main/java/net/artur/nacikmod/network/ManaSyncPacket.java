package net.artur.nacikmod.network;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ManaSyncPacket  {
    private final int mana;
    private final int maxMana;
    private final boolean isTrueMage;

    public ManaSyncPacket(int mana, int maxMana, boolean isTrueMage) {
        this.mana = mana;
        this.maxMana = maxMana;
        this.isTrueMage = isTrueMage;
    }

    public ManaSyncPacket(FriendlyByteBuf buf) {
        this.mana = buf.readInt();
        this.maxMana = buf.readInt();
        this.isTrueMage = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(mana);
        buf.writeInt(maxMana);
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
                mana.setMana(this.mana);
                mana.setMaxMana(this.maxMana);
            });
        }
    }
}