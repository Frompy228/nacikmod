package net.artur.nacikmod;

import com.mojang.logging.LogUtils;
import net.artur.nacikmod.capability.mana.ManaSyncOnDeath;
import net.artur.nacikmod.capability.reward.RewardEvents;
import net.artur.nacikmod.capability.reward.RewardSyncOnDeath;
import net.artur.nacikmod.datagen.ModWorldGenProvider;
import net.artur.nacikmod.item.ModCreativeModTabs;
import net.artur.nacikmod.network.ModMessages;
import net.artur.nacikmod.registry.*;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.CompletableFuture;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(NacikMod.MOD_ID)
public class NacikMod
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "nacikmod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public NacikMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modEventBus);
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherData);
        ModEffects.EFFECTS.register(modEventBus);
        ModEntities.register(modEventBus);
        ModAttributes.ATTRIBUTES.register(modEventBus);
        ModSounds.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModMessages.register();
        MinecraftForge.EVENT_BUS.register(ManaSyncOnDeath.class);
        MinecraftForge.EVENT_BUS.register(RewardEvents.class);
        MinecraftForge.EVENT_BUS.register(RewardSyncOnDeath.class);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        ModCreativeModTabs.register(modEventBus);
        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    @SubscribeEvent
    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<net.minecraft.core.HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new ModWorldGenProvider(packOutput, lookupProvider));
    }
}
