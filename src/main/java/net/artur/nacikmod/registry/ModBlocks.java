package net.artur.nacikmod.registry;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.block.custom.*;
import net.artur.nacikmod.block.custom.BarrierBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
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

    public static final RegistryObject<Block> BLOOD_CIRCLE = registerBlock("blood_circle",
            BloodCircle::new);

    public static final RegistryObject<Block> BLOOD_CIRCLE_CORNER = registerBlock("blood_circle_corner",
            BloodCircleCorner::new);

    public static final RegistryObject<Block> ENCHANTMENT_LIMIT_TABLE = registerBlock("enchantment_limit_table",
            () -> new EnchantmentLimitTable(
                    BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                            .requiresCorrectToolForDrops()
                            .strength(60.0F, 1200.0F)
                            .lightLevel(s -> 10) // brighter than vanilla tables
            ));

    public static final RegistryObject<Block> BARRIER = registerBlock("barrier",
            () -> new Barrier(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .strength(0.1F, 0.1F) // почти моментально ломается
                    .noCollission()));

    public static final RegistryObject<Block> BARRIER_BLOCK = registerBlock("barrier_block",
            () -> new BarrierBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .strength(0.8F, 0.8F)
                    .noLootTable()));




    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        Item.Properties properties = new Item.Properties();

        // Добавляем редкость только для стола зачарования
        if (name.equals("enchantment_limit_table")) {
            properties.rarity(Rarity.EPIC);
        }

        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), properties));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
