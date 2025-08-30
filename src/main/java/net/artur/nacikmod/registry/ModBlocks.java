package net.artur.nacikmod.registry;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.block.custom.EnchantmentLimitTable;
import net.artur.nacikmod.block.custom.ModPortalBlock;
import net.artur.nacikmod.block.custom.TemporaryDirt;
import net.artur.nacikmod.block.custom.TemporaryIce;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, NacikMod.MOD_ID);

    public static final RegistryObject<Block> MOD_PORTAL = registerBlock("mod_portal",
            () -> new ModPortalBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noLootTable().noOcclusion().noCollission()));

    public static final RegistryObject<Block> TEMPORARY_DIRT = registerBlock("temporary_dirt",
            () -> new TemporaryDirt(BlockBehaviour.Properties.copy(Blocks.DIRT).noLootTable()));

    public static final RegistryObject<Block> TEMPORARY_ICE = registerBlock("temporary_ice",
            () -> new TemporaryIce(BlockBehaviour.Properties.copy(Blocks.ICE).noLootTable()));

    public static final RegistryObject<Block> FIRE_TRAP = registerBlock("fire_trap",
            () -> new net.artur.nacikmod.block.custom.FireTrap(BlockBehaviour.Properties.of().noLootTable().noOcclusion()));

    public static final RegistryObject<Block> ENCHANTMENT_LIMIT_TABLE = registerBlock("enchantment_limit_table",
            () -> new EnchantmentLimitTable(
                    BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                            .requiresCorrectToolForDrops()
                            .lightLevel(s -> 10) // brighter than vanilla tables
            ));



    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
