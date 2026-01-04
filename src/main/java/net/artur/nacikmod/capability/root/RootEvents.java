package net.artur.nacikmod.capability.root;

import net.artur.nacikmod.effect.EffectRoot;
import net.artur.nacikmod.registry.ModEffects;
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
        // Обновляем root capability только если это ROOT эффект
        if (event.getEffectInstance().getEffect() instanceof EffectRoot) {
            updateRootDataOnRootEffect(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        // Обновляем root capability только если это ROOT эффект
        if (event.getEffectInstance().getEffect() instanceof EffectRoot) {
            updateRootDataOnRootEffect(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        // При истечении ROOT эффекта ничего не делаем - очистка происходит в onEffectRemoved
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() instanceof EffectRoot
                && event.getEntity() != null
                && !event.getEntity().level().isClientSide()) {

            event.getEntity().getCapability(RootProvider.ROOT_CAPABILITY).ifPresent(IRootData::clear);
        }
    }

    /**
     * Обновляет root capability только при применении ROOT эффекта.
     * Обновляет только если root capability еще не инициализирован (первое применение),
     * чтобы не перезаписывать позицию при постоянном обновлении других эффектов.
     * 
     * Также проверяет, что у сущности действительно есть активный ROOT эффект,
     * чтобы не обновлять capability, если эффект был удален между событиями.
     */
    private static void updateRootDataOnRootEffect(LivingEntity entity) {
        if (entity != null && !entity.level().isClientSide()) {
            // Проверяем, что у сущности действительно есть активный ROOT эффект
            if (!entity.hasEffect(ModEffects.ROOT.get())) {
                return;
            }
            
            entity.getCapability(RootProvider.ROOT_CAPABILITY).ifPresent(data -> {
                // Обновляем только если root capability еще не инициализирован
                // Это предотвращает перезапись позиции при постоянном обновлении других эффектов
                if (!data.isInitialized()) {
                    data.setPendingData(
                            entity.blockPosition(),
                            entity.level().dimension()
                    );
                    data.forceCommitData();
                }
            });
        }
    }
}