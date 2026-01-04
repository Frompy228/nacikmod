package net.artur.nacikmod.registry;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.block.custom.Barrier;
import net.artur.nacikmod.block.entity.BarrierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, NacikMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<BarrierBlockEntity>> BARRIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("barrier",
                    () -> BlockEntityType.Builder.of(
                            (BlockPos pos, BlockState state) -> new BarrierBlockEntity(pos, state),
                            ModBlocks.BARRIER.get()
                    ).build(null));
}







