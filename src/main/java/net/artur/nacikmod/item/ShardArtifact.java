package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class ShardArtifact extends Item {
    public static final Rarity RED = Rarity.create("RED", ChatFormatting.RED);
    
    public ShardArtifact(Properties properties) {
        super(new Item.Properties()
                .rarity(RED)); // Красный цвет
    }
}
