package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Grail extends Item {
    public Grail(Properties properties) {
        super(properties.rarity(Rarity.RARE).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.grail.desc1"));

    }
    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // Предмет всегда светится
    }
}
