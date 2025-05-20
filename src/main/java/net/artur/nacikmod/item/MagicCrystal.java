package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.IMana;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import java.awt.*;

public class MagicCrystal extends Item {
    private static final String MANA_TAG = "StoredMana";
    private static final int MAX_STORAGE = 3000; // Максимальное количество маны в кристалле

    public MagicCrystal() {
        super(new Item.Properties()
                .stacksTo(1) // Один предмет в слоте
                .rarity(Rarity.UNCOMMON)); // Редкость
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            LazyOptional<IMana> manaCap = serverPlayer.getCapability(ManaProvider.MANA_CAPABILITY);

            manaCap.ifPresent(playerMana -> {
                int storedMana = getStoredMana(stack);
                int playerManaAmount = playerMana.getMana();

                if (player.isShiftKeyDown()) {
                    // Забираем ману из кристалла в игрока
                    if (storedMana > 0 && playerManaAmount < playerMana.getMaxMana()) {
                        int maxCanReceive = playerMana.getMaxMana() - playerManaAmount; // Сколько игрок может принять
                        int transferAmount = Math.min(storedMana, Math.min(maxCanReceive, 100)); // Переносим не больше 50 и не больше свободного места

                        playerMana.addMana(transferAmount);
                        setStoredMana(stack, storedMana - transferAmount);
                    }
                } else {
                    // Передаём ману из игрока в кристалл
                    if (playerManaAmount > 0 && storedMana < MAX_STORAGE) {
                        int transferAmount = Math.min(playerManaAmount, 100);
                        playerMana.removeMana(transferAmount);
                        setStoredMana(stack, storedMana + transferAmount);
                    }
                }
            });
        }

        return InteractionResultHolder.success(stack);
    }

    public static int getStoredMana(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(MANA_TAG);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getStoredMana(stack) > 0; // Показывать полоску, если есть мана
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round((float) getStoredMana(stack) / MAX_STORAGE * 13); // 13 — максимальная длина
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FFFF; // Цвет (голубой, цвет маны)
    }

    public static void setStoredMana(ItemStack stack, int amount) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(MANA_TAG, Math.min(amount, MAX_STORAGE)); // Ограничиваем макс. значение
    }
    @Override
    public void appendHoverText(ItemStack stack, Level level, java.util.List<net.minecraft.network.chat.Component> tooltip, TooltipFlag flag) {
        int storedMana = getStoredMana(stack);
        tooltip.add(net.minecraft.network.chat.Component.literal("Stored Mana: " + storedMana + "/" + MAX_STORAGE)
                .setStyle(Style.EMPTY.withColor(0x00FFFF))); // Цвет текста - голубой
    }

}
