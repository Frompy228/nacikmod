package net.artur.nacikmod.item;

import net.artur.nacikmod.item.ability.BloodCircleManager;
import net.artur.nacikmod.item.ability.BloodContractManager;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.artur.nacikmod.item.ability.BloodContractManager.TICK_DRAIN_COST;

public class BloodContract extends Item implements ItemUtils.ITogglableMagicItem {
    private static final String ACTIVE_TAG = "active";
    public static final int ACTIVATION_MANA_COST = 1000;
    public static final float HEALTH_COST = 10.0f;

    public BloodContract(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }

    // --- Реализация интерфейса ITogglableMagicItem ---
    @Override
    public String getActiveTag() { return ACTIVE_TAG; }

    @Override
    public void deactivate(Player player, ItemStack stack) {
        if (player instanceof ServerPlayer serverPlayer) {
            BloodContractManager.breakContract(serverPlayer, null);
        }
        stack.getOrCreateTag().putBoolean(getActiveTag(), false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // ДЕАКТИВАЦИЯ (SHIFT + ПКМ)
            if (player.isShiftKeyDown()) {
                BloodContractManager.breakContract((ServerPlayer) player, "Contract Terminated Manually");
                return InteractionResultHolder.success(stack);
            }

            // ПРОВЕРКА ЦЕЛИ (только если контракт еще не активен или мы хотим сменить цель)
            LivingEntity target = getTargetEntity(player, 15.0D);
            if (target == null) {
                player.sendSystemMessage(Component.literal("No target in sight!").withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            // ПРОВЕРКА РЕСУРСОВ
            if (!canPayCosts(player)) return InteractionResultHolder.fail(stack);

            // АКТИВАЦИЯ
            BloodContractManager.activateContract((ServerPlayer) player, target);
            stack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);

            // Оплата
            payCosts(player);

            player.sendSystemMessage(Component.literal("Blood Contract Signed with: ")
                    .append(target.getDisplayName()).withStyle(ChatFormatting.DARK_RED));

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WITCH_CELEBRATE, SoundSource.PLAYERS, 1.0f, 0.5f);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private boolean canPayCosts(Player player) {
        boolean hasMana = player.getCapability(ManaProvider.MANA_CAPABILITY).map(m -> m.getMana() >= ACTIVATION_MANA_COST).orElse(false);
        boolean hasHealth = player.getHealth() > HEALTH_COST;

        if (!hasMana) player.sendSystemMessage(Component.literal("Not enough mana!").withStyle(ChatFormatting.RED));
        if (!hasHealth) player.sendSystemMessage(Component.literal("Too weak for contract!").withStyle(ChatFormatting.RED));

        return hasMana && hasHealth;
    }

    private void payCosts(Player player) {
        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(m -> m.removeMana(ACTIVATION_MANA_COST));
        player.hurt(player.damageSources().magic(), HEALTH_COST);
    }

    private LivingEntity getTargetEntity(Player player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().scale(range);
        Vec3 end = start.add(look);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level(), player, start, end,
                player.getBoundingBox().expandTowards(look).inflate(1.0D),
                entity -> entity instanceof LivingEntity && entity != player && !entity.isSpectator());
        return (hit != null && hit.getEntity() instanceof LivingEntity living) ? living : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.nacikmod.blood_contract.desc1"));

        if (level != null && level.isClientSide) {
            Player player = net.artur.nacikmod.util.ItemUtils.getClientPlayer(level);
            if (player != null) {
                // Расходы
                tooltip.add(Component.translatable("item.nacikmod.blood_contract.desc2", String.format("%.1f", HEALTH_COST))
                        .withStyle(ChatFormatting.DARK_RED));

                tooltip.add(Component.translatable("item.nacikmod.blood_contract.desc3", ACTIVATION_MANA_COST, TICK_DRAIN_COST).withStyle(style -> style.withColor(0x00FFFF)));

                // Бафф от круга
                if (BloodCircleManager.isActive(player)) {
                    tooltip.add(Component.literal("BLOOD CIRCLE ACTIVE: Linked Network (15 blocks)")
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                }

                boolean isActive = BloodContractManager.isContractActive(player);
                if (isActive) {
                    tooltip.add(Component.translatable("item.active").withStyle(ChatFormatting.GREEN));
                } else {
                    tooltip.add(Component.translatable("item.inactive").withStyle(ChatFormatting.RED));
                }
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Проверка активности через менеджер (только на клиенте для рендеринга)
        Player player = net.artur.nacikmod.util.ItemUtils.getClientPlayerForFoil();
        if (player != null) {
            return BloodContractManager.isContractActive(player);
        }
        return false;
    }
}
