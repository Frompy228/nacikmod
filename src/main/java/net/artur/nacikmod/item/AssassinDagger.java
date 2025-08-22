package net.artur.nacikmod.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

public class AssassinDagger extends SwordItem {
    public AssassinDagger() {
        super(Tiers.IRON, 5, -2.1f, new Item.Properties());
    }
}
