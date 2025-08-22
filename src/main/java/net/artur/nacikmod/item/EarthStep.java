package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashSet;

@Mod.EventBusSubscriber
public class EarthStep extends Item {
    private static final int SPEED_AMPLIFIER = 3;
    private static final int MANA_COST_PER_SECOND = 30; // 20 маны в секунду
    private static final String ACTIVE_TAG = "active";
    private static final java.util.Set<UUID> activeEarthStepPlayers = new java.util.HashSet<>();

    public EarthStep(Properties properties) {
        super(properties.fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            boolean isActive = itemStack.hasTag() && itemStack.getTag().getBoolean(ACTIVE_TAG);
            if (isActive) {
                // Деактивация
                activeEarthStepPlayers.remove(player.getUUID());
                itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                player.sendSystemMessage(Component.literal("Earth Step deactivated!").withStyle(ChatFormatting.RED));
            } else {
                // Проверяем ману
                if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST_PER_SECOND).orElse(false)) {
                    player.sendSystemMessage(Component.literal("Not enough mana!").withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.fail(itemStack);
                }
                // Активация
                activeEarthStepPlayers.add(player.getUUID());
                itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                player.sendSystemMessage(Component.literal("Earth Step activated!").withStyle(ChatFormatting.GREEN));
            }
            // Кулдаун для предотвращения спама
            player.getCooldowns().addCooldown(this, 20); // 1 сек
        }
        return InteractionResultHolder.success(itemStack);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof net.minecraft.server.level.ServerPlayer player)) return;
        if (event.phase != TickEvent.Phase.END) return;

        if (!activeEarthStepPlayers.contains(player.getUUID())) return;
        ItemStack earthStepItem = null;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof EarthStep && stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG)) {
                earthStepItem = stack;
                break;
            }
        }
        if (earthStepItem == null) {
            // Если предмета нет или неактивен, выключаем способность
            activeEarthStepPlayers.remove(player.getUUID());
            return;
        }
        // Каждый тик: эффекты и земля
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 25, SPEED_AMPLIFIER, true, false));
        if (!player.isShiftKeyDown()) {
            createEarthBlocksUnderPlayer(player.level(), player);
        }
        // Раз в секунду: трата маны и отключение при нехватке
        if (player.tickCount % 20 == 0) {
            var manaOpt = player.getCapability(ManaProvider.MANA_CAPABILITY);
            var mana = manaOpt.orElse(null);
            if (mana != null) {
                if (mana.getMana() >= MANA_COST_PER_SECOND) {
                    mana.removeMana(MANA_COST_PER_SECOND);
                } else {
                    // Маны не хватает — выключаем способность
                    activeEarthStepPlayers.remove(player.getUUID());
                    earthStepItem.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                    player.sendSystemMessage(Component.literal("Not enough mana! Earth Step deactivated.").withStyle(ChatFormatting.RED));
                }
            }
        }
    }

    private static void createEarthBlocksUnderPlayer(Level level, Player player) {
        BlockPos playerPos = player.blockPosition();
        
        // Проверяем блоки в радиусе 2 блоков вокруг игрока
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos checkPos = playerPos.offset(x, -1, z);
                
                // Проверяем, есть ли твердый блок под игроком
                BlockState blockBelow = level.getBlockState(checkPos);
                if (!blockBelow.isSolid()) {
                    // Создаем блок земли
                    level.setBlock(checkPos, net.artur.nacikmod.registry.ModBlocks.TEMPORARY_DIRT.get().defaultBlockState(), 3);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (activeEarthStepPlayers.contains(player.getUUID())) {
                if (event.getSource() == player.level().damageSources().fall()) {
                    float reducedDamage = event.getAmount() * 0.7f;
                    event.setAmount(reducedDamage);
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("item.nacikmod.earth_step.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.earth_step.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));
        boolean isActive = stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
        if (isActive) {
            tooltipComponents.add(Component.translatable("item.nacikmod.release.active")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.translatable("item.nacikmod.release.inactive")
                    .withStyle(ChatFormatting.RED));
        }
    }
    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }
}
