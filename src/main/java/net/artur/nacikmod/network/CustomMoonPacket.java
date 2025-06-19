package net.artur.nacikmod.network;

import net.artur.nacikmod.client.MoonTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CustomMoonPacket {
    public CustomMoonPacket() {}
    public static CustomMoonPacket decode(FriendlyByteBuf buf) { return new CustomMoonPacket(); }
    public void encode(FriendlyByteBuf buf) {}

    public static void handle(CustomMoonPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                handleClient();
            }
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            mc.tell(MoonTextureManager::activateMagicNight);
            return;
        }
        MoonTextureManager.activateMagicNight();
    }
}
