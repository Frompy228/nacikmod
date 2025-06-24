package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.item.Intangibility;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IntangibilityAbility {
    // 1. Другие игроки и мобы не могут атаковать игрока с активной способностью
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && isIntangible(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && isIntangible(player)) {
            event.setCanceled(true);
        }
    }

    // 2. Игрок под способностью не может атаковать других
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (isIntangible(player)) {
            event.setCanceled(true);
        }
    }

    // 3. Игрок под способностью не может использовать предметы, кроме Intangibility
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getEntity();
        if (isIntangible(player)) {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof Intangibility)) {
                if (event.isCancelable()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    // 4. Игрок не может разрушать блоки
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (isIntangible(player)) {
            event.setCanceled(true);
            event.setNewSpeed(0.0F);
        }
    }

    // 5. При поднятии предмета Intangibility он всегда становится неактивным
    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        ItemStack pickedStack = event.getItem().getItem();
        if (pickedStack.getItem() instanceof Intangibility) {
            if (pickedStack.hasTag() && pickedStack.getTag().getBoolean("active")) {
                pickedStack.getOrCreateTag().putBoolean("active", false);
            }
        }
    }

    // Проверка: активна ли способность (есть ли активный предмет Intangibility)
    public static boolean isIntangible(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof Intangibility && stack.hasTag() && stack.getTag().getBoolean("active")) {
                return true;
            }
        }
        return false;
    }
}
