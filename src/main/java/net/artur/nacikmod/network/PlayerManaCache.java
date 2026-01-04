package net.artur.nacikmod.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Кэш маны других игроков на клиенте
 * Используется для отображения маны в рендере, так как capability других игроков недоступна
 */
@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
public class PlayerManaCache {
    private static final Map<UUID, ManaData> cache = new HashMap<>();

    public static void updatePlayerMana(UUID playerId, int mana, int maxMana) {
        cache.put(playerId, new ManaData(mana, maxMana));
    }

    public static ManaData getPlayerMana(UUID playerId) {
        return cache.get(playerId);
    }

    public static void clear() {
        cache.clear();
    }

    public static void remove(UUID playerId) {
        cache.remove(playerId);
    }

    public static class ManaData {
        public final int mana;
        public final int maxMana;

        public ManaData(int mana, int maxMana) {
            this.mana = mana;
            this.maxMana = maxMana;
        }
    }
}

