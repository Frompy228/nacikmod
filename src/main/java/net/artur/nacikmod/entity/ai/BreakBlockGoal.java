package net.artur.nacikmod.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI Goal для ломания блоков, чтобы достичь цели.
 * Основан на оригинальном коде для Fabric, адаптирован для Forge.
 * 
 * Ключевые особенности:
 * 1. Система приоритетов поиска блоков:
 *    - Приоритет 1: Блоки напрямую между сущностью и целью
 *    - Приоритет 2: Блоки в конечной точке пути навигации
 *    - Приоритет 3: Двери в радиусе поиска
 *    - Приоритет 4: Блоки в направлении взгляда
 *    - Приоритет 5: Удаленные блоки (периодический поиск)
 * 
 * 2. Система кэширования пути для оптимизации
 * 3. Проверка доступности блока (line of sight)
 * 4. Синхронизация ломания блоков между несколькими сущностями
 * 5. Визуальная обратная связь (частицы, звуки, прогресс ломания)
 */
public class BreakBlockGoal extends Goal {
    // Константы
    private static final float MINING_SPEED = 2.0F; // Скорость ломания
    private static final float HORIZONTAL_MINING_RANGE = 2.0F; // Горизонтальный радиус ломания
    private static final float VERTICAL_MINING_RANGE = 3.0F; // Вертикальный радиус ломания
    private static final int MAX_MINING_POSITIONS = 50; // Максимум одновременно ломаемых блоков
    private static final int MAX_IDLE_TICKS_DURING_BLOCK_FINDING = 20; // Максимум тиков простоя при поиске
    private static final int PATH_UPDATE_INTERVAL = 3; // Интервал обновления кэшированного пути
    private static final int DOOR_SEARCH_RADIUS = 5; // Радиус поиска дверей
    private static final int FAR_BLOCK_SEARCH_DISTANCE = 8; // Дистанция поиска удаленных блоков
    
    // Отладочные флаги
    private final boolean debug = false;
    private final boolean blockDebug = false;
    
    // Ссылка на сущность
    private final Monster mob;
    
    // Состояние цели
    private BlockPos targetBlock;
    private int miningTicks;
    private float breakProgress;
    private int noPathTicks;
    
    // Счетчики для управления поиском блоков
    private int failedBlockFindings;
    private int blockFindingIdleTicks;
    
    // Кэширование пути
    private Path cachedPath;
    private int pathUpdateCounter;
    private BlockPos lastTargetPos;
    
    // Статические коллекции для синхронизации между сущностями
    private static final Set<BlockPos> currentlyMining = Collections.newSetFromMap(new ConcurrentHashMap<>(MAX_MINING_POSITIONS));
    private static final Map<Monster, BreakBlockGoal> goalMap = new ConcurrentHashMap<>();
    
    // Теги ломаемых блоков (кэш)
    private static final Set<String> DEFAULT_BREAKABLE_BLOCKS = new HashSet<>(Arrays.asList(
            "#minecraft:mineable/pickaxe",
            "#minecraft:mineable/axe",
            "#minecraft:mineable/shovel",
            "minecraft:glass",
            "minecraft:glass_pane",
            "minecraft:stone",
            "minecraft:cobblestone",
            "minecraft:dirt",
            "minecraft:grass_block",
            "minecraft:wooden_door",
            "minecraft:iron_door"
    ));
    
    public BreakBlockGoal(Monster mob) {
        this.mob = mob;
        goalMap.put(mob, this);
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }
    
    /**
     * Сбрасывает все состояния ломания блоков (для очистки при необходимости)
     */
    public static void resetAllMiningStates() {
        currentlyMining.clear();
    }
    
    /**
     * Сбрасывает состояние ломания для конкретной сущности
     */
    public static synchronized void resetMiningState(Monster mob) {
        BreakBlockGoal goal = goalMap.get(mob);
        if (goal != null) {
            Level world = mob.level();
            if (world != null && goal.targetBlock != null) {
                world.destroyBlockProgress(mob.getId(), goal.targetBlock, -1);
                currentlyMining.remove(goal.targetBlock);
            }
            goal.targetBlock = null;
            goal.breakProgress = 0;
            goal.blockFindingIdleTicks = 0;
            goalMap.remove(mob);
        }
    }
    
    @Override
    public boolean canUse() {
        if (mob == null || !mob.isAlive()) {
            return false;
        }
        
        // ПРИОРИТЕТ 1: Защита от удушья - проверяем блоки, которые душат сущность
        BlockPos suffocationBlock = findSuffocationBlock();
        if (suffocationBlock != null && !currentlyMining.contains(suffocationBlock)) {
            targetBlock = suffocationBlock;
            currentlyMining.add(targetBlock);
            miningTicks = 0;
            breakProgress = 0;
            blockFindingIdleTicks = 0;
            if (debug) System.out.println("Suffocation protection activated for " + mob.getName().getString());
            return true;
        }
        
        // ПРИОРИТЕТ 2: Обычный поиск блоков на пути к цели
        if (mob.getTarget() == null || !mob.getTarget().isAlive()) {
            return false;
        }
        
        // Если сущность следует по пути, не нужно ломать блоки
        if (mob.getNavigation().isInProgress()) {
            return false;
        }
        
        // Проверяем кэшированный путь до цели
        Path path = getCachedPathToTarget();
        
        // Если есть валидный путь до цели, не нужно ломать блоки
        if (path != null && path.canReach() && path.isDone()) {
            return false;
        }
        
        // Если путь существует, но не достигает цели, ищем блок для ломания
        if (path != null && path.getNodeCount() > 0 && !path.canReach()) {
            BlockPos blockToBreak = findBlockToBreak();
            if (blockToBreak != null && !currentlyMining.contains(blockToBreak)) {
                targetBlock = blockToBreak;
                currentlyMining.add(targetBlock);
                miningTicks = 0;
                breakProgress = 0;
                blockFindingIdleTicks = 0;
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void start() {
        if (debug) System.out.println("BreakBlockGoal started for " + mob.getName().getString());
        mob.getNavigation().stop();
        miningTicks = 0;
        breakProgress = 0;
        goalMap.put(mob, this);
    }
    
    @Override
    public void stop() {
        Level world = mob.level();
        if (world != null && targetBlock != null && mob != null) {
            world.destroyBlockProgress(mob.getId(), targetBlock, -1);
            currentlyMining.remove(targetBlock);
        }
        targetBlock = null;
        breakProgress = 0;
        goalMap.remove(mob);
        if (debug) System.out.println("BreakBlockGoal stopped");
    }
    
    @Override
    public boolean canContinueToUse() {
        if (mob == null || mob.level() == null || !mob.isAlive()) {
            return false;
        }
        
        // ПРИОРИТЕТ 1: Защита от удушья - абсолютный приоритет, работает даже без цели
        BlockPos suffocationBlock = findSuffocationBlock();
        if (suffocationBlock != null) {
            // Если есть блок удушья, продолжаем ломать его независимо от цели
            // НЕ проверяем цель, не проверяем путь - только выживание
            return true;
        }
        
        // ПРИОРИТЕТ 2: Если есть текущий блок, продолжаем его ломать, если он валиден
        if (targetBlock != null) {
            Level world = mob.level();
            if (world != null && world.isLoaded(targetBlock)) {
                BlockState state = world.getBlockState(targetBlock);
                // Если блок еще существует и не воздух, продолжаем ломать
                if (!state.isAir()) {
                    // Продолжаем, даже если цель изменилась - нужно закончить текущий блок
                    return true;
                }
            }
        }
        
        // ПРИОРИТЕТ 3: Обычная логика с целью
        if (mob.getTarget() == null || !mob.getTarget().isAlive()) {
            // Нет цели и нет удушья - можем остановиться
            return false;
        }
        
        Path path = mob.getNavigation().createPath(mob.getTarget(), 0);
        if (path == null) {
            noPathTicks++;
        } else {
            noPathTicks = 0;
        }
        
        // Если долго нет пути, прекращаем цель
        if (noPathTicks > 5) {
            if (debug) System.out.println("No path for 5+ ticks, stopping");
            noPathTicks = 0;
            return false;
        }
        
        // Если слишком много неудачных попыток найти блок, прекращаем
        if (failedBlockFindings > 100) {
            failedBlockFindings = 0;
            if (debug) System.out.println("Too many failed block findings, stopping");
            return false;
        }
        
        // Проверяем, не стал ли путь к цели лучше
        if (isPathingBetterThanBlockBreaking()) {
            return false;
        }
        
        // Продолжаем ломать текущий блок, если путь все еще не ведет к цели
        if (path != null && path.getNodeCount() > 0 && !path.canReach() && !path.isDone()) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public void tick() {
        // Проверки безопасности
        Level world = mob.level();
        if (mob == null || world == null || !mob.isAlive()) {
            stop();
            return;
        }
        
        // ПРИОРИТЕТ 1: Защита от удушья - абсолютный приоритет, работает даже без цели
        BlockPos suffocationBlock = findSuffocationBlock();
        boolean isSuffocating = suffocationBlock != null;
        
        if (isSuffocating) {
            // Немедленно переключаемся на блок удушья, если это не текущий блок
            if (!suffocationBlock.equals(targetBlock)) {
                if (targetBlock != null) {
                    currentlyMining.remove(targetBlock);
                    world.destroyBlockProgress(mob.getId(), targetBlock, -1);
                }
                targetBlock = suffocationBlock;
                currentlyMining.add(targetBlock);
                miningTicks = 0;
                breakProgress = 0;
                blockFindingIdleTicks = 0;
                if (debug) System.out.println("Suffocation detected! Switching to block: " + suffocationBlock);
            }
            // Если есть блок удушья, продолжаем его ломать (логика продолжается ниже)
        } else {
            // Нет удушья - можем заниматься целью или продолжать ломать текущий блок
            // ПРИОРИТЕТ 2: Если есть текущий блок, продолжаем его ломать (не прерываем)
            if (targetBlock != null) {
                // Проверяем валидность текущего блока
                BlockState currentState = world.getBlockState(targetBlock);
                if (currentState.isAir()) {
                    // Блок уже сломан, завершаем
                    completeBlockBreak();
                    targetBlock = null;
                } else {
                    // Блок еще существует - продолжаем его ломать, даже если изменилась цель
                    // Это предотвращает прерывание ломания
                }
            }
            
            // ПРИОРИТЕТ 3: Поиск нового блока на пути к цели (только если нет текущего блока)
            if (targetBlock == null) {
                if (mob.getTarget() != null && mob.getTarget().isAlive()) {
                    // Поиск нового блока на пути к цели
                    targetBlock = findBlockToBreak();
                    if (targetBlock != null) {
                        currentlyMining.add(targetBlock);
                        miningTicks = 0;
                        breakProgress = 0;
                        blockFindingIdleTicks = 0;
                    } else {
                        // Если блок не найден, пытаемся следовать по пути
                        Path path = getCachedPathToTarget();
                        if (path != null) {
                            mob.getNavigation().moveTo(path, 1.0);
                            mob.getLookControl().setLookAt(mob.getTarget(), 30.0F, 30.0F);
                        }
                        return;
                    }
                } else {
                    // Нет цели и нет удушья и нет текущего блока - останавливаемся
                    stop();
                    return;
                }
            }
        }
        
        // Если нет текущего блока (не должно произойти, но для безопасности)
        if (targetBlock == null) {
            if (isSuffocating) {
                // Если удушье, но блок не найден - ищем снова
                targetBlock = findSuffocationBlock();
                if (targetBlock != null) {
                    currentlyMining.add(targetBlock);
                    miningTicks = 0;
                    breakProgress = 0;
                    blockFindingIdleTicks = 0;
                } else {
                    return; // Странная ситуация - нет блока удушья, но должно быть
                }
            } else {
                return; // Нет блока для ломания
            }
        }
        
        // Если блок больше не существует, завершаем ломание и ищем новый
        BlockState blockState = world.getBlockState(targetBlock);
        if (blockState.isAir()) {
            completeBlockBreak();
            return;
        }
        
        // Выбираем новый блок, если долго простаиваем при поиске пути к блоку
        if (blockFindingIdleTicks > MAX_IDLE_TICKS_DURING_BLOCK_FINDING && targetBlock != null) {
            currentlyMining.remove(targetBlock);
            targetBlock = null;
            if (debug) System.out.println("Mob idle during block finding -> selecting new block");
            return;
        }
        
        // Проверки валидности блока
        if (targetBlock != null && !world.isLoaded(targetBlock)) {
            stop();
            return;
        }
        
        // Проверяем, можно ли ломать блок (hardness < 0 означает неразрушимый блок)
        float hardness = blockState.getDestroySpeed(world, targetBlock);
        if (hardness < 0 || hardness > 50) {
            stop();
            return;
        }
        
        // Основная логика: ломание или навигация к блоку
        if (isWithinMiningRange(targetBlock)) {
            performMining(world);
        } else {
            navigateToTargetBlock();
        }
    }
    
    /**
     * Проверяет, не стал ли путь к цели лучше, чем ломание блоков
     */
    private boolean isPathingBetterThanBlockBreaking() {
        Path path = getCachedPathToTarget();
        if (path == null || path.getNodeCount() == 0 || path.getNodeCount() < 30 || mob.getTarget() == null) {
            return false;
        }
        
        BlockPos targetPos = mob.getTarget().blockPosition();
        BlockPos mobPos = mob.blockPosition();
        Node endNode = path.getEndNode();
        if (endNode == null) {
            return false;
        }
        BlockPos nodePos = new BlockPos(endNode.x, endNode.y, endNode.z);
        
        double mobDistanceToTarget = mobPos.distSqr(targetPos);
        double nodeDistanceToTarget = nodePos.distSqr(targetPos);
        
        // Если конечная точка пути значительно ближе к цели (на 20% от изначальной дистанции)
        if (nodeDistanceToTarget < mobDistanceToTarget * 0.2) {
            if (debug) System.out.println("Pathing is better than block breaking, stopping goal");
            return true;
        }
        
        return false;
    }
    
    /**
     * Получает кэшированный путь до цели
     */
    private Path getCachedPathToTarget() {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            cachedPath = null;
            lastTargetPos = null;
            return null;
        }
        
        BlockPos targetPos = target.blockPosition();
        pathUpdateCounter++;
        
        // Обновляем путь при необходимости
        if (pathUpdateCounter >= PATH_UPDATE_INTERVAL || cachedPath == null || 
                cachedPath.isDone() || cachedPath.getNodeCount() == 0 ||
                (lastTargetPos != null && !lastTargetPos.equals(targetPos) && lastTargetPos.distSqr(targetPos) > 1.0)) {
            cachedPath = mob.getNavigation().createPath(target, 0);
            pathUpdateCounter = 0;
            lastTargetPos = targetPos;
        }
        
        return cachedPath;
    }
    
    /**
     * Ищет блоки, которые душат сущность (блоки внутри хитбокса)
     * Учитывает случай, когда сущность стоит на стыке блоков
     * Приоритет отдается уже ломаемому блоку, если он все еще валиден
     */
    private BlockPos findSuffocationBlock() {
        if (mob == null || mob.level() == null) {
            return null;
        }
        
        Level world = mob.level();
        AABB boundingBox = mob.getBoundingBox();
        
        // ПРИОРИТЕТ 1: Если текущий блок - это блок удушья и он все еще валиден, продолжаем его ломать
        if (targetBlock != null) {
            BlockPos currentPos = targetBlock;
            // Проверяем, что текущий блок все еще пересекается с хитбоксом
            AABB currentBlockBox = new AABB(currentPos);
            if (boundingBox.intersects(currentBlockBox)) {
                BlockState currentState = world.getBlockState(currentPos);
                // Если блок все еще существует и не воздух, и все еще душит
                if (!currentState.isAir() && !currentState.canBeReplaced() && isBreakableBlock(world, currentPos)) {
                    // Проверяем, что блок не под ногами
                    double mobFeetY = mob.getBoundingBox().minY;
                    if (currentPos.getY() >= (int) Math.floor(mobFeetY)) {
                        if (debug) System.out.println("Current block is still suffocating: " + currentPos);
                        return currentPos; // Продолжаем ломать текущий блок
                    }
                }
            }
        }
        
        // ПРИОРИТЕТ 2: Ищем другие блоки удушья, если текущий блок больше не валиден
        int minX = (int) Math.floor(boundingBox.minX);
        int maxX = (int) Math.ceil(boundingBox.maxX);
        int minY = (int) Math.floor(boundingBox.minY);
        int maxY = (int) Math.ceil(boundingBox.maxY);
        int minZ = (int) Math.floor(boundingBox.minZ);
        int maxZ = (int) Math.ceil(boundingBox.maxZ);
        
        // Проверяем все блоки внутри и на границах хитбокса
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    BlockPos checkPos = new BlockPos(x, y, z);
                    
                    // Пропускаем блоки под ногами (не ломаем опору)
                    double mobFeetY = mob.getBoundingBox().minY;
                    if (y < (int) Math.floor(mobFeetY)) {
                        continue;
                    }
                    
                    BlockState state = world.getBlockState(checkPos);
                    
                    // Проверяем, что блок не воздух и не может быть заменен
                    if (!state.isAir() && !state.canBeReplaced()) {
                        // Проверяем, что блок реально пересекается с хитбоксом
                        AABB blockBox = new AABB(checkPos);
                        if (boundingBox.intersects(blockBox)) {
                            // Проверяем, можно ли ломать этот блок
                            if (isBreakableBlock(world, checkPos)) {
                                // Предпочитаем блок, который не ломается другими сущностями
                                if (!currentlyMining.contains(checkPos)) {
                                    if (debug) System.out.println("Found new suffocation block at " + checkPos);
                                    return checkPos;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Ищет блок для ломания с системой приоритетов
     */
    private BlockPos findBlockToBreak() {
        // Выполняем интенсивный поиск блоков только в половине случаев для оптимизации
        if (new Random().nextInt(2) == 0) {
            return null;
        }
        
        Level world = mob.level();
        LivingEntity target = mob.getTarget();
        
        if (!(world instanceof ServerLevel) || target == null) {
            return null;
        }
        
        Vec3 mobPos = mob.position();
        Vec3 targetPos = target.position();
        Direction facing = mob.getDirection();
        
        BlockPos mobBlockPos = mob.blockPosition();
        double distanceToTarget = Math.sqrt(mob.distanceToSqr(target));
        
        // Получаем диапазон высоты в зависимости от позиции относительно цели
        int[] heightRange = getMinYMaxYAtEndNode(mobPos, targetPos);
        int minY = heightRange[0];
        int maxY = heightRange[1];
        int mobBelowTarget = heightRange[2];
        
        // ----------------------------------------
        // ПРИОРИТЕТ 1: Блоки напрямую между мобом и целью
        // ----------------------------------------
        List<BlockPos> directPathBlocks = new ArrayList<>();
        Vec3 directionVec = targetPos.subtract(mobPos).normalize();
        int maxCheckDistance = (int) Math.min(distanceToTarget, 3.0);
        
        for (int i = 1; i <= maxCheckDistance; i++) {
            Vec3 checkVec = mobPos.add(directionVec.scale(i));
            int checkX = (int) Math.floor(checkVec.x);
            int checkZ = (int) Math.floor(checkVec.z);
            
            int startY = (mobBelowTarget == 1) ? maxY : minY;
            int endY = (mobBelowTarget == 1) ? minY : maxY;
            int step = (mobBelowTarget == 1) ? -1 : 1;
            
            for (int y = startY; (mobBelowTarget == 1) ? (y >= endY) : (y <= endY); y += step) {
                BlockPos checkPos = new BlockPos(checkX, (int) mobPos.y + y, checkZ);
                BlockState blockState = world.getBlockState(checkPos);
                
                if (!blockState.isAir() && isBreakableBlock(world, checkPos)) {
                    if (isBlockAccessibleFromDirection(world, checkPos, mobBlockPos)) {
                        if (!currentlyMining.contains(checkPos)) {
                            directPathBlocks.add(checkPos);
                        }
                    }
                }
            }
        }
        
        if (!directPathBlocks.isEmpty()) {
            BlockPos pos = directPathBlocks.get(new Random().nextInt(directPathBlocks.size()));
            if (!currentlyMining.contains(pos)) {
                failedBlockFindings = 0;
                if (blockDebug) System.out.println("Direct path block found!");
                return pos;
            }
        }
        
        // Проверяем смежные блоки, если прямых блоков нет
        List<int[]> offsets = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                offsets.add(new int[]{x, z});
            }
        }
        Collections.shuffle(offsets);
        
        for (int i = 1; i <= maxCheckDistance; i++) {
            Vec3 checkVec = mobPos.add(directionVec.scale(i));
            int checkX = (int) Math.floor(checkVec.x);
            int checkZ = (int) Math.floor(checkVec.z);
            
            for (int[] offset : offsets) {
                int xOffset = offset[0];
                int zOffset = offset[1];
                
                int startY = (mobBelowTarget == 1) ? maxY : minY;
                int endY = (mobBelowTarget == 1) ? minY : maxY;
                int step = (mobBelowTarget == 1) ? -1 : 1;
                
                for (int y = startY; (mobBelowTarget == 1) ? (y >= endY) : (y <= endY); y += step) {
                    BlockPos adjacentPos = new BlockPos(checkX + xOffset, (int) mobPos.y + y, checkZ + zOffset);
                    BlockState blockState = world.getBlockState(adjacentPos);
                    
                    if (!blockState.isAir() && isBreakableBlock(world, adjacentPos)) {
                        if (!currentlyMining.contains(adjacentPos) && isBlockAccessibleFromDirection(world, adjacentPos, mobBlockPos)) {
                            failedBlockFindings = 0;
                            if (blockDebug) System.out.println("Adjacent block found!");
                            return adjacentPos;
                        }
                    }
                }
            }
        }
        
        // ----------------------------------------
        // ПРИОРИТЕТ 2: Блоки в конечной точке пути навигации
        // ----------------------------------------
        Path path = getCachedPathToTarget();
        if (path != null && path.getNodeCount() > 0) {
            Node endNode = path.getEndNode();
            if (endNode == null) {
                failedBlockFindings++;
                return null;
            }
            BlockPos endNodeBlockPos = new BlockPos(endNode.x, endNode.y, endNode.z);
            
            int[] nodeHeightRange = getMinYMaxYAtEndNode(new Vec3(endNode.x, endNode.y, endNode.z), targetPos);
            int minYatNode = nodeHeightRange[0];
            int maxYatNode = nodeHeightRange[1];
            int mobBelowTargetatNode = nodeHeightRange[2];
            
            Collections.shuffle(offsets);
            for (int[] offset : offsets) {
                int xOffset = offset[0];
                int zOffset = offset[1];
                
                int startY = (mobBelowTargetatNode == 1) ? maxYatNode : minYatNode;
                int endY = (mobBelowTargetatNode == 1) ? minYatNode : maxYatNode;
                int step = (mobBelowTargetatNode == 1) ? -1 : 1;
                
                for (int y = startY; (mobBelowTargetatNode == 1) ? (y >= endY) : (y <= endY); y += step) {
                    BlockPos checkPos = endNodeBlockPos.offset(xOffset, y, zOffset);
                    BlockState blockState = world.getBlockState(checkPos);
                    
                    if (!blockState.isAir() && isBreakableBlock(world, checkPos)) {
                        if (!currentlyMining.contains(checkPos) && isBlockAccessibleFromDirection(world, checkPos, endNodeBlockPos)) {
                            failedBlockFindings = 0;
                            if (blockDebug) System.out.println("Block on path end node found!");
                            return checkPos;
                        }
                    }
                }
            }
        }
        
        // ----------------------------------------
        // ПРИОРИТЕТ 3: Поиск дверей
        // ----------------------------------------
        if (failedBlockFindings % 10 == 0) {
            List<BlockPos> doorBlocks = new ArrayList<>();
            for (int x = -DOOR_SEARCH_RADIUS; x <= DOOR_SEARCH_RADIUS; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -DOOR_SEARCH_RADIUS; z <= DOOR_SEARCH_RADIUS; z++) {
                        BlockPos doorPos = mobBlockPos.offset(x, y, z);
                        if (x * x + y * y + z * z > DOOR_SEARCH_RADIUS * DOOR_SEARCH_RADIUS) {
                            continue;
                        }
                        
                        BlockState state = world.getBlockState(doorPos);
                        if (!state.isAir() && state.is(BlockTags.DOORS) && isBreakableBlock(world, doorPos)) {
                            if (canPathToBlockAndIsAccessible(doorPos)) {
                                doorBlocks.add(doorPos);
                            }
                        }
                    }
                }
            }
            
            for (BlockPos doorPos : doorBlocks) {
                if (!currentlyMining.contains(doorPos)) {
                    failedBlockFindings = 0;
                    if (blockDebug) System.out.println("Door block found!");
                    return doorPos;
                }
            }
        }
        
        // ----------------------------------------
        // ПРИОРИТЕТ 4: Блоки в направлении взгляда
        // ----------------------------------------
        List<BlockPos> facingBlocks = new ArrayList<>();
        int checkRadius = distanceToTarget > 8.0 ? 2 : 1;
        
        for (int dist = 1; dist <= checkRadius; dist++) {
            for (int y = minY; y <= maxY; y++) {
                BlockPos checkPos = mobBlockPos.relative(facing, dist).offset(0, y, 0);
                BlockState blockState = world.getBlockState(checkPos);
                if (!blockState.isAir() && isBreakableBlock(world, checkPos)) {
                    if (isBlockAccessibleFromDirection(world, checkPos, mobBlockPos)) {
                        if (!currentlyMining.contains(checkPos)) {
                            facingBlocks.add(checkPos);
                        }
                    }
                }
            }
        }
        
        for (BlockPos pos : facingBlocks) {
            if (!currentlyMining.contains(pos)) {
                failedBlockFindings = 0;
                if (blockDebug) System.out.println("Facing direction block found!");
                return pos;
            }
        }
        
        failedBlockFindings++;
        return null;
    }
    
    /**
     * Определяет диапазон высоты для поиска блоков в зависимости от относительной позиции
     */
    private int[] getMinYMaxYAtEndNode(Vec3 endPos, Vec3 targetPos) {
        double yDiff = targetPos.y - endPos.y;
        int minY, maxY, mobBelowTarget;
        
        if (Math.abs(yDiff) < 0.5) {
            minY = 0;
            maxY = 1;
            mobBelowTarget = 0;
        } else if (yDiff < -1) {
            minY = -1;
            maxY = 1;
            mobBelowTarget = 0;
        } else if (yDiff < 0) {
            minY = 0;
            maxY = 1;
            mobBelowTarget = 0;
        } else if (yDiff <= 1) {
            minY = 0;
            maxY = 1;
            mobBelowTarget = 1;
        } else if (yDiff <= 2) {
            minY = 0;
            maxY = 2;
            mobBelowTarget = 1;
        } else {
            minY = 0;
            maxY = 3;
            mobBelowTarget = 1;
        }
        
        return new int[]{minY, maxY, mobBelowTarget};
    }
    
    /**
     * Проверяет, доступен ли блок с определенного направления (line of sight)
     */
    private boolean isBlockAccessibleFromDirection(Level world, BlockPos targetBlock, BlockPos startPos) {
        Vec3 startPosBottom = new Vec3(
                startPos.getX() + (mob.getBbWidth() / 2.0),
                startPos.getY() + 0.5,
                startPos.getZ() + (mob.getBbWidth() / 2.0)
        );
        Vec3 startPosTop = new Vec3(
                startPos.getX() + (mob.getBbWidth() / 2.0),
                startPos.getY() + mob.getBbHeight() - 0.5,
                startPos.getZ() + (mob.getBbWidth() / 2.0)
        );
        
        Vec3 targetPos = new Vec3(
                targetBlock.getX() + 0.5,
                targetBlock.getY() + 0.5,
                targetBlock.getZ() + 0.5
        );
        
        return hasLineOfSight(world, startPosBottom, targetPos, targetBlock) ||
                hasLineOfSight(world, startPosTop, targetPos, targetBlock);
    }
    
    /**
     * Проверяет наличие прямой видимости между двумя точками
     */
    private boolean hasLineOfSight(Level world, Vec3 start, Vec3 end, BlockPos targetBlock) {
        double distance = start.distanceTo(end);
        Vec3 ray = end.subtract(start).normalize();
        
        for (double d = 0.30; d < distance - 0.30; d += 0.30) {
            Vec3 checkPoint = start.add(ray.scale(d));
            BlockPos checkPos = new BlockPos(
                    (int) Math.floor(checkPoint.x),
                    (int) Math.floor(checkPoint.y),
                    (int) Math.floor(checkPoint.z)
            );
            
            if (checkPos.equals(targetBlock)) {
                continue;
            }
            
            if (!world.getBlockState(checkPos).isAir()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Проверяет, можно ли пройти к блоку и доступен ли он
     */
    private boolean canPathToBlockAndIsAccessible(BlockPos blockPos) {
        if (mob == null || mob.level() == null || blockPos == null) {
            return false;
        }
        
        Level world = mob.level();
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return false;
        }
        
        Vec3 targetPos = target.position();
        Path blockPath = mob.getNavigation().createPath(blockPos, 0);
        if (blockPath == null || blockPath.getNodeCount() == 0) {
            return false;
        }
        
        Node endNode = blockPath.getEndNode();
        if (endNode == null) {
            return false;
        }
        Vec3 endPos = new Vec3(endNode.x, endNode.y, endNode.z);
        BlockPos endNodeBlockPos = new BlockPos(endNode.x, endNode.y, endNode.z);
        
        int[] heightRange = getMinYMaxYAtEndNode(endPos, targetPos);
        int minYatNode = heightRange[0];
        int maxYatNode = heightRange[1];
        
        int blockYRelativeToNode = blockPos.getY() - endNodeBlockPos.getY();
        if (blockYRelativeToNode < minYatNode || blockYRelativeToNode > maxYatNode) {
            return false;
        }
        
        double xDistance = Math.abs((endPos.x + mob.getBbWidth() / 2.0) - (blockPos.getX() + 0.5));
        double yDistance = Math.abs((endPos.y + mob.getBbHeight() / 2.0) - (blockPos.getY() + 0.5));
        double zDistance = Math.abs((endPos.z + mob.getBbWidth() / 2.0) - (blockPos.getZ() + 0.5));
        
        if (xDistance <= HORIZONTAL_MINING_RANGE && zDistance <= HORIZONTAL_MINING_RANGE && yDistance <= VERTICAL_MINING_RANGE) {
            return isBlockAccessibleFromDirection(world, blockPos, endNodeBlockPos);
        }
        
        return false;
    }
    
    /**
     * Проверяет, можно ли ломать блок
     */
    public static boolean isBreakableBlock(Level world, BlockPos pos) {
        if (world == null || pos == null) {
            return false;
        }
        
        try {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            
            // Проверяем hardness: < 0 означает неразрушимый блок
            float hardness = state.getDestroySpeed(world, pos);
            if (state.isAir() || hardness < 0 || hardness > 50) {
                return false;
            }
            
            // Проверяем теги
            if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) ||
                state.is(BlockTags.MINEABLE_WITH_AXE) ||
                state.is(BlockTags.MINEABLE_WITH_SHOVEL) ||
                state.is(BlockTags.DOORS) ||
                state.is(BlockTags.FENCES) ||
                state.is(BlockTags.WALLS)) {
                return true;
            }
            
            // Можно добавить проверку по конкретным блокам
            String blockName = block.getDescriptionId();
            return blockName.contains("glass") || blockName.contains("door") || blockName.contains("fence");
            
        } catch (Exception e) {
            System.err.println("Error checking block breakability at " + pos + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Вычисляет скорость разрушения блока для расчета прогресса ломания
     * 
     * Формула основана на hardness (твердости) блока:
     * - Более мягкие блоки (низкий hardness) ломаются быстрее
     * - MINING_SPEED умножается на коэффициент для баланса
     * - Для большинства блоков hardness находится в диапазоне 0.5-50
     * 
     * Примеры hardness:
     * - Земля/песок: 0.5
     * - Камень: 1.5
     * - Железо: 5.0
     * - Обсидиан: 50.0
     */
    private float getBlockStrength(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        float hardness = state.getDestroySpeed(world, pos);
        
        // Неразрушимые блоки (bedrock и т.д.)
        if (hardness < 0) return 0.0F;
        
        // Для очень твердых блоков уменьшаем скорость
        if (hardness > 50) return 0.0F;
        
        // Формула: чем выше hardness, тем медленнее ломание
        // Базовая скорость зависит от MINING_SPEED
        // Делим на hardness, чтобы более твердые блоки ломались медленнее
        float baseSpeed = MINING_SPEED * 0.1F; // Базовый множитель скорости
        float strength = baseSpeed / Math.max(hardness*0.7F, 0.5F); // Минимум 0.5 для избежания деления на 0
        
        return strength;
    }
    
    /**
     * Проверяет, находится ли блок в радиусе ломания
     */
    private boolean isWithinMiningRange(BlockPos blockPos) {
        if (blockPos == null) return false;
        
        BlockPos mobPos = mob.blockPosition();
        int xDiff = Math.abs(blockPos.getX() - mobPos.getX());
        int yDiff = Math.abs(blockPos.getY() - mobPos.getY());
        int zDiff = Math.abs(blockPos.getZ() - mobPos.getZ());
        
        if (xDiff > HORIZONTAL_MINING_RANGE + 2 ||
                zDiff > HORIZONTAL_MINING_RANGE + 2 ||
                yDiff > VERTICAL_MINING_RANGE + 2) {
            return false;
        }
        
        double mobCenterX = mob.getX() + mob.getBbWidth() / 2.0;
        double mobCenterY = mob.getY() + mob.getBbHeight() / 2.0;
        double mobCenterZ = mob.getZ() + mob.getBbWidth() / 2.0;
        
        double targetX = blockPos.getX() + 0.5;
        double targetY = blockPos.getY() + 0.5;
        double targetZ = blockPos.getZ() + 0.5;
        
        double xDistance = Math.abs(targetX - mobCenterX);
        double yDistance = Math.abs(targetY - mobCenterY);
        double zDistance = Math.abs(targetZ - mobCenterZ);
        
        double effectiveHorizontalRange = HORIZONTAL_MINING_RANGE + (miningTicks > 0 ? 1.0 : 0.0);
        double effectiveVerticalRange = VERTICAL_MINING_RANGE + (miningTicks > 0 ? 1.0 : 0.0);
        
        return xDistance <= effectiveHorizontalRange &&
                zDistance <= effectiveHorizontalRange &&
                yDistance <= effectiveVerticalRange;
    }
    
    /**
     * Выполняет ломание блока
     */
    private void performMining(Level world) {
        mob.getNavigation().stop();
        
        if (!mob.getNavigation().isDone() || mob.getNavigation().isInProgress()) {
            mob.setDeltaMovement(0, mob.getDeltaMovement().y, 0);
        }
        
        failedBlockFindings = 0;
        
        // Смотрим на блок во время ломания
        mob.getLookControl().setLookAt(
                targetBlock.getX() + 0.5,
                targetBlock.getY() + 0.5,
                targetBlock.getZ() + 0.5
        );
        
        // Анимация ломания
        mob.swing(InteractionHand.MAIN_HAND);
        miningTicks++;
        
        float strength = getBlockStrength(world, targetBlock);
        breakProgress += strength;
        
        // Обновляем прогресс ломания (0-10)
        int progress = (int) (breakProgress * 10.0F);
        world.destroyBlockProgress(mob.getId(), targetBlock, progress);
        

        
        // Звуки ломания
        if (miningTicks % 2 == 0) {
            BlockState blockState = world.getBlockState(targetBlock);
            net.minecraft.world.level.block.SoundType soundType = blockState.getSoundType();
            float volume = 0.7F;
            float pitch = 0.4F + world.random.nextFloat() * 0.4F;
            
            world.playSound(
                    null,
                    targetBlock,
                    soundType.getHitSound(),
                    SoundSource.NEUTRAL,
                    volume,
                    pitch
            );
        }
        
        // Завершаем ломание, если прогресс достиг 100%
        if (breakProgress >= 1.0F) {
            completeBlockBreak();
        }
    }
    
    /**
     * Навигация к целевому блоку
     */
    private void navigateToTargetBlock() {
        double targetX = targetBlock.getX() + 0.5;
        double targetY = targetBlock.getY() + 0.5;
        double targetZ = targetBlock.getZ() + 0.5;
        
        mob.getNavigation().moveTo(targetX, targetY, targetZ, 1.0);
        mob.getLookControl().setLookAt(targetX, targetY, targetZ);
        
        // Проверяем, не простаивает ли моб при поиске пути к блоку
        if ((mob.getNavigation().isDone() || !mob.getNavigation().isInProgress()) && targetBlock != null) {
            blockFindingIdleTicks++;
        } else {
            blockFindingIdleTicks = Math.max(0, blockFindingIdleTicks - 1);
        }
    }
    
    /**
     * Завершает ломание блока
     */
    private void completeBlockBreak() {
        if (mob == null || mob.level() == null || targetBlock == null) {
            return;
        }
        
        Level world = mob.level();
        world.destroyBlock(targetBlock, true);
        world.destroyBlockProgress(mob.getId(), targetBlock, -1);
        
        currentlyMining.remove(targetBlock);
        targetBlock = null;
        breakProgress = 0;
        failedBlockFindings = 0;
        blockFindingIdleTicks = 0;
    }
}
