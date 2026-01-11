package net.artur.nacikmod.item;

import net.artur.nacikmod.item.ability.BloodCircleManager;
import net.artur.nacikmod.util.ItemUtils;
import net.artur.nacikmod.util.PlayerCooldowns;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.artur.nacikmod.capability.mana.ManaProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BloodCircleItem extends Item implements ItemUtils.ITogglableMagicItem {
    private static final String ACTIVE_TAG = "active";
    private static final int MANA_COST = 750;
    private static final int COOLDOWN_TICKS = 700; // 35 секунд (35 * 20 = 700)

    public BloodCircleItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }

    // --- Реализация интерфейса ITogglableMagicItem ---
    @Override
    public String getActiveTag() { return ACTIVE_TAG; }

    @Override
    public void deactivate(Player player, ItemStack stack) {
        BloodCircleManager.removeCircle(player);
        stack.getOrCreateTag().putBoolean(getActiveTag(), false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // 0. ПРОВЕРКА: Не активировать повторно, если способность уже активна
            if (BloodCircleManager.isActive(player)) {
                player.sendSystemMessage(Component.literal("Blood Circle is already active!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            // 1. ПРОВЕРКА ВОЗДУХА / ПОВЕРХНОСТИ И БЛОКОВ ВОКРУГ
            if (!BloodCircleManager.canCreateAt(player)) {
                player.sendSystemMessage(Component.literal("You must be on solid ground to perform the ritual!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            // 2. ПРОВЕРКА КУЛДАУНА
            if (PlayerCooldowns.isOnCooldown(player, this)) {
                int ticksLeft = PlayerCooldowns.getCooldownLeft(player, this);
                int secondsLeft = (ticksLeft / 20) + 1;
                player.sendSystemMessage(Component.literal("Item is on cooldown! (" + secondsLeft + "s left)")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            // 3. ПРОВЕРКА МАНЫ
            if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                player.sendSystemMessage(Component.literal("Not enough mana!").withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            // 4. ПРОВЕРКА ЗДОРОВЬЯ
            if (player.getHealth() <= 2.0f) {
                player.sendSystemMessage(Component.literal("Not enough health to perform ritual!").withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            // ЕСЛИ ВСЕ ПРОВЕРКИ ПРОЙДЕНЫ -> ТРАТИМ РЕСУРСЫ
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));
            player.hurt(player.damageSources().magic(), player.getHealth() * 0.7f);

            // Активация через менеджер
            BloodCircleManager.startCircleCreation(player);
            stack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 0.5f);

            PlayerCooldowns.setCooldown(player, this, COOLDOWN_TICKS);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.blood_circle.desc1"));

        if (level != null && level.isClientSide) {
            Player player = ItemUtils.getClientPlayer(level);
            if (player != null) {
                float healthCost = player.getHealth() * 0.7f;
                tooltipComponents.add(Component.translatable("item.nacikmod.blood_circle_item.desc2", String.format("%.1f", healthCost))
                        .withStyle(ChatFormatting.DARK_RED));

                tooltipComponents.add(Component.translatable("item.nacikmod.blood_circle_item.desc3", MANA_COST).withStyle(style -> style.withColor(0x00FFFF)));

                // Проверка активности через менеджер
                boolean isActive = BloodCircleManager.isActive(player);
                if (isActive) {
                    tooltipComponents.add(Component.translatable("item.active").withStyle(ChatFormatting.GREEN));
                } else {
                    tooltipComponents.add(Component.translatable("item.inactive").withStyle(ChatFormatting.RED));
                }
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Проверка активности через менеджер (только на клиенте для рендеринга)
        Player player = ItemUtils.getClientPlayerForFoil();
        if (player != null) {
            return BloodCircleManager.isActive(player);
        }
        return false;
    }


}
