package net.artur.nacikmod.datagen;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Supplier;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, NacikMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        registerBlock(ModBlocks.MAGIC_ENCHANTING_TABLE_BLOCK, "magic_enchanting_table_block");
    }

    private void registerBlock(Supplier<? extends Block> blockSupplier, String name) {
        Block block = blockSupplier.get();

        // Используем уже существующую модель
        ModelFile model = models().getExistingFile(new ResourceLocation(NacikMod.MOD_ID, "block/" + name));

        // Регистрируем blockstate
        simpleBlock(block, model);
    }
}
