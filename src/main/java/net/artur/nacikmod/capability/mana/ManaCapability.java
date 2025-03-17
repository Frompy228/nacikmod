package net.artur.nacikmod.capability.mana;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ManaCapability implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<IMana> MANA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final IMana instance = new Mana(100);
    private final LazyOptional<IMana> optional = LazyOptional.of(() -> instance);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == MANA_CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Mana", instance.getMana());
        tag.putInt("MaxMana", instance.getMaxMana());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.setMana(nbt.getInt("Mana"));
        instance.setMaxMana(nbt.getInt("MaxMana"));
    }

    public void invalidate() {
        optional.invalidate();
    }
}