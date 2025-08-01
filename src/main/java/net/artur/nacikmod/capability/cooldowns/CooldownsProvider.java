package net.artur.nacikmod.capability.cooldowns;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CooldownsProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    public static final Capability<ICooldowns> COOLDOWNS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    
    private Cooldowns cooldowns = null;
    private final LazyOptional<ICooldowns> optional = LazyOptional.of(this::createCooldowns);
    
    private Cooldowns createCooldowns() {
        if (this.cooldowns == null) {
            this.cooldowns = new Cooldowns();
        }
        return this.cooldowns;
    }
    
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return COOLDOWNS_CAPABILITY.orEmpty(cap, optional);
    }
    
    @Override
    public CompoundTag serializeNBT() {
        return createCooldowns().serializeNBT();
    }
    
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createCooldowns().deserializeNBT(nbt);
    }
} 