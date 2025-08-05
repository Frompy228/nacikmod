package net.artur.nacikmod.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.artur.nacikmod.NacikMod;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class AdvancementRewardManager {
    
    private static final String REWARDS_FILE = "nacikmod_advancement_rewards.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type SET_TYPE = new TypeToken<Set<String>>(){}.getType();
    
    private static Set<String> rewardedAdvancements = new HashSet<>();
    private static Path rewardsFilePath;
    
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        rewardsFilePath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve(REWARDS_FILE);
        loadRewards();
    }
    
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        saveRewards();
    }
    
    /**
     * Загружает данные о выданных наградах из файла
     */
    private static void loadRewards() {
        if (!Files.exists(rewardsFilePath)) {
            rewardedAdvancements = new HashSet<>();
            return;
        }
        
        try (Reader reader = Files.newBufferedReader(rewardsFilePath)) {
            rewardedAdvancements = GSON.fromJson(reader, SET_TYPE);
            if (rewardedAdvancements == null) {
                rewardedAdvancements = new HashSet<>();
            }
        } catch (IOException e) {
            rewardedAdvancements = new HashSet<>();
        }
    }
    
    /**
     * Сохраняет данные о выданных наградах в файл
     */
    private static void saveRewards() {
        try {
            Files.createDirectories(rewardsFilePath.getParent());
            try (Writer writer = Files.newBufferedWriter(rewardsFilePath)) {
                GSON.toJson(rewardedAdvancements, writer);
            }
        } catch (IOException e) {
        }
    }
    
    /**
     * Проверяет, была ли уже выдана награда за достижение
     */
    public static boolean isRewardAlreadyGiven(String advancementId) {
        return rewardedAdvancements.contains(advancementId);
    }
    
    /**
     * Отмечает достижение как награжденное
     */
    public static void markAsRewarded(String advancementId) {
        rewardedAdvancements.add(advancementId);
        saveRewards(); // Сохраняем сразу после изменения
    }
    
    /**
     * Сбрасывает все награды (для администраторов)
     */
    public static void resetAllRewards() {
        rewardedAdvancements.clear();
        saveRewards();
        NacikMod.LOGGER.info("All advancement rewards have been reset");
    }
    
    /**
     * Получает количество выданных наград
     */
    public static int getRewardedCount() {
        return rewardedAdvancements.size();
    }
    
    /**
     * Получает список всех выданных наград
     */
    public static Set<String> getAllRewardedAdvancements() {
        return new HashSet<>(rewardedAdvancements);
    }
} 