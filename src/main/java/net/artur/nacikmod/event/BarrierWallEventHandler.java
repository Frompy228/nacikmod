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

    // Обработка разрушения блоков barrier_block
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        BlockState state = event.getState();
        if (!state.is(ModBlocks.BARRIER_BLOCK.get())) return;

        BlockPos pos = event.getPos();
        
        // Ищем активный барьер, которому принадлежит этот блок
        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            for (ItemStack stack : player.getInventory().items) {
                if (!(stack.getItem() instanceof BarrierWall)) continue;
                if (!BarrierWall.hasOwner(stack) || !BarrierWall.isOwner(stack, player)) continue;
                
                List<BarrierWall.ActiveBarrierInfo> activeBarriers = BarrierWall.getActiveBarriers(stack, serverLevel);
                for (BarrierWall.ActiveBarrierInfo barrierInfo : activeBarriers) {
                    if (barrierInfo.wallBlocks.contains(pos)) {
                        // Сохраняем информацию о сломанном блоке для восстановления
                        // Блок будет восстановлен в следующем тике, если у владельца есть мана
                        brokenBlocks.put(pos, new BrokenBlockInfo(pos, barrierInfo.ownerUUID, stack, barrierInfo.slot));
                        return;
                    }
                }
            }
        }
    }

    // Обработка тиков сервера - восстановление блоков, удаление истекших барьеров, применение эффектов
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        // Проверяем каждую секунду
        if (event.getServer().getTickCount() % 20 != 0) return;

        // Обрабатываем все уровни
        for (ServerLevel level : event.getServer().getAllLevels()) {
            // Восстановление сломанных блоков
            restoreBrokenBlocks(level);
            
            // Удаление истекших барьеров и применение эффектов
            processActiveBarriers(level);
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
                if (barrierInfo.slot == info.slot && barrierInfo.wallBlocks.contains(pos)) {
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
                    long currentTime = level.getGameTime();
                    long elapsed = currentTime - barrierInfo.activeTime;
                    
                    // Проверяем, истек ли барьер (30 секунд)
                    if (elapsed >= DURATION_TICKS) {
                        // Удаляем все блоки стен
                        removeBarrierWalls(level, barrierInfo);
                        
                        // Деактивируем барьер в NBT
                        deactivateBarrier(stack, barrierInfo.slot);
                        
                        // Уведомляем владельца
                        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(barrierInfo.ownerUUID);
                        if (owner != null) {
                            owner.sendSystemMessage(Component.literal("Barrier Wall in slot " + (barrierInfo.slot + 1) + " has expired!")
                                    .withStyle(ChatFormatting.YELLOW));
                        }
                    } else {
                        // Применяем эффект медлительности на всех сущностей кроме владельца
                        applySlownessEffect(level, barrierInfo);
                    }
                }
            }
        }
    }

    private static void removeBarrierWalls(ServerLevel level, BarrierWall.ActiveBarrierInfo barrierInfo) {
        for (BlockPos wallPos : barrierInfo.wallBlocks) {
            if (level.getBlockState(wallPos).is(ModBlocks.BARRIER_BLOCK.get())) {
                level.removeBlock(wallPos, false);
            }
            // Удаляем из списка сломанных блоков, если там есть
            brokenBlocks.remove(wallPos);
        }
    }

    private static void deactivateBarrier(ItemStack stack, int slot) {
        if (!stack.hasTag()) return;
        
        CompoundTag tag = stack.getTag();
        ListTag positions = tag.getList("BarrierPositions", 10);
        
        if (slot >= positions.size()) return;
        
        CompoundTag barrierTag = positions.getCompound(slot);
        barrierTag.putBoolean("BarrierActive", false);
        barrierTag.remove("BarrierWallBlocks"); // Удаляем список блоков
        positions.set(slot, barrierTag);
        tag.put("BarrierPositions", positions);
    }

    private static void applySlownessEffect(ServerLevel level, BarrierWall.ActiveBarrierInfo barrierInfo) {
        // Создаем AABB вокруг барьера для поиска сущностей
        AABB effectBox = new AABB(barrierInfo.barrierPos)
                .inflate(EFFECT_RADIUS);
        
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, effectBox);
        
        for (LivingEntity entity : entities) {
            // Пропускаем владельца
            if (entity.getUUID().equals(barrierInfo.ownerUUID)) continue;
            
            // Применяем эффект медлительности (уровень 2, длительность 2 секунды)
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false, true));
        }
    }
}

