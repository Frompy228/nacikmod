package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.artur.nacikmod.item.ability.ManaLastMagic;
import net.artur.nacikmod.item.ability.BloodCircleManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LastMagic extends Item {
    public LastMagic(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // Активируем эффект
            ManaLastMagic.startLastMagic(player);
            
            // Удаляем предмет после использования
            itemStack.shrink(1);
            
            // Start cooldown to prevent spam
            player.getCooldowns().addCooldown(this, 20); // 1 second cooldown
        }
        
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.last_magic.desc2"));
        tooltipComponents.add(Component.translatable("item.nacikmod.last_magic.desc1")
                .withStyle(ChatFormatting.DARK_RED));
        tooltipComponents.add(Component.translatable("item.nacikmod.last_magic.desc3")
                .withStyle(ChatFormatting.DARK_RED));

        // Проверяем уровень и сторону клиента
        if (level != null && level.isClientSide) {
            Player player = net.artur.nacikmod.util.ItemUtils.getClientPlayer(level);
            if (player != null) {
                // Базовая длительность: 180 секунд (3600 тиков)
                int baseDuration = 180;
                int bonusDuration = 0;

                // Проверка активности Blood Circle через менеджер
                boolean bloodCircleActive = BloodCircleManager.isActive(player);

                if (bloodCircleActive) {
                    bonusDuration = 10; // +10 секунд
                }

                int totalDuration = baseDuration + bonusDuration;

                // Отображение длительности эффекта
                tooltipComponents.add(Component.literal("Duration: " + totalDuration + "s")
                        .withStyle(ChatFormatting.GOLD));

                // УСИЛЕНИЕ: Показываем фиолетовый текст при активном Blood Circle
                if (bloodCircleActive) {
                    tooltipComponents.add(Component.literal("BLOOD CIRCLE ACTIVE: +10s Duration")
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }
        }

        tooltipComponents.add(Component.translatable("item.disappears")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Предмет всегда светится
    }
}
