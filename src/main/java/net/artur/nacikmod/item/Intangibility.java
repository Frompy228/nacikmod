package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.List;

public class Intangibility extends Item {
    private static final String ACTIVE_TAG = "active";
    private static final int MANA_COST_PER_SECOND = 50;

    public Intangibility(Properties properties) {
        super(properties.stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            // Проверяем наличие Dark Sphere в слотах Curios
            boolean hasDarkSphere = CuriosApi.getCuriosInventory(player)
                    .map(handler -> {
                        for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                            for (int i = 0; i < stacksHandler.getSlots(); i++) {
                                ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                                if (stack.getItem() == ModItems.DARK_SPHERE.get()) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    })
                    .orElse(false);

            if (!hasDarkSphere) {
                player.sendSystemMessage(Component.literal("You need Dark Sphere to use this ability!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            if (isActive(itemStack)) {
                // Деактивация
                itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            } else {
                // Активация
                itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
            }
            player.getCooldowns().addCooldown(this, 100); // небольшой кулдаун
        }
        return InteractionResultHolder.success(itemStack);
    }

    public static boolean isActive(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("item.nacikmod.intangibility.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.intangibility.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));
        boolean isActive = isActive(stack);
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
        return isActive(stack);
    }

    // Вся логика траты маны и продления эффекта — здесь
    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        if (!(entity instanceof Player player) || level.isClientSide) return;
        if (!isActive(stack)) return;

        // Проверка наличия Dark Sphere
        boolean hasDarkSphere = CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                        for (int i = 0; i < stacksHandler.getSlots(); i++) {
                            ItemStack curiosStack = stacksHandler.getStacks().getStackInSlot(i);
                            if (curiosStack.getItem() == ModItems.DARK_SPHERE.get()) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .orElse(false);
        if (!hasDarkSphere) {
            stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            return;
        }

        // Снимаем ману каждую секунду
        if (level.getGameTime() % 20 == 0) {
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                if (mana.getMana() >= MANA_COST_PER_SECOND) {
                    mana.removeMana(MANA_COST_PER_SECOND);
                } else {
                    // Недостаточно маны — деактивируем
                    stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                }
            });
        }
    }
}
