package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.artur.nacikmod.item.ability.HundredSealAbility;
import net.artur.nacikmod.capability.mana.ManaProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HundredSeal extends Item {
    private static final String ACTIVE_TAG = "active";
    private static final int MAX_STORAGE = 100000;

    public HundredSeal(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                // Добавляем ману из игрока в предмет
                player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                    int playerMana = mana.getMana();
                    int storedMana = HundredSealAbility.getStoredMana(itemStack);
                    
                    if (playerMana > 0 && storedMana < MAX_STORAGE) {
                        int transferAmount = Math.min(playerMana, Math.min(100, MAX_STORAGE - storedMana));
                        mana.removeMana(transferAmount);
                        HundredSealAbility.addManaToItem(itemStack, transferAmount);
                    }
                });
            } else {
                // Toggle Hundred Seal effect
                if (HundredSealAbility.isHundredSealActive(player)) {
                    HundredSealAbility.stopHundredSeal(player);
                    itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                } else {
                    // Проверяем, есть ли достаточно маны для активации
                    int storedMana = HundredSealAbility.getStoredMana(itemStack);
                    if (storedMana >= 100) {
                        HundredSealAbility.startHundredSeal(player);
                        itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                    } else {
                        player.sendSystemMessage(Component.literal("Not enough mana in Hundred Seal")
                                .withStyle(ChatFormatting.RED));
                    }
                }
                
                // Start cooldown only for activation/deactivation
                player.getCooldowns().addCooldown(this, 20); // 1 second cooldown
            }
        }
        
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        int storedMana = HundredSealAbility.getStoredMana(stack);
        tooltipComponents.add(Component.translatable("item.nacikmod.hundred_seal.desc1"));
        tooltipComponents.add(net.minecraft.network.chat.Component.literal("Stored Mana: " + storedMana + "/" + MAX_STORAGE)
                .setStyle(Style.EMPTY.withColor(0x00FFFF))); // Цвет текста - голубой
        tooltipComponents.add(Component.translatable("item.nacikmod.hundred_seal.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));
        tooltipComponents.add(Component.translatable("item.nacikmod.hundred_seal.desc3")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.nacikmod.hundred_seal.desc4")
                .withStyle(ChatFormatting.GRAY));

        boolean isActive = stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
        if (isActive) {
            tooltipComponents.add(Component.translatable("item.nacikmod.hundred_seal.active")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.translatable("item.nacikmod.hundred_seal.inactive")
                    .withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return HundredSealAbility.getStoredMana(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round((float) HundredSealAbility.getStoredMana(stack) / MAX_STORAGE * 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FFFF; // Голубой цвет маны
    }
}
