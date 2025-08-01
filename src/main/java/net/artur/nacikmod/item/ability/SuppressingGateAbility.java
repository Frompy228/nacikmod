package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SuppressingGateAbility {
    
    // 1. Игрок под эффектом не может атаковать других
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (hasSuppressingGateEffect(player)) {
            event.setCanceled(true);
        }
    }

    // 2. Игрок под эффектом не может использовать предметы
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getEntity();
        if (hasSuppressingGateEffect(player)) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            }
        }
    }

    // 3. Игрок не может разрушать блоки
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (hasSuppressingGateEffect(player)) {
            event.setCanceled(true);
            event.setNewSpeed(0.0F);
        }
    }

    // 4. Игрок под эффектом не может получать урон от других игроков (опционально)
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && hasSuppressingGateEffect(player)) {
            // Можно добавить логику для защиты от определенных типов атак
            // Пока оставляем как есть - игрок может получать урон
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && hasSuppressingGateEffect(player)) {
            // Можно добавить логику для изменения получаемого урона
            // Пока оставляем как есть
        }
    }

    // Проверка: есть ли у игрока эффект SuppressingGate
    public static boolean hasSuppressingGateEffect(Player player) {
        return player.hasEffect(ModEffects.SUPPRESSING_GATE.get());
    }
} 