package net.artur.nacikmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class EyeParticlePacket {
    private final double x;
    private final double y;
    private final double z;

    public EyeParticlePacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public EyeParticlePacket(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    public static void send(ServerPlayer player, Entity target) {
        ModMessages.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new EyeParticlePacket(
                        target.getX(),
                        target.getEyeY(),
                        target.getZ()
                )
        );
    }

    public static void handle(EyeParticlePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                handleClient(msg);
            }
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(EyeParticlePacket msg) {
        if (Minecraft.getInstance().level == null) return;
        if (!ModParticles.EYE_TRACK.isPresent()) return;

        try {
            Minecraft.getInstance().level.addParticle(
                    ModParticles.EYE_TRACK.get(),
                    msg.x, msg.y, msg.z,
                    0, 0, 0
            );
        } catch (Exception e) {
            // Игнорируем ошибки при создании частиц
        }
    }
}
