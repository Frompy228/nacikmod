package net.artur.nacikmod.item;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.block.entity.BarrierBlockEntity;
import net.artur.nacikmod.capability.mana.IMana;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModBlocks;
import net.artur.nacikmod.util.PlayerCooldowns;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.artur.nacikmod.event.BarrierWallEventHandler.RESTORE_MANA_COST;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class BarrierWall extends Item {
    private static final int SLOT_COUNT = 2;
    private static final int ACTIVATION_MANA_COST = 3000;
    private static final int COOLDOWN_TICKS = 2400; // 2 минуты = 120 секунд * 20 тиков
    public static final int BARRIER_RADIUS = 20; // Радиус барьера
    public static final String BARRIER_POSITIONS_TAG = "BarrierPositions";
    private static final String BARRIER_ACTIVE_TAG = "BarrierActive";
    private static final String BARRIER_ACTIVE_TIME_TAG = "BarrierActiveTime";
    public static final String BARRIER_WALL_BLOCKS_TAG = "BarrierWallBlocks"; // Оставлено для обратной совместимости, но больше не используется
    private static final String SELECTED_SLOT_TAG = "SelectedSlot";
    private static final String OWNER_TAG = "Owner";
    private static final String OWNER_NAME_TAG = "OwnerName";
    private static final String JUST_ADDED_TAG = "JustAdded"; // Флаг для предотвращения конфликта use/useOn

    public BarrierWall(Properties properties) {
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
            player.sendSystemMessage(Component.literal("This Barrier Wall belongs to " + stack.getTag().getString(OWNER_NAME_TAG))
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        BlockState state = level.getBlockState(pos);

        /*
         * Аналогично BarrierSeal:
         * Сервер не может надёжно узнать состояние Ctrl, поэтому используем Shift
         * как "комбо" для добавления барьера в слот именно по самому блоку барьера.
         * Конфликта с use() нет благодаря JUST_ADDED_TAG.
         */
        boolean isAddCombo = player.isShiftKeyDown();
        
        // Если кликнули на блок барьера с Shift - добавляем в слот
        if (state.is(ModBlocks.BARRIER.get()) && isAddCombo) {
            InteractionResult result = addBarrierToSlot(stack, player, pos);
            if (result == InteractionResult.SUCCESS) {
                // Устанавливаем флаг, чтобы предотвратить вызов use()
                CompoundTag tag = stack.getOrCreateTag();
                tag.putBoolean(JUST_ADDED_TAG, true);
                return InteractionResult.CONSUME;
            }
            return result;
        }

        // Если кликнули на блок барьера без Shift - подсказка
        if (state.is(ModBlocks.BARRIER.get()) && !isAddCombo) {
            player.sendSystemMessage(Component.literal("Hold Shift and right-click on a barrier block to add it to a slot")
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

        // Если смотрим на блок барьера, не переключаем слоты и не активируем предмет.
        // Добавление по самому барьеру обрабатывается в useOn().
        HitResult hit = player.pick(5.0D, 0.0F, false);
        if (hit instanceof BlockHitResult blockHit) {
            BlockPos hitPos = blockHit.getBlockPos();
            if (level.getBlockState(hitPos).is(ModBlocks.BARRIER.get())) {
                return InteractionResultHolder.pass(stack);
            }
        }

        if (!isOwner(stack, player)) {
            player.sendSystemMessage(Component.literal("This Barrier Wall belongs to " + stack.getTag().getString(OWNER_NAME_TAG))
                    .withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(stack);
        }

        // Здесь также используем только Shift:
        // - Shift + ПКМ (не по барьеру) = переключить слот
        // - ПКМ без Shift = активировать барьер
        // Добавление по самому блоку барьера обрабатывается в useOn().
        if (player.isShiftKeyDown()) {
            // Shift + ПКМ = переключение слотов
            switchSlot(stack, player);
            return InteractionResultHolder.success(stack);
        } else {
            // Обычный ПКМ = активация барьера
            activateBarrier(stack, player, level);
            return InteractionResultHolder.success(stack);
        }
    }

    private InteractionResult addBarrierToSlot(ItemStack stack, Player player, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        int slot = getSelectedSlot(tag);

        // Проверяем, не добавлен ли уже этот блок в другой слот
        ListTag positions = tag.getList(BARRIER_POSITIONS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < positions.size(); i++) {
            if (i == slot) continue;

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

    private void activateBarrier(ItemStack stack, Player player, Level level) {
        // Проверяем КД
        if (PlayerCooldowns.isOnCooldown(player, this)) {
            int left = PlayerCooldowns.getCooldownLeft(player, this);
            player.sendSystemMessage(Component.literal("Barrier Wall is on cooldown! (" + (left / 20) + "s left)")
                    .withStyle(ChatFormatting.RED));
            return;
        }

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

        // Проверяем, не активен ли уже барьер
        if (barrierTag.getBoolean(BARRIER_ACTIVE_TAG)) {
            player.sendSystemMessage(Component.literal("Barrier in slot " + (slot + 1) + " is already active!")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        // Проверяем ману
        LazyOptional<IMana> manaCap = player.getCapability(ManaProvider.MANA_CAPABILITY);
        if (!manaCap.isPresent()) {
            player.sendSystemMessage(Component.literal("Mana capability not found!")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        IMana mana = manaCap.orElseThrow(IllegalStateException::new);
        if (mana.getMana() < ACTIVATION_MANA_COST) {
            player.sendSystemMessage(Component.literal("Not enough mana! Need " + ACTIVATION_MANA_COST)
                    .withStyle(ChatFormatting.RED));
            return;
        }

        BlockPos barrierPos = new BlockPos(
                barrierTag.getInt("x"),
                barrierTag.getInt("y"),
                barrierTag.getInt("z")
        );

        // Проверяем, что блок барьера все еще существует
        if (!level.getBlockState(barrierPos).is(ModBlocks.BARRIER.get())) {
            player.sendSystemMessage(Component.literal("Barrier block no longer exists!")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        // Создаем стены вокруг барьера
        WallCreationResult result = createWallBlocks(level, barrierPos, player);
        if (result.placedBlocks.isEmpty()) {
            player.sendSystemMessage(Component.literal("Could not create barrier walls!")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        // Тратим ману
        mana.removeMana(ACTIVATION_MANA_COST);

        // Активируем барьер
        barrierTag.putBoolean(BARRIER_ACTIVE_TAG, true);
        barrierTag.putLong(BARRIER_ACTIVE_TIME_TAG, level.getGameTime());
        
        // НЕ сохраняем позиции блоков стен в NBT - вычисляем их динамически
        // Это предотвращает превышение лимита размера NBT (2MB)
        // Позиции можно вычислить на основе центра барьера и радиуса
        
        // Сохраняем только pending позиции (где блоки не могли быть размещены)
        // Их должно быть намного меньше, чем всех блоков стен
        if (!result.pendingPositions.isEmpty()) {
            ListTag pendingTag = new ListTag();
            for (BlockPos pendingPos : result.pendingPositions) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("x", pendingPos.getX());
                posTag.putInt("y", pendingPos.getY());
                posTag.putInt("z", pendingPos.getZ());
                pendingTag.add(posTag);
            }
            barrierTag.put("PendingPositions", pendingTag);
        }
        
        positions.set(slot, barrierTag);
        tag.put(BARRIER_POSITIONS_TAG, positions);

        // Обновляем BlockEntity барьера – включаем эффекты стены
        if (!level.isClientSide && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            var be = serverLevel.getBlockEntity(barrierPos);
            if (be instanceof BarrierBlockEntity barrierBe) {
                barrierBe.updateWallState(player.getUUID(), true, slot);
            }
        }

        // Устанавливаем КД
        PlayerCooldowns.setCooldown(player, this, COOLDOWN_TICKS);

        // Накладываем эффект EffectBaseDomain на все сущности в области барьера
        applyBaseDomainEffect(level, barrierPos, player);

        player.sendSystemMessage(Component.literal("Barrier Wall activated in slot " + (slot + 1) + "!")
                .withStyle(ChatFormatting.GREEN));
    }

    /**
     * Накладывает эффект EffectBaseDomain на все сущности в области барьера
     */
    private void applyBaseDomainEffect(Level level, BlockPos barrierCenter, Player owner) {
        if (level.isClientSide || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        
        double radius = 20.0;
        AABB aabb = new AABB(
                barrierCenter.getX() - radius,
                barrierCenter.getY() - radius,
                barrierCenter.getZ() - radius,
                barrierCenter.getX() + radius,
                barrierCenter.getY() + radius,
                barrierCenter.getZ() + radius
        );
        
        // Находим все сущности в области барьера (кроме владельца)
        List<net.minecraft.world.entity.LivingEntity> entities = serverLevel.getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class,
                aabb,
                entity -> entity != owner && entity.isAlive()
        );
        
        // Накладываем эффект на каждую сущность
        for (net.minecraft.world.entity.LivingEntity entity : entities) {
            // Проверяем, что сущность действительно внутри барьера (радиус 20 блоков)
            double distanceSq = entity.blockPosition().distSqr(barrierCenter);
            if (distanceSq <= radius * radius) {
                // Накладываем эффект с длительностью 2 секунды (будет обновляться)
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
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

    /**
     * Результат создания стены барьера
     */
    private static class WallCreationResult {
        final List<BlockPos> placedBlocks;
        final List<BlockPos> pendingPositions;
        
        WallCreationResult(List<BlockPos> placedBlocks, List<BlockPos> pendingPositions) {
            this.placedBlocks = placedBlocks;
            this.pendingPositions = pendingPositions;
        }
    }
    
    /**
     * Вычисляет все позиции блоков стен барьера на основе центра и радиуса.
     * Используется для динамического определения позиций без хранения в NBT.
     */
    public static List<BlockPos> calculateWallBlockPositions(BlockPos center, int radius) {
        List<BlockPos> positions = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Создаем блоки только на границах куба (на поверхности)
                    boolean isOnBoundary = Math.abs(x) == radius || 
                                          Math.abs(y) == radius || 
                                          Math.abs(z) == radius;
                    if (isOnBoundary) {
                        positions.add(center.offset(x, y, z));
                    }
                }
            }
        }
        return positions;
    }

    /**
     * Проверяет, является ли позиция частью барьера (находится на границе куба барьера).
     * Используется для проверки без необходимости вычислять все позиции.
     */
    public static boolean isWallBlockPosition(BlockPos pos, BlockPos barrierCenter, int radius) {
        int dx = Math.abs(pos.getX() - barrierCenter.getX());
        int dy = Math.abs(pos.getY() - barrierCenter.getY());
        int dz = Math.abs(pos.getZ() - barrierCenter.getZ());
        
        // Позиция должна быть на границе куба (хотя бы одна координата на границе)
        return (dx == radius || dy == radius || dz == radius) &&
               dx <= radius && dy <= radius && dz <= radius;
    }

    private WallCreationResult createWallBlocks(Level level, BlockPos center, Player player) {
        List<BlockPos> blocks = new ArrayList<>();
        List<BlockPos> pendingPositions = new ArrayList<>(); // Позиции, где блоки не могли быть размещены
        int radius = BARRIER_RADIUS;

        // Создаем куб из блоков на границах радиуса
        // Проходим по всем граням куба размером (radius*2+1) x (radius*2+1) x (radius*2+1)
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Создаем блоки только на границах куба (на поверхности)
                    // Это означает, что хотя бы одна координата должна быть на границе
                    boolean isOnBoundary = Math.abs(x) == radius || 
                                          Math.abs(y) == radius || 
                                          Math.abs(z) == radius;
                    
                    if (isOnBoundary) {
                        BlockPos pos = center.offset(x, y, z);
                        
                        // Проверяем, что позиция свободна или может быть заменена
                        BlockState currentState = level.getBlockState(pos);
                        // Создаем блоки везде, где возможно (включая воздух и заменяемые блоки)
                        if (currentState.isAir() || currentState.canBeReplaced()) {
                            level.setBlock(pos, ModBlocks.BARRIER_BLOCK.get().defaultBlockState(), 3);
                            blocks.add(pos);
                        } else {
                            // Позиция занята блоком, который нельзя заменить - сохраняем для отслеживания
                            pendingPositions.add(pos);
                        }
                    }
                }
            }
        }

        return new WallCreationResult(blocks, pendingPositions);
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
        barrierTag.putBoolean(BARRIER_ACTIVE_TAG, false);
        barrierTag.putUUID("owner", player.getUUID());

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

    public static List<ActiveBarrierInfo> getActiveBarriers(ItemStack stack, Level level) {
        List<ActiveBarrierInfo> activeBarriers = new ArrayList<>();
        if (stack == null || !stack.hasTag()) return activeBarriers;

        CompoundTag tag = stack.getTag();
        ListTag positions = tag.getList(BARRIER_POSITIONS_TAG, Tag.TAG_COMPOUND);

        for (int i = 0; i < positions.size(); i++) {
            CompoundTag barrierTag = positions.getCompound(i);
            if (barrierTag.contains("x") && barrierTag.contains("y") && barrierTag.contains("z")) {
                boolean isActive = barrierTag.getBoolean(BARRIER_ACTIVE_TAG);
                if (isActive) {
                    BlockPos barrierPos = new BlockPos(
                            barrierTag.getInt("x"),
                            barrierTag.getInt("y"),
                            barrierTag.getInt("z")
                    );
                    long activeTime = barrierTag.getLong(BARRIER_ACTIVE_TIME_TAG);
                    UUID ownerUUID = barrierTag.getUUID("owner");
                    
                    // Вычисляем позиции блоков стен динамически вместо загрузки из NBT
                    // Это предотвращает превышение лимита размера NBT
                    // НЕ фильтруем по наличию блоков - это позволяет находить сломанные блоки
                    List<BlockPos> wallBlocks = calculateWallBlockPositions(barrierPos, BARRIER_RADIUS);
                    
                    activeBarriers.add(new ActiveBarrierInfo(barrierPos, wallBlocks, ownerUUID, activeTime, i, stack));
                }
            }
        }

        return activeBarriers;
    }

    /**
     * Удаляет барьер с заданной позицией из NBT предмета (используется при разрушении блока barier).
     */
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
                    // Полностью очищаем слот: позиция, активность, стены и pending
                    positions.set(i, new CompoundTag());
                    tag.put(BARRIER_POSITIONS_TAG, positions);
                    break;
                }
            }
        }
    }

    public static class ActiveBarrierInfo {
        public final BlockPos barrierPos;
        public final List<BlockPos> wallBlocks;
        public final UUID ownerUUID;
        public final long activeTime;
        public final int slot;
        public final ItemStack itemStack;

        public ActiveBarrierInfo(BlockPos barrierPos, List<BlockPos> wallBlocks, UUID ownerUUID, long activeTime, int slot, ItemStack itemStack) {
            this.barrierPos = barrierPos;
            this.wallBlocks = wallBlocks;
            this.ownerUUID = ownerUUID;
            this.activeTime = activeTime;
            this.slot = slot;
            this.itemStack = itemStack;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (hasOwner(stack)) {
            tooltip.add(Component.translatable("item.owner", stack.getTag().getString(OWNER_NAME_TAG))
                    .withStyle(ChatFormatting.GOLD));
        }

        tooltip.add(Component.translatable("item.nacikmod.barrier_wall.desc1"));
        tooltip.add(Component.translatable("item.nacikmod.barrier_wall.desc2", ACTIVATION_MANA_COST, RESTORE_MANA_COST )
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

    }
}
