package net.artur.nacikmod.capability.root;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoRegisterCapability
public class RootProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<IRootData> ROOT_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final IRootData instance = new RootData();
    private final LazyOptional<IRootData> optional = LazyOptional.of(() -> instance);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ROOT_CAPABILITY.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        // Сохраняем зафиксированные данные
        if (instance.isInitialized()) {
            BlockPos committedPos = instance.getCommittedPosition();
            ResourceKey<Level> committedDim = instance.getCommittedDimension();

            tag.putInt("committedX", committedPos.getX());
            tag.putInt("committedY", committedPos.getY());
            tag.putInt("committedZ", committedPos.getZ());
            tag.putString("committedDim", committedDim.location().toString());
        }

        // Сохраняем временные данные
        BlockPos pendingPos = instance.getPendingPosition();
        ResourceKey<Level> pendingDim = instance.getPendingDimension();
        if (pendingPos != null && pendingDim != null) {
            tag.putInt("pendingX", pendingPos.getX());
            tag.putInt("pendingY", pendingPos.getY());
            tag.putInt("pendingZ", pendingPos.getZ());
            tag.putString("pendingDim", pendingDim.location().toString());
        }

        tag.putBoolean("initialized", instance.isInitialized());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        // Загружаем зафиксированные данные
        if (tag.contains("committedX") && tag.contains("committedDim")) {
            BlockPos pos = new BlockPos(
                    tag.getInt("committedX"),
                    tag.getInt("committedY"),
                    tag.getInt("committedZ")
            );
            ResourceKey<Level> dim = ResourceKey.create(
                    Registries.DIMENSION,
                    new ResourceLocation(tag.getString("committedDim"))
            );
            instance.setCommittedData(pos, dim);
        }

        // Загружаем временные данные
        if (tag.contains("pendingX") && tag.contains("pendingDim")) {
            BlockPos pos = new BlockPos(
                    tag.getInt("pendingX"),
                    tag.getInt("pendingY"),
                    tag.getInt("pendingZ")
            );
            ResourceKey<Level> dim = ResourceKey.create(
                    Registries.DIMENSION,
                    new ResourceLocation(tag.getString("pendingDim"))
            );
            instance.setPendingData(pos, dim);
        }

        instance.setInitialized(tag.getBoolean("initialized"));
    }
}