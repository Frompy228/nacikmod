package net.artur.nacikmod.capability.mana;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;

public class ManaCapability implements ICapabilityProvider {
    public static final Capability<IMana> MANA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final IMana instance = new Mana();
    private final LazyOptional<IMana> optional = LazyOptional.of(() -> instance);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == MANA_CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    public void invalidate() {
        optional.invalidate();
    }

    /**
     * Сохраняет ману в `PlayerData` игрока.
     */
    public void saveManaToPlayerData(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(MANA_CAPABILITY).ifPresent(mana -> {
                CompoundTag playerData = serverPlayer.getPersistentData();

                // Создаем новый тег, если его нет
                if (!playerData.contains("ManaData")) {
                    playerData.put("ManaData", new CompoundTag());
                }

                CompoundTag manaData = playerData.getCompound("ManaData");

                manaData.putInt("Mana", mana.getMana());
                manaData.putInt("MaxMana", mana.getMaxMana());

                // Убеждаемся, что данные обновляются
                playerData.put("ManaData", manaData);
            });
        }
    }

    /**
     * Загружает ману из `PlayerData` при входе игрока в мир.
     */
    public void loadManaFromPlayerData(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(MANA_CAPABILITY).ifPresent(mana -> {
                CompoundTag playerData = serverPlayer.getPersistentData();
                CompoundTag manaData = playerData.getCompound("ManaData"); // Получаем данные, если есть

                int currentMana = manaData.contains("Mana") ? manaData.getInt("Mana") : 100;
                int maxMana = manaData.contains("MaxMana") ? manaData.getInt("MaxMana") : 100;

                mana.setMana(currentMana);
                mana.setMaxMana(maxMana);

                // Добавим проверку: если мана все еще 0, то установить её в значение по умолчанию
                if (mana.getMana() == 0) {
                    mana.setMana(100);  // Устанавливаем значение по умолчанию, если мана была 0
                }
            });
        }
    }
}
