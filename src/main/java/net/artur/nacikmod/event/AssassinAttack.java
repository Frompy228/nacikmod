package net.artur.nacikmod.event;

import net.artur.nacikmod.entity.custom.AssassinEntity;
import net.artur.nacikmod.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;

import java.util.EnumSet;
import java.util.Random;

@Mod.EventBusSubscriber
public class AssassinAttack {
    
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 1200; // 50 секунд (20 тиков * 50)
    private static final double SPAWN_CHANCE = 0.01;
    private static final int ASSASSIN_COUNT = 20; // Количество ассасинов
    private static final double SPAWN_RADIUS = 20.0; // Радиус спавна в блоках
    private static final Random random = new Random();
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Player player = event.player;
        Level level = player.level();
        
        // Проверяем только на серверной стороне
        if (level.isClientSide()) return;
        
        // Проверяем каждые 50 секунд
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) return;
        tickCounter = 0;
        
        // Проверяем, что сейчас ночь (время от 13000 до 23000 тиков)
        long time = level.getDayTime() % 24000;
        if (time < 13000 || time > 23000) return;
        
        // Проверяем 1% шанс
        if (random.nextDouble() >= SPAWN_CHANCE) return;

        
        // Запускаем атаку ассасинов
        spawnAssassinAttack((ServerLevel) level, (ServerPlayer) player);
    }
    
    private static void spawnAssassinAttack(ServerLevel level, ServerPlayer player) {
        Vec3 playerPos = player.position();
        
        for (int i = 0; i < ASSASSIN_COUNT; i++) {
            // Генерируем позицию по кругу вокруг игрока на расстоянии 15 блоков
            double angle = (i * 360.0 / ASSASSIN_COUNT) * Math.PI / 180.0; // Равномерно распределяем по кругу
            double distance = SPAWN_RADIUS; // Фиксированное расстояние 15 блоков
            
            double x = playerPos.x + Math.cos(angle) * distance;
            double z = playerPos.z + Math.sin(angle) * distance;
            
            // Находим безопасную позицию для спавна (на земле)
            BlockPos spawnPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos((int)x, 0, (int)z));
            double y = spawnPos.getY();
            
            // Проверяем, что позиция подходящая
            if (y <= level.getMinBuildHeight()) continue;
            
            // Проверяем, что вокруг есть место для моба
            BlockPos blockPos = new BlockPos((int)x, (int)y, (int)z);
            if (!level.getBlockState(blockPos).isAir() ||
                !level.getBlockState(blockPos.above()).isAir()) {
                continue;
            }
            
            // Создаем ассасина
            AssassinEntity assassin = ModEntities.ASSASSIN.get().create(level);
            if (assassin != null) {
                assassin.setPos(x, y, z);
                assassin.setTarget(player); // Устанавливаем игрока как цель
                
                // Инициализируем ассасина через finalizeSpawn
                assassin.finalizeSpawn(level, 
                        level.getCurrentDifficultyAt(assassin.blockPosition()),
                        MobSpawnType.MOB_SUMMONED, null, null);
                
                // Спавним моба
                if (level.addFreshEntity(assassin)) {
                    // Отправляем сообщение игроку о начале атаки
                    if (i == 0) {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Do you feel the danger in the night..."));
                    }
                }
            }
        }
    }
}
