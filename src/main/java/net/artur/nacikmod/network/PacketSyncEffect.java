package net.artur.nacikmod.network;

import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.artur.nacikmod.NacikMod;

import java.util.function.Supplier;

public class PacketSyncEffect {
    private final boolean hasEffect;
    private final int playerId;

    public PacketSyncEffect(int playerId, boolean hasEffect) {
        this.playerId = playerId;
        this.hasEffect = hasEffect;
    }

    public PacketSyncEffect(FriendlyByteBuf buf) {
        this.playerId = buf.readInt();
        this.hasEffect = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(playerId);
        buf.writeBoolean(hasEffect);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientLevel world = Minecraft.getInstance().level; // Получаем мир клиента
            if (world == null) return;

            Player player = (Player) world.getEntity(playerId);
            if (player != null) {
                if (hasEffect) {
                    player.addEffect(new MobEffectInstance(ModEffects.ROOT.get()));
                } else {
                    player.removeEffect(ModEffects.ROOT.get());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
