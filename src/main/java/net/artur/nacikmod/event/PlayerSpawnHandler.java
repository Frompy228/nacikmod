package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class PlayerSpawnHandler {
    private static final Set<UUID> playersWithItems = new HashSet<>();
    private static final String DATA_FILE = "nacikmod_players.dat";
    private static final Logger LOGGER = LoggerFactory.getLogger("NacikMod");

    @SubscribeEvent
    public static void onPlayerFirstJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        // Загружаем список игроков, если файл существует
        loadPlayersList();
        
        // Проверяем, получал ли игрок предметы ранее
        if (!playersWithItems.contains(player.getUUID())) {
            // Выдаем предметы
            player.getInventory().add(new ItemStack(ModItems.MAGIC_HEALING.get()));
            player.getInventory().add(new ItemStack(ModItems.RELEASE.get()));
            
            // Добавляем игрока в список и сохраняем
            playersWithItems.add(player.getUUID());
            savePlayersList();
        }
    }

    private static void loadPlayersList() {
        try {
            Path path = Paths.get(DATA_FILE);
            if (Files.exists(path)) {
                String content = Files.readString(path);
                String[] uuids = content.split("\n");
                for (String uuid : uuids) {
                    if (!uuid.isEmpty()) {
                        playersWithItems.add(UUID.fromString(uuid));
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load players list: " + e.getMessage());
        }
    }

    private static void savePlayersList() {
        try {
            StringBuilder content = new StringBuilder();
            for (UUID uuid : playersWithItems) {
                content.append(uuid.toString()).append("\n");
            }
            Files.writeString(Paths.get(DATA_FILE), content.toString());
        } catch (IOException e) {
            LOGGER.error("Failed to save players list: " + e.getMessage());
        }
    }
} 