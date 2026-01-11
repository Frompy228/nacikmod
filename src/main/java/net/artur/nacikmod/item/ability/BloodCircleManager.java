package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.BloodCircleItem;
import net.artur.nacikmod.registry.ModBlocks;
import net.artur.nacikmod.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class BloodCircleManager {
    private static final Map<UUID, List<BlockPos>> ACTIVE_CIRCLES = new HashMap<>();
    private static final Map<UUID, BlockPos> CIRCLE_CENTERS = new HashMap<>();
    private static final Map<UUID, List<CirclePart>> CREATION_QUEUE = new HashMap<>();
    private static final Map<UUID, Integer> TICK_COUNTER = new HashMap<>();
    private static final Set<UUID> ACTIVE_PLAYERS = new HashSet<>();

    private record CirclePart(BlockPos pos, net.minecraft.world.level.block.Block block, Direction facing) {}

    // ПРОВЕРКА: Можно ли создать круг здесь?
    public static boolean canCreateAt(Player player) {
        BlockPos center = player.blockPosition();
        Level level = player.level();

        // 1. Проверка на воздух под ногами (нельзя в небе)
        if (level.getBlockState(center.below()).isAir()) {
            return false;
        }

        // 2. Проверка: не стоим ли мы внутри стены
        BlockState feetState = level.getBlockState(center);
        if (!feetState.isAir() && !feetState.canBeReplaced()) {
            return false;
        }

        // 3. Проверка блоков вокруг для создания круга
        // Если хотя бы одна точка занята камнем/землей, которую нельзя заменить — ритуал не начнется
        Direction f = player.getDirection();
        Direction r = f.getClockWise();
        Direction b = f.getOpposite();
        Direction l = f.getCounterClockWise();

        List<BlockPos> circlePositions = List.of(
                center.relative(f),      // перед
                center.relative(r),      // справа
                center.relative(b),      // сзади
                center.relative(l),      // слева
                center.relative(f).relative(r),  // перед-справа (угол)
                center.relative(b).relative(r), // сзади-справа (угол)
                center.relative(b).relative(l), // сзади-слева (угол)
                center.relative(f).relative(l)  // перед-слева (угол)
        );

        for (BlockPos pos : circlePositions) {
            BlockState state = level.getBlockState(pos);
            // Проверяем, можно ли заменить блок (воздух, трава, снег — можно. Камень, дерево — нельзя)
            if (!state.isAir() && !state.canBeReplaced() &&
                    !state.is(ModBlocks.BLOOD_CIRCLE.get()) && !state.is(ModBlocks.BLOOD_CIRCLE_CORNER.get())) {
                return false;
            }
        }

        return true;
    }

    public static void startCircleCreation(Player player) {
        UUID uuid = player.getUUID();
        BlockPos center = player.blockPosition();
        removeCircle(player);

        CIRCLE_CENTERS.put(uuid, center);
        ACTIVE_CIRCLES.put(uuid, new ArrayList<>());
        ACTIVE_PLAYERS.add(uuid);

        Direction f = player.getDirection();
        Direction r = f.getClockWise();
        Direction b = f.getOpposite();
        Direction l = f.getCounterClockWise();

        List<CirclePart> parts = new ArrayList<>();

        parts.add(new CirclePart(center.relative(f), ModBlocks.BLOOD_CIRCLE.get(), f.getOpposite()));
        parts.add(new CirclePart(center.relative(r), ModBlocks.BLOOD_CIRCLE.get(), r.getOpposite()));
        parts.add(new CirclePart(center.relative(b), ModBlocks.BLOOD_CIRCLE.get(), b.getOpposite()));
        parts.add(new CirclePart(center.relative(l), ModBlocks.BLOOD_CIRCLE.get(), l.getOpposite()));

        parts.add(new CirclePart(center.relative(f).relative(r), ModBlocks.BLOOD_CIRCLE_CORNER.get(), f.getCounterClockWise()));
        parts.add(new CirclePart(center.relative(b).relative(r), ModBlocks.BLOOD_CIRCLE_CORNER.get(), r.getCounterClockWise()));
        parts.add(new CirclePart(center.relative(b).relative(l), ModBlocks.BLOOD_CIRCLE_CORNER.get(), b.getCounterClockWise()));
        parts.add(new CirclePart(center.relative(f).relative(l), ModBlocks.BLOOD_CIRCLE_CORNER.get(), l.getCounterClockWise()));

        CREATION_QUEUE.put(uuid, parts);
        TICK_COUNTER.put(uuid, 0);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;
        UUID uuid = player.getUUID();
        Level level = player.level();

        if (!ACTIVE_CIRCLES.containsKey(uuid) && !CREATION_QUEUE.containsKey(uuid)) return;

        // 1. ПРОВЕРКА ЦЕЛОСТНОСТИ: Если блоки круга сломаны
        if (ACTIVE_CIRCLES.containsKey(uuid) && !CREATION_QUEUE.containsKey(uuid)) {
            for (BlockPos pos : ACTIVE_CIRCLES.get(uuid)) {
                BlockState state = level.getBlockState(pos);
                if (!state.is(ModBlocks.BLOOD_CIRCLE.get()) && !state.is(ModBlocks.BLOOD_CIRCLE_CORNER.get())) {
                    removeCircle(player);
                    player.sendSystemMessage(Component.literal("The Blood Circle has been broken!")
                            .withStyle(ChatFormatting.DARK_RED));
                    return;
                }
            }
        }

        // 2. Деактивация при потере предмета
        ItemStack activeStack = ItemUtils.findActiveItem(player, BloodCircleItem.class);

        if (activeStack == null) {
            removeCircle(player);
            return;
        }

        // 3. Плавное создание
        if (CREATION_QUEUE.containsKey(uuid)) {
            int ticks = TICK_COUNTER.getOrDefault(uuid, 0);
            if (ticks % 2 == 0) {
                List<CirclePart> queue = CREATION_QUEUE.get(uuid);
                if (!queue.isEmpty()) {
                    placePart(level, queue.remove(0), uuid);
                } else {
                    CREATION_QUEUE.remove(uuid);
                }
            }
            TICK_COUNTER.put(uuid, ticks + 1);
        }

        // 4. Дистанция
        if (CIRCLE_CENTERS.containsKey(uuid)) {
            BlockPos center = CIRCLE_CENTERS.get(uuid);
            if (player.distanceToSqr(center.getX() + 0.5, center.getY(), center.getZ() + 0.5) > 5.0) {
                removeCircle(player);
            }
        }
    }

    private static void placePart(Level level, CirclePart part, UUID owner) {
        BlockState state = level.getBlockState(part.pos);
        if (state.isAir() || state.canBeReplaced() || state.is(ModBlocks.BLOOD_CIRCLE.get()) || state.is(ModBlocks.BLOOD_CIRCLE_CORNER.get())) {
            level.setBlock(part.pos, part.block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, part.facing), 3);
            ACTIVE_CIRCLES.get(owner).add(part.pos);
        }
    }

    public static void removeCircle(Player player) {
        if (player == null) return;
        UUID uuid = player.getUUID();
        if (ACTIVE_CIRCLES.containsKey(uuid)) {
            for (BlockPos pos : ACTIVE_CIRCLES.get(uuid)) {
                player.level().removeBlock(pos, false);
            }
            ACTIVE_CIRCLES.remove(uuid);
            CIRCLE_CENTERS.remove(uuid);
            CREATION_QUEUE.remove(uuid);
        }
        ACTIVE_PLAYERS.remove(uuid);

        // Обновляем состояние всех предметов BloodCircleItem в инвентаре
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BloodCircleItem) {
                stack.getOrCreateTag().putBoolean("active", false);
            }
        }
    }

    public static boolean isActive(Player player) {
        return ACTIVE_PLAYERS.contains(player.getUUID());
    }

    @SubscribeEvent
    public static void onItemPickup(net.minecraftforge.event.entity.player.EntityItemPickupEvent event) {
        Player player = event.getEntity();
        ItemStack pickedStack = event.getItem().getItem();
        if (pickedStack.getItem() instanceof BloodCircleItem && !isActive(player)) {
            pickedStack.getOrCreateTag().putBoolean("active", false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        removeCircle(player);
    }
}
