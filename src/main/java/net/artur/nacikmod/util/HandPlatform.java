package net.artur.nacikmod.util;

import net.artur.nacikmod.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

public class HandPlatform {

    // Проверка, можно ли атаковать LansOfProtection в левой руке
    public static boolean canUseLansOfProtectionOffhand(Player player) {
        if (player == null) return false;

        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);

        // Проверяем, что в правой руке находится LansOfNacii
        return mainHandItem.is(ModItems.LANS_OF_NACII.get()) && offHandItem.is(ModItems.LANS_OF_PROTECTION.get());
    }
}
