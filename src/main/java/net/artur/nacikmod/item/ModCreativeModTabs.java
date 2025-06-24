package net.artur.nacikmod.item;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NacikMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> NACIKMOD_TAB = CREATIVE_MODE_TABS.register("nacikmod_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.MAGIC_CIRCUIT.get()))
                    .title(Component.translatable("creativetab.nacikmod_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.LANS_OF_NACII.get());
                        pOutput.accept(ModItems.LANS_OF_PROTECTION.get());
                        pOutput.accept(ModItems.HIRAISHIN_WITHOUT_SEAL.get());
                        pOutput.accept(ModItems.HIRAISHIN.get());
                        pOutput.accept(ModItems.MANA_SWORD.get());
                        pOutput.accept(ModItems.MAGIC_BOW.get());
                        pOutput.accept(ModItems.LEONID_SHIELD.get());
                        pOutput.accept(ModItems.LEONID_HELMET.get());
                        pOutput.accept(ModItems.MAGIC_CHARM.get());
                        pOutput.accept(ModItems.MAGIC_ARMOR.get());
                        pOutput.accept(ModItems.RING_OF_TIME.get());
                        pOutput.accept(ModItems.DARK_SPHERE.get());
                        pOutput.accept(ModItems.MANA_CRYSTAL.get());
                        pOutput.accept(ModItems.MAGIC_SEAL.get());
                        pOutput.accept(ModItems.MAGIC_CIRCUIT.get());
                        pOutput.accept(ModItems.MANA_BLESSING.get());
                        pOutput.accept(ModItems.SHARD_OF_ARTIFACT.get());
                        pOutput.accept(ModItems.LANSER_SPAWN_EGG.get());
                        pOutput.accept(ModItems.LEONID_SPAWN_EGG.get());
                        pOutput.accept(ModItems.SPARTAN_SPAWN_EGG.get());
                        pOutput.accept(ModItems.BERSERKER_SPAWN_EGG.get());
                        pOutput.accept(ModItems.MAGIC_HEALING.get());
                        pOutput.accept(ModItems.RELEASE.get());
                        pOutput.accept(ModItems.MAGIC_WEAPONS.get());
                        pOutput.accept(ModItems.FIRE_FLOWER.get());
                        pOutput.accept(ModItems.ICE_PRISON.get());
                        pOutput.accept(ModItems.GRAVITY.get());
                        pOutput.accept(ModItems.SHINRA_TENSEI.get());
                        pOutput.accept(ModItems.SENSORY_RAIN.get());
                        pOutput.accept(ModItems.POCKET.get());
                        pOutput.accept(ModItems.ABSOLUTE_VISION.get());
                        pOutput.accept(ModItems.INTANGIBILITY.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}