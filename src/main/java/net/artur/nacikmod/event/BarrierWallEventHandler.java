package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.mana.IMana;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.item.BarrierWall;
import net.artur.nacikmod.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class BarrierWallEventHandler {
    private static final int DURATION_TICKS = 600; // 30 секунд = 30 * 20 тиков
    public static final int RESTORE_MANA_COST = 100;
    private static final double EFFECT_RADIUS = 20.0; // Радиус действия эффекта медлительности
    
    // Храним информацию о сломанных блоках для восстановления
    private static final Map<BlockPos, BrokenBlockInfo> brokenBlocks = new HashMap<>();
    
    private static class BrokenBlockInfo {
        final BlockPos pos;
        final UUID ownerUUID;
        final ItemStack itemStack;
        final int slot;
        final long breakTime;
        
        BrokenBlockInfo(BlockPos pos, UUID ownerUUID, ItemStack itemStack, int slot) {
            this.pos = pos;
            this.ownerUUID = ownerUUID;
            this.itemStack = itemStack;
            this.slot = slot;
            this.breakTime = System.currentTimeMillis();
        }
    }

    // Обработка разрушения блоков barrier_block и блоков в pending позициях
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        // Оставляем ТОЛЬКО логику восстановления уже существующего барьера
        if (state.is(ModBlocks.BARRIER_BLOCK.get())) {
            for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
                for (ItemStack stack : player.getInventory().items) {
                    if (!(stack.getItem() instanceof BarrierWall)) continue;
                    if (!BarrierWall.hasOwner(stack) || !BarrierWall.isOwner(stack, player)) continue;

                    List<BarrierWall.ActiveBarrierInfo> activeBarriers = BarrierWall.getActiveBarriers(stack, serverLevel);
                    for (BarrierWall.ActiveBarrierInfo barrierInfo : activeBarriers) {
                        // Используем эффективную проверку вместо contains() на большом списке
                        if (BarrierWall.isWallBlockPosition(pos, barrierInfo.barrierPos, BarrierWall.BARRIER_RADIUS)) {
                            brokenBlocks.put(pos, new BrokenBlockInfo(pos, barrierInfo.ownerUUID, stack, barrierInfo.slot));
                            return;
                        }
                    }
                }
            }
        }

        // ЛОГИКУ ПРОВЕРКИ PendingPositions ОТСЮДА УДАЛЯЕМ ПОЛНОСТЬЮ.
        // Она мешает стандартному разрушению блока земли.
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Чтобы барьер появлялся быстрее после поломки земли,
        // можно проверять PendingPositions чаще (например, каждые 5 тиков = 0.25 сек)
        boolean checkPending = event.getServer().getTickCount() % 5 == 0;
        boolean checkFull = event.getServer().getTickCount() % 20 == 0;

        if (!checkPending && !checkFull) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (checkFull) {
                restoreBrokenBlocks(level);
            }

            // Выносим обработку активных барьеров
            processActiveBarriers(level, checkPending, checkFull);
        }
    }

    private static void processActiveBarriers(ServerLevel level, boolean checkPending, boolean checkFull) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            for (ItemStack stack : player.getInventory().items) {
                if (!(stack.getItem() instanceof BarrierWall)) continue;

                List<BarrierWall.ActiveBarrierInfo> activeBarriers = BarrierWall.getActiveBarriers(stack, level);
                for (BarrierWall.ActiveBarrierInfo barrierInfo : activeBarriers) {

                    // 1. Попытка поставить блоки в пустые места (теперь работает чаще)
                    if (checkPending) {
                        tryPlacePendingBlocks(level, stack, barrierInfo);
                    }

                    // 2. Проверка времени жизни (раз в секунду)
                    if (checkFull) {
                        long currentTime = level.getGameTime();
                        if (currentTime - barrierInfo.activeTime >= DURATION_TICKS) {
                            removeBarrierWalls(level, barrierInfo);
                            deactivateBarrier(level, barrierInfo.barrierPos, stack, barrierInfo.slot);
                        }
                    }
                }
            }
        }
    }

    private static void restoreBrokenBlocks(ServerLevel level) {
        List<BlockPos> toRemove = new ArrayList<>();
        
        for (Map.Entry<BlockPos, BrokenBlockInfo> entry : brokenBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BrokenBlockInfo info = entry.getValue();
            
            // Проверяем, что позиция все еще в этом уровне
            if (!level.isLoaded(pos)) {
                toRemove.add(pos);
                continue;
            }
            
            // Если блок уже восстановлен, удаляем из списка
            if (level.getBlockState(pos).is(ModBlocks.BARRIER_BLOCK.get())) {
                toRemove.add(pos);
                continue;
            }
            
            // Проверяем, что блок действительно сломан (не воздух и не другой блок)
            BlockState currentState = level.getBlockState(pos);
            if (!currentState.isAir() && !currentState.canBeReplaced()) {
                // Блок заменен на другой - не восстанавливаем
                toRemove.add(pos);
                continue;
            }
            
            // Ищем владельца
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(info.ownerUUID);
            if (owner == null) {
                toRemove.add(pos);
                continue;
            }
            
            // Проверяем, что барьер все еще активен
            List<BarrierWall.ActiveBarrierInfo> activeBarriers = BarrierWall.getActiveBarriers(info.itemStack, level);
            boolean barrierStillActive = false;
            for (BarrierWall.ActiveBarrierInfo barrierInfo : activeBarriers) {
                if (barrierInfo.slot == info.slot && 
                    BarrierWall.isWallBlockPosition(pos, barrierInfo.barrierPos, BarrierWall.BARRIER_RADIUS)) {
                    barrierStillActive = true;
                    break;
                }
            }
            
            if (!barrierStillActive) {
                // Барьер больше не активен - не восстанавливаем
                toRemove.add(pos);
                continue;
            }
            
            // Проверяем ману
            LazyOptional<IMana> manaCap = owner.getCapability(ManaProvider.MANA_CAPABILITY);
            if (!manaCap.isPresent()) {
                toRemove.add(pos);
                continue;
            }
            
            IMana mana = manaCap.orElseThrow(IllegalStateException::new);
            if (mana.getMana() < RESTORE_MANA_COST) {
                // Не хватает маны - блок не восстанавливается, но оставляем в списке для следующей попытки
                continue;
            }
            
            // Восстанавливаем блок
            // Блок сам планирует тик для автоудаления через onPlace()
            level.setBlock(pos, ModBlocks.BARRIER_BLOCK.get().defaultBlockState(), 3);
            mana.removeMana(RESTORE_MANA_COST);
            
            toRemove.add(pos);
        }
        
        // Удаляем восстановленные блоки из списка
        for (BlockPos pos : toRemove) {
            brokenBlocks.remove(pos);
        }
    }

    private static void processActiveBarriers(ServerLevel level) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level) continue;
            
            for (ItemStack stack : player.getInventory().items) {
                if (!(stack.getItem() instanceof BarrierWall)) continue;
                if (!BarrierWall.hasOwner(stack) || !BarrierWall.isOwner(stack, player)) continue;
                
                List<BarrierWall.ActiveBarrierInfo> activeBarriers = BarrierWall.getActiveBarriers(stack, level);
                
                for (BarrierWall.ActiveBarrierInfo barrierInfo : activeBarriers) {
                    // Пытаемся дозаставить pending‑позиции, если блоки стали доступны
                    tryPlacePendingBlocks(level, stack, barrierInfo);

                    long currentTime = level.getGameTime();
                    long elapsed = currentTime - barrierInfo.activeTime;
                    
                    // Проверяем, истек ли барьер (30 секунд)
                    if (elapsed >= DURATION_TICKS) {
                        // Удаляем все блоки стен
                        removeBarrierWalls(level, barrierInfo);
                        
                        // Деактивируем барьер в NBT
                        deactivateBarrier(level, barrierInfo.barrierPos, stack, barrierInfo.slot);
                        
                        // Уведомляем владельца
                        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(barrierInfo.ownerUUID);
                        if (owner != null) {
                            owner.sendSystemMessage(Component.literal("Barrier Wall in slot " + (barrierInfo.slot + 1) + " has expired!")
                                    .withStyle(ChatFormatting.YELLOW));
                        }
                    }
                }
            }
        }
    }

    /**
     * Пытается превратить pending‑позиции в настоящие блоки barier_block,
     * если теперь там воздух или заменяемый блок.
     */
    private static void tryPlacePendingBlocks(ServerLevel level, ItemStack stack, BarrierWall.ActiveBarrierInfo barrierInfo) {
        if (!stack.hasTag()) return;

        CompoundTag tag = stack.getTag();
        ListTag barrierList = tag.getList(BarrierWall.BARRIER_POSITIONS_TAG, net.minecraft.nbt.Tag.TAG_COMPOUND);
        if (barrierInfo.slot >= barrierList.size()) return;

        CompoundTag barrierTag = barrierList.getCompound(barrierInfo.slot);
        if (!barrierTag.contains("PendingPositions")) return;

        ListTag pendingTag = barrierTag.getList("PendingPositions", net.minecraft.nbt.Tag.TAG_COMPOUND);
        if (pendingTag.isEmpty()) return;

        List<Integer> toRemove = new ArrayList<>();

        for (int i = 0; i < pendingTag.size(); i++) {
            CompoundTag pendingPosTag = pendingTag.getCompound(i);
            BlockPos pendingPos = new BlockPos(
                    pendingPosTag.getInt("x"),
                    pendingPosTag.getInt("y"),
                    pendingPosTag.getInt("z")
            );

            if (!level.isLoaded(pendingPos)) continue;

            BlockState currentState = level.getBlockState(pendingPos);
            if (currentState.isAir() || currentState.canBeReplaced()) {
                // Ставим блок барьера
                level.setBlock(pendingPos, ModBlocks.BARRIER_BLOCK.get().defaultBlockState(), 3);

                // НЕ добавляем в NBT - позиции вычисляются динамически
                // Просто добавляем в runtime‑список wallBlocks для текущей сессии
                if (!barrierInfo.wallBlocks.contains(pendingPos)) {
                    barrierInfo.wallBlocks.add(pendingPos);
                }

                toRemove.add(i);
            }
        }

        // Удаляем обработанные pending‑позиции (с конца, чтобы индексы не съехали)
        for (int i = toRemove.size() - 1; i >= 0; i--) {
            pendingTag.remove((int) toRemove.get(i));
        }

        if (pendingTag.isEmpty()) {
            barrierTag.remove("PendingPositions");
        } else {
            barrierTag.put("PendingPositions", pendingTag);
        }

        barrierList.set(barrierInfo.slot, barrierTag);
        tag.put(BarrierWall.BARRIER_POSITIONS_TAG, barrierList);
    }

    /**
     * Обновляет эффект EffectBaseDomain для сущностей внутри барьера
     */
    private static void updateBaseDomainEffect(ServerLevel level, BarrierWall.ActiveBarrierInfo barrierInfo) {
        double radius = 20.0;
        BlockPos barrierCenter = barrierInfo.barrierPos;
        
        AABB aabb = new AABB(
                barrierCenter.getX() - radius,
                barrierCenter.getY() - radius,
                barrierCenter.getZ() - radius,
                barrierCenter.getX() + radius,
                barrierCenter.getY() + radius,
                barrierCenter.getZ() + radius
        );
        
        // Находим все сущности в области барьера
        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                aabb,
                entity -> entity.isAlive() && !entity.getUUID().equals(barrierInfo.ownerUUID)
        );
        
        // Обновляем эффект для каждой сущности
        for (LivingEntity entity : entities) {
            // Проверяем, что сущность действительно внутри барьера
            double distanceSq = entity.blockPosition().distSqr(barrierCenter);
            if (distanceSq <= radius * radius) {
                // Проверяем, есть ли уже эффект
                if (entity.hasEffect(net.artur.nacikmod.registry.ModEffects.EFFECT_BASE_DOMAIN.get())) {
                    // Обновляем эффект (продлеваем на 2 секунды)
                    entity.addEffect(new MobEffectInstance(
                            net.artur.nacikmod.registry.ModEffects.EFFECT_BASE_DOMAIN.get(),
                            40, // 2 секунды
                            0,
                            false,
                            false,
                            true
                    ));
                    
                    // Обновляем позицию барьера в root capability
                    entity.getCapability(net.artur.nacikmod.capability.root.RootProvider.ROOT_CAPABILITY).ifPresent(data -> {
                        data.setPendingData(barrierCenter, level.dimension());
                        data.commitData();
                    });
                } else {
                    // Накладываем эффект впервые
                    entity.addEffect(new MobEffectInstance(
                            net.artur.nacikmod.registry.ModEffects.EFFECT_BASE_DOMAIN.get(),
                            40, // 2 секунды
                            0,
                            false,
                            false,
                            true
                    ));
                    
                    // Устанавливаем позицию барьера в root capability
                    entity.getCapability(net.artur.nacikmod.capability.root.RootProvider.ROOT_CAPABILITY).ifPresent(data -> {
                        data.setPendingData(barrierCenter, level.dimension());
                        data.forceCommitData();
                    });
                }
            }
        }
    }

    private static void removeBarrierWalls(ServerLevel level, BarrierWall.ActiveBarrierInfo barrierInfo) {
        // Используем динамическое вычисление позиций для удаления всех блоков барьера
        // Это гарантирует удаление всех блоков, даже если список wallBlocks неполный
        List<BlockPos> allWallPositions = BarrierWall.calculateWallBlockPositions(
            barrierInfo.barrierPos, BarrierWall.BARRIER_RADIUS);
        
        for (BlockPos wallPos : allWallPositions) {
            if (level.isLoaded(wallPos) && level.getBlockState(wallPos).is(ModBlocks.BARRIER_BLOCK.get())) {
                level.removeBlock(wallPos, false);
            }
            // Удаляем из списка сломанных блоков, если там есть
            brokenBlocks.remove(wallPos);
        }
    }

    private static void deactivateBarrier(ServerLevel level, BlockPos center, ItemStack stack, int slot) {
        if (!stack.hasTag()) return;
        
        CompoundTag tag = stack.getTag();
        ListTag positions = tag.getList("BarrierPositions", 10);
        
        if (slot >= positions.size()) return;
        
        CompoundTag barrierTag = positions.getCompound(slot);
        barrierTag.putBoolean("BarrierActive", false);
        barrierTag.remove("BarrierWallBlocks"); // Удаляем старый список блоков (для обратной совместимости)
        barrierTag.remove("PendingPositions"); // Удаляем pending позиции при деактивации
        positions.set(slot, barrierTag);
        tag.put("BarrierPositions", positions);

        // Отключаем эффекты стены на BlockEntity
        var be = level.getBlockEntity(center);
        if (be instanceof net.artur.nacikmod.block.entity.BarrierBlockEntity barrierBe) {
            barrierBe.updateWallState(barrierTag.getUUID("owner"), false, slot);
        }
    }
}

