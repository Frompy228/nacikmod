package net.artur.nacikmod.registry;

import net.artur.nacikmod.NacikMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = 
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NacikMod.MOD_ID);

    public static final RegistryObject<SoundEvent> ROAR = registerSoundEvent("roar");
    public static final RegistryObject<SoundEvent> GRAVITY = registerSoundEvent("gravity");
    public static final RegistryObject<SoundEvent> BERSERKER_ROAR = registerSoundEvent("berserker_roar");
    public static final RegistryObject<SoundEvent> GOD_HAND = registerSoundEvent("god_hand");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(NacikMod.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
} 