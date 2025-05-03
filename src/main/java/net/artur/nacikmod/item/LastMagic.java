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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LastMagic extends Item {
    public LastMagic(Properties properties) {
        super(properties.rarity(Rarity.EPIC));
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // Активируем эффект (предмет будет удален внутри startLastMagic)
            ManaLastMagic.startLastMagic(player);
            
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
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Предмет всегда светится
    }
}
