package net.artur.nacikmod.capability.mana;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoRegisterCapability
public class ManaProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    public static final Capability<IMana> MANA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final IMana mana = new Mana();
    private final LazyOptional<IMana> optionalMana = LazyOptional.of(() -> mana);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == MANA_CAPABILITY ? optionalMana.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Mana", mana.getMana());
        tag.putInt("MaxMana", mana.getMaxMana());
        tag.putBoolean("IsTrueMage", mana.isTrueMage());
        tag.putBoolean("HasVisionBlessing", mana.hasVisionBlessing());
        tag.putBoolean("KodaiActive", mana.isKodaiActive());
        tag.putBoolean("BloodBoneActive", mana.isBloodBoneActive());

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        mana.setMana(tag.getInt("Mana"));
        mana.setMaxMana(tag.getInt("MaxMana"));
        mana.setTrueMage(tag.getBoolean("IsTrueMage"));
        mana.setVisionBlessing(tag.getBoolean("HasVisionBlessing"));
        mana.setKodaiActive(tag.getBoolean("KodaiActive"));
        mana.setBloodBoneActive(tag.getBoolean("BloodBoneActive"));
    }

    // ✅ Сохранение маны после смерти
    public static void copyForRespawn(Player oldPlayer, Player newPlayer) {
        oldPlayer.reviveCaps(); // Получаем капабилити старого игрока
        newPlayer.reviveCaps(); // Получаем капабилити нового игрока

        LazyOptional<IMana> oldManaCap = oldPlayer.getCapability(MANA_CAPABILITY);
        LazyOptional<IMana> newManaCap = newPlayer.getCapability(MANA_CAPABILITY);

        oldManaCap.ifPresent(oldMana -> newManaCap.ifPresent(newMana -> {
            newMana.setMana(oldMana.getMana()); // Передаём количество маны
            newMana.setMaxMana(oldMana.getMaxMana()); // Передаём максимальную ману
            newMana.setTrueMage(oldMana.isTrueMage()); // Передаём статус истинного мага
            newMana.setVisionBlessing(oldMana.hasVisionBlessing()); // Передаём статус Vision Blessing
            newMana.setKodaiActive(oldMana.isKodaiActive());
            newMana.setBloodBoneActive(oldMana.isBloodBoneActive());
        }));
    }
}