package net.artur.nacikmod.item;

import net.artur.nacikmod.item.ability.FireArrowSpell;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FireStaff extends Item {
    public FireStaff(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            if (FireArrowSpell.cast(player, level)) {
                // Add cooldown if spell was successfully cast
                player.getCooldowns().addCooldown(this, 20); // 1 second cooldown
                return InteractionResultHolder.success(itemstack);
            } else {
                // If spell failed (not enough mana), return pass to allow other interactions
                return InteractionResultHolder.pass(itemstack);
            }
        }
        
        return InteractionResultHolder.success(itemstack);
    }
}



