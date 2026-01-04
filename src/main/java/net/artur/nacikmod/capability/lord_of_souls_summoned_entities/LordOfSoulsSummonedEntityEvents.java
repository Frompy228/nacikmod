package net.artur.nacikmod.capability.lord_of_souls_summoned_entities;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class LordOfSoulsSummonedEntityEvents {

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Player> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY).isPresent()) {
                event.addCapability(new ResourceLocation(NacikMod.MOD_ID, "lord_of_souls_summoned_entities"), new LordOfSoulsSummonedEntityProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        event.getOriginal().reviveCaps();
        event.getEntity().reviveCaps();

        LordOfSoulsSummonedEntityProvider.copyForRespawn(event.getOriginal(), event.getEntity());

        event.getOriginal().invalidateCaps();
        event.getEntity().invalidateCaps();
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ILordOfSoulsSummonedEntity.class);
    }
} 