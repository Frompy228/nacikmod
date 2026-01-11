package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber
public class EarthStep extends Item implements ItemUtils.ITogglableMagicItem {
    private static final int SPEED_AMPLIFIER = 3;
    private static final int MANA_COST_PER_SECOND = 30;
    private static final String ACTIVE_TAG = "active";
    private static final java.util.Set<UUID> activeEarthStepPlayers = new java.util.HashSet<>();

    public EarthStep(Properties properties) {
        super(properties.fireResistant());
    }

    // --- Реализация интерфейса ITogglableMagicItem ---
    @Override
    public String getActiveTag() { return ACTIVE_TAG; }

    @Override
    public void deactivate(Player player, ItemStack stack) {
        activeEarthStepPlayers.remove(player.getUUID());
        stack.getOrCreateTag().putBoolean(getActiveTag(), false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            boolean isActive = itemStack.getOrCreateTag().getBoolean(ACTIVE_TAG);
            if (isActive) {
                deactivate(player, itemStack);
                player.sendSystemMessage(Component.literal("Earth Step deactivated!").withStyle(ChatFormatting.RED));
            } else {
                if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST_PER_SECOND).orElse(false)) {
                    player.sendSystemMessage(Component.literal("Not enough mana!").withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.fail(itemStack);
                }
                activeEarthStepPlayers.add(player.getUUID());
                itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                player.sendSystemMessage(Component.literal("Earth Step activated!").withStyle(ChatFormatting.GREEN));
            }
            player.getCooldowns().addCooldown(this, 30);
        }
        return InteractionResultHolder.success(itemStack);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player) || event.phase != TickEvent.Phase.END) return;

        if (!activeEarthStepPlayers.contains(player.getUUID())) return;

        // ИСПОЛЬЗУЕМ УТИЛИТУ: Поиск предмета по всему инвентарю и курсору
        ItemStack activeStack = ItemUtils.findActiveItem(player, EarthStep.class);

        if (activeStack == null) {
            activeEarthStepPlayers.remove(player.getUUID());
            return;
        }

        applyActiveEffects(player, activeStack);
    }

    private static void applyActiveEffects(ServerPlayer player, ItemStack stack) {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 35, SPEED_AMPLIFIER, true, false));
        if (!player.isShiftKeyDown()) {
            createEarthBlocksUnderPlayer(player.level(), player);
        }

        if (player.tickCount % 20 == 0) {
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                if (mana.getMana() >= MANA_COST_PER_SECOND) {
                    mana.removeMana(MANA_COST_PER_SECOND);
                } else {
                    // Используем метод из интерфейса для выключения
                    if (stack.getItem() instanceof EarthStep item) {
                        item.deactivate(player, stack);
                        player.sendSystemMessage(Component.literal("Not enough mana!").withStyle(ChatFormatting.RED));
                    }
                }
            });
        }
    }

    // --- Логика блоков и урона ---
    private static void createEarthBlocksUnderPlayer(Level level, Player player) {
        BlockPos playerPos = player.blockPosition();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos checkPos = playerPos.offset(x, -1, z);
                if (!level.getBlockState(checkPos).isSolid()) {
                    level.setBlock(checkPos, net.artur.nacikmod.registry.ModBlocks.TEMPORARY_DIRT.get().defaultBlockState(), 3);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && activeEarthStepPlayers.contains(player.getUUID())) {
            if (event.getSource() == player.level().damageSources().fall()) {
                event.setAmount(event.getAmount() * 0.7f);
            }
        }
    }

    // --- Визуал и предотвращение багов свечения ---
    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            if (stack.getOrCreateTag().getBoolean(ACTIVE_TAG) && !activeEarthStepPlayers.contains(player.getUUID())) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            }
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        deactivate(player, item);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("item.nacikmod.earth_step.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.earth_step.desc2").withStyle(s -> s.withColor(0x00FFFF)));

        boolean isActive = stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
        tooltipComponents.add(Component.translatable(isActive ? "item.active" : "item.inactive")
                .withStyle(isActive ? ChatFormatting.GREEN : ChatFormatting.RED));
    }
}