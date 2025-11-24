package net.artur.nacikmod.item;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.mana.IMana;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class BarrierSeal extends Item {
    private static final int SLOT_COUNT = 3;
    private static final int MANA_COST_PER_BARRIER_PER_SECOND = 2;
    private static final String BARRIER_POSITIONS_TAG = "BarrierPositions";
    private static final String BARRIER_ACTIVE_TAG = "BarrierActive";
    private static final String BARRIER_OWNER_TAG = "BarrierOwner";
    private static final String SELECTED_SLOT_TAG = "SelectedSlot";
    private static final String OWNER_TAG = "Owner";
    private static final String OWNER_NAME_TAG = "OwnerName";
    private static final String JUST_ADDED_TAG = "JustAdded"; // Флаг для предотвращения конфликта use/useOn

    public BarrierSeal(Properties properties) {
        super(properties);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        setOwner(stack, player);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide && entity instanceof Player player) {
            if (!hasOwner(stack)) {
                setOwner(stack, player);
            }
        }
    }

    public static void setOwner(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID(OWNER_TAG, player.getUUID());
        tag.putString(OWNER_NAME_TAG, player.getGameProfile().getName());
    }

    public static boolean hasOwner(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(OWNER_TAG);
    }

    public static boolean isOwner(ItemStack stack, Player player) {
        if (!hasOwner(stack)) return false;
        return stack.getTag().getUUID(OWNER_TAG).equals(player.getUUID());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (level.isClientSide || player == null) {
            return InteractionResult.PASS;
        }

        if (!isOwner(stack, player)) {
            player.sendSystemMessage(Component.literal("This Barrier Seal belongs to " + stack.getTag().getString(OWNER_NAME_TAG))
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        BlockState state = level.getBlockState(pos);
        
        // Добавление барьера в слот только при Ctrl + Shift + ПКМ
        boolean isCtrlShift = player.isShiftKeyDown() && player.isCrouching();
        
        // Если кликнули на блок барьера с Ctrl + Shift - добавляем в слот
        if (state.is(ModBlocks.BARRIER.get()) && isCtrlShift) {
            InteractionResult result = addBarrierToSlot(stack, player, pos);
            if (result == InteractionResult.SUCCESS) {
                // Устанавливаем флаг, чтобы предотвратить вызов use()
                CompoundTag tag = stack.getOrCreateTag();
                tag.putBoolean(JUST_ADDED_TAG, true);
                return InteractionResult.CONSUME;
            }
            return result;
        }

        // Если кликнули на другой блок или без Ctrl+Shift - ничего не делаем
        if (state.is(ModBlocks.BARRIER.get()) && !isCtrlShift) {
            player.sendSystemMessage(Component.literal("Hold Ctrl + Shift and right-click on a barrier block to add it to a slot")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        // Проверяем флаг - если только что добавили барьер, игнорируем use()
        if (stack.hasTag() && stack.getTag().getBoolean(JUST_ADDED_TAG)) {
            stack.getTag().remove(JUST_ADDED_TAG);
            return InteractionResultHolder.pass(stack);
        }

        if (!isOwner(stack, player)) {
            player.sendSystemMessage(Component.literal("This Barrier Seal belongs to " + stack.getTag().getString(OWNER_NAME_TAG))
                    .withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(stack);
        }

        if (player.isShiftKeyDown()) {
            // Shift + ПКМ = переключение слотов
            switchSlot(stack, player);
            return InteractionResultHolder.success(stack);
        } else {
            // Обычный ПКМ = включение/отключение барьера в выбранном слоте
            toggleBarrier(stack, player);
            return InteractionResultHolder.success(stack);
        }
    }

    private InteractionResult addBarrierToSlot(ItemStack stack, Player player, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        int slot = getSelectedSlot(tag);
        
        // Проверяем, не добавлен ли уже этот блок в другой слот
        ListTag positions = tag.getList(BARRIER_POSITIONS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < positions.size(); i++) {
            if (i == slot) continue; // Пропускаем текущий слот (можно перезаписать)
            
            CompoundTag barrierTag = positions.getCompound(i);
            if (barrierTag.contains("x") && barrierTag.contains("y") && barrierTag.contains("z")) {
                BlockPos existingPos = new BlockPos(
                        barrierTag.getInt("x"),
                        barrierTag.getInt("y"),
                        barrierTag.getInt("z")
                );
                if (existingPos.equals(pos)) {
                    player.sendSystemMessage(Component.literal("This barrier is already in slot " + (i + 1))
                            .withStyle(ChatFormatting.RED));
                    return InteractionResult.FAIL;
                }
            }
        }
        
        saveBarrierPosition(stack, slot, pos, player);
        
        player.sendSystemMessage(Component.literal("Barrier added to slot " + (slot + 1))
                .withStyle(ChatFormatting.GREEN));
        return InteractionResult.SUCCESS;
    }

    private InteractionResult switchSlot(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        int currentSlot = getSelectedSlot(tag);
        currentSlot = (currentSlot + 1) % SLOT_COUNT;
        setSelectedSlot(tag, currentSlot);
        
        displayBarriersList(player, stack);
        return InteractionResult.SUCCESS;
    }

    private void toggleBarrier(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        int slot = getSelectedSlot(tag);
        
        ListTag positions = tag.getList(BARRIER_POSITIONS_TAG, Tag.TAG_COMPOUND);
        if (slot >= positions.size()) {
            player.sendSystemMessage(Component.literal("No barrier in slot " + (slot + 1))
                    .withStyle(ChatFormatting.RED));
            return;
        }

        CompoundTag barrierTag = positions.getCompound(slot);
        if (!barrierTag.contains("x") || !barrierTag.contains("y") || !barrierTag.contains("z")) {
            player.sendSystemMessage(Component.literal("No barrier in slot " + (slot + 1))
                    .withStyle(ChatFormatting.RED));
            return;
        }

        boolean isActive = barrierTag.getBoolean(BARRIER_ACTIVE_TAG);
        barrierTag.putBoolean(BARRIER_ACTIVE_TAG, !isActive);
        positions.set(slot, barrierTag);
        tag.put(BARRIER_POSITIONS_TAG, positions);

        player.sendSystemMessage(Component.literal("Barrier in slot " + (slot + 1) + " " + (!isActive ? "activated" : "deactivated"))
                .withStyle(!isActive ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private void saveBarrierPosition(ItemStack stack, int slot, BlockPos pos, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag positions = tag.getList(BARRIER_POSITIONS_TAG, Tag.TAG_COMPOUND);
        
        while (positions.size() <= slot) {
            positions.add(new CompoundTag());
        }

        CompoundTag barrierTag = new CompoundTag();
        barrierTag.putInt("x", pos.getX());
        barrierTag.putInt("y", pos.getY());
        barrierTag.putInt("z", pos.getZ());
        barrierTag.putBoolean(BARRIER_ACTIVE_TAG, false); // По умолчанию неактивен
        barrierTag.putUUID(BARRIER_OWNER_TAG, player.getUUID());
        
        positions.set(slot, barrierTag);
        tag.put(BARRIER_POSITIONS_TAG, positions);
    }

    private int getSelectedSlot(CompoundTag tag) {
        return tag.contains(SELECTED_SLOT_TAG) ? tag.getInt(SELECTED_SLOT_TAG) : 0;
    }

    private void setSelectedSlot(CompoundTag tag, int slot) {
        if (slot >= 0 && slot < SLOT_COUNT) {
            tag.putInt(SELECTED_SLOT_TAG, slot);
        }
    }

    private void displayBarriersList(Player player, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;

        int selectedSlot = getSelectedSlot(tag);
        ListTag positions = tag.getList(BARRIER_POSITIONS_TAG, Tag.TAG_COMPOUND);

        player.sendSystemMessage(Component.literal("Barrier slots:")
                .withStyle(ChatFormatting.GREEN));

        for (int i = 0; i < SLOT_COUNT; i++) {
            String prefix = i == selectedSlot ? "> " : "  ";
            String status = "Empty";

            if (i < positions.size()) {
                CompoundTag barrierTag = positions.getCompound(i);
                if (barrierTag.contains("x") && barrierTag.contains("y") && barrierTag.contains("z")) {
                    BlockPos pos = new BlockPos(
                            barrierTag.getInt("x"),
                            barrierTag.getInt("y"),
                            barrierTag.getInt("z")
                    );
                    boolean isActive = barrierTag.getBoolean(BARRIER_ACTIVE_TAG);
                    status = String.format("(%d, %d, %d) - %s", pos.getX(), pos.getY(), pos.getZ(),
                            isActive ? "Active" : "Inactive");
                }
            }

            player.sendSystemMessage(Component.literal(prefix + (i + 1) + ". " + status)
                    .withStyle(i == selectedSlot ? ChatFormatting.YELLOW : ChatFormatting.WHITE));
        }
    }

    public static List<BlockPos> getBarrierPositions(ItemStack stack) {
        List<BlockPos> positions = new ArrayList<>();
        if (stack == null || !stack.hasTag()) return positions;

        CompoundTag tag = stack.getTag();
        ListTag barrierList = tag.getList(BARRIER_POSITIONS_TAG, Tag.TAG_COMPOUND);

        for (int i = 0; i < barrierList.size(); i++) {
            CompoundTag barrierTag = barrierList.getCompound(i);
            if (barrierTag.contains("x") && barrierTag.contains("y") && barrierTag.contains("z")) {
                boolean isActive = barrierTag.getBoolean(BARRIER_ACTIVE_TAG);
                if (isActive) {
                    positions.add(new BlockPos(
                            barrierTag.getInt("x"),
                            barrierTag.getInt("y"),
                            barrierTag.getInt("z")
                    ));
                }
            }
        }

        return positions;
    }

    public static BlockPos getBarrierPosition(ItemStack stack, int slot) {
        if (stack == null || !stack.hasTag()) return null;
        CompoundTag tag = stack.getTag();
        ListTag positions = tag.getList(BARRIER_POSITIONS_TAG, Tag.TAG_COMPOUND);
        if (slot >= positions.size()) return null;
        
        CompoundTag barrierTag = positions.getCompound(slot);
        if (!barrierTag.contains("x") || !barrierTag.contains("y") || !barrierTag.contains("z")) {
            return null;
        }
        
        return new BlockPos(
                barrierTag.getInt("x"),
                barrierTag.getInt("y"),
                barrierTag.getInt("z")
        );
    }

    public static boolean isBarrierActive(ItemStack stack, int slot) {
        if (stack == null || !stack.hasTag()) return false;
        CompoundTag tag = stack.getTag();
        ListTag positions = tag.getList(BARRIER_POSITIONS_TAG, Tag.TAG_COMPOUND);
        if (slot >= positions.size()) return false;
        
        CompoundTag barrierTag = positions.getCompound(slot);
        return barrierTag.getBoolean(BARRIER_ACTIVE_TAG);
    }

    public static UUID getBarrierOwner(ItemStack stack, int slot) {
        if (stack == null || !stack.hasTag()) return null;
        CompoundTag tag = stack.getTag();
        ListTag positions = tag.getList(BARRIER_POSITIONS_TAG, Tag.TAG_COMPOUND);
        if (slot >= positions.size()) return null;
        
        CompoundTag barrierTag = positions.getCompound(slot);
        if (!barrierTag.contains(BARRIER_OWNER_TAG)) return null;
        
        return barrierTag.getUUID(BARRIER_OWNER_TAG);
    }

    public static void removeBarrier(ItemStack stack, BlockPos pos) {
        if (stack == null || !stack.hasTag()) return;
        CompoundTag tag = stack.getTag();
        ListTag positions = tag.getList(BARRIER_POSITIONS_TAG, Tag.TAG_COMPOUND);

        for (int i = 0; i < positions.size(); i++) {
            CompoundTag barrierTag = positions.getCompound(i);
            if (barrierTag.contains("x") && barrierTag.contains("y") && barrierTag.contains("z")) {
                BlockPos barrierPos = new BlockPos(
                        barrierTag.getInt("x"),
                        barrierTag.getInt("y"),
                        barrierTag.getInt("z")
                );
                if (barrierPos.equals(pos)) {
                    positions.set(i, new CompoundTag()); // Очищаем слот
                    tag.put(BARRIER_POSITIONS_TAG, positions);
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        // Проверяем все BarrierSeal в инвентаре игрока
        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof BarrierSeal)) continue;
            if (!hasOwner(stack) || !isOwner(stack, player)) continue;

            List<BlockPos> activeBarriers = getBarrierPositions(stack);
            if (activeBarriers.isEmpty()) continue;

            // Тратим ману каждую секунду
            if (player.tickCount % 20 == 0) {
                int totalCost = activeBarriers.size() * MANA_COST_PER_BARRIER_PER_SECOND;
                
                LazyOptional<IMana> manaCap = player.getCapability(ManaProvider.MANA_CAPABILITY);
                if (!manaCap.isPresent()) continue;

                IMana mana = manaCap.orElseThrow(IllegalStateException::new);
                if (mana.getMana() < totalCost) {
                    // Не хватает маны - отключаем все барьеры
                    deactivateAllBarriers(stack, player);
                    player.sendSystemMessage(Component.literal("Not enough mana! All barriers deactivated.")
                            .withStyle(ChatFormatting.RED));
                } else {
                    mana.removeMana(totalCost);
                }
            }
        }
    }

    private static void deactivateAllBarriers(ItemStack stack, Player player) {
        if (stack == null || !stack.hasTag()) return;
        CompoundTag tag = stack.getTag();
        ListTag positions = tag.getList(BARRIER_POSITIONS_TAG, Tag.TAG_COMPOUND);

        for (int i = 0; i < positions.size(); i++) {
            CompoundTag barrierTag = positions.getCompound(i);
            if (barrierTag.contains("x")) {
                barrierTag.putBoolean(BARRIER_ACTIVE_TAG, false);
                positions.set(i, barrierTag);
            }
        }
        tag.put(BARRIER_POSITIONS_TAG, positions);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (hasOwner(stack)) {
            tooltip.add(Component.translatable("item.owner", stack.getTag().getString(OWNER_NAME_TAG))
                    .withStyle(ChatFormatting.GOLD));
        }

        tooltip.add(Component.translatable("item.nacikmod.barrier_seal.desc1"));
        tooltip.add(Component.translatable("item.nacikmod.barrier_seal.desc2", MANA_COST_PER_BARRIER_PER_SECOND)
                .withStyle(style -> style.withColor(0x00FFFF)));

        CompoundTag tag = stack.getTag();
        if (tag == null) {
            tooltip.add(Component.literal("No barriers set").withStyle(ChatFormatting.GRAY));
            return;
        }

        int selectedSlot = getSelectedSlot(tag);
        ListTag positions = tag.getList(BARRIER_POSITIONS_TAG, Tag.TAG_COMPOUND);

        tooltip.add(Component.literal("Barriers:").withStyle(ChatFormatting.GREEN));

        for (int i = 0; i < SLOT_COUNT; i++) {
            String prefix = i == selectedSlot ? "> " : "  ";
            String status = "Empty";

            if (i < positions.size()) {
                CompoundTag barrierTag = positions.getCompound(i);
                if (barrierTag.contains("x") && barrierTag.contains("y") && barrierTag.contains("z")) {
                    BlockPos pos = new BlockPos(
                            barrierTag.getInt("x"),
                            barrierTag.getInt("y"),
                            barrierTag.getInt("z")
                    );
                    boolean isActive = barrierTag.getBoolean(BARRIER_ACTIVE_TAG);
                    status = String.format("(%d, %d, %d) - %s", pos.getX(), pos.getY(), pos.getZ(),
                            isActive ? "Active" : "Inactive");
                }
            }

            tooltip.add(Component.literal(prefix + (i + 1) + ". " + status)
                    .withStyle(i == selectedSlot ? ChatFormatting.YELLOW : ChatFormatting.WHITE));
        }
        tooltip.add(Component.translatable("item.nacikmod.barrier_wall.desc3")
                .withStyle(ChatFormatting.GRAY));
    }
}

