package net.artur.nacikmod.capability.root;

import net.artur.nacikmod.effect.EffectRoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RootEvents {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(
                    new ResourceLocation("nacikmod", "root"),
                    new RootProvider()
            );
        }
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect() instanceof EffectRoot
                && event.getEntity() != null
                && !event.getEntity().level().isClientSide()) {

            event.getEntity().getCapability(RootProvider.ROOT_CAPABILITY).ifPresent(data -> {
                data.setPendingData(
                        event.getEntity().blockPosition(),
                        event.getEntity().level().dimension()
                );

                if (!(event.getEntity() instanceof ServerPlayer)) {
                    data.forceCommitData();
                } else {
                    if (!data.isInitialized()) {
                        data.commitData();
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() instanceof EffectRoot
                && event.getEntity() != null
                && !event.getEntity().level().isClientSide()) {

            event.getEntity().getCapability(RootProvider.ROOT_CAPABILITY).ifPresent(IRootData::clear);
        }
    }
}