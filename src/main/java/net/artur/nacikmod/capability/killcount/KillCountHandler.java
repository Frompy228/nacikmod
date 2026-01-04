package net.artur.nacikmod.capability.killcount;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KillCountHandler {
    private static final int REQUIRED_KILLS_FOR_WORLD_SLASH = 500;

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity killed = event.getEntity();
        
        // Проверяем источник урона
        var damageSource = event.getSource();
        var directEntity = damageSource.getDirectEntity();
        var entity = damageSource.getEntity();
        
        Player killer = null;
        
        // Если прямой источник - снаряд, ищем владельца
        if (directEntity instanceof net.artur.nacikmod.entity.projectiles.SlashProjectile) {
            var owner = ((net.artur.nacikmod.entity.projectiles.SlashProjectile) directEntity).getOwner();
            if (owner instanceof Player) {
                killer = (Player) owner;
            }
        } else if (directEntity instanceof net.artur.nacikmod.entity.projectiles.DoubleSlashProjectile) {
            var owner = ((net.artur.nacikmod.entity.projectiles.DoubleSlashProjectile) directEntity).getOwner();
            if (owner instanceof Player) {
                killer = (Player) owner;
            }
        } else if (entity instanceof Player) {
            killer = (Player) entity;
        }
        
        if (killer == null || !(killer instanceof ServerPlayer)) return;
        
        ServerPlayer serverKiller = (ServerPlayer) killer;
        
        // Получаем capability убийцы
        serverKiller.getCapability(KillCountProvider.KILL_COUNT_CAPABILITY).ifPresent(killCount -> {
            boolean isSlashKill = false;
            
            // Определяем тип убийства по типу снаряда
            if (directEntity instanceof net.artur.nacikmod.entity.projectiles.SlashProjectile || 
                directEntity instanceof net.artur.nacikmod.entity.projectiles.DoubleSlashProjectile) {
                isSlashKill = true;
            }
            
            // Увеличиваем счетчик убийств
            if (isSlashKill) {
                killCount.addSlashKill();
                
                // Проверяем, достиг ли игрок нужного количества убийств для получения World Slash
                if (!killCount.hasReceivedWorldSlashReward() && killCount.getSlashKills() >= REQUIRED_KILLS_FOR_WORLD_SLASH) {
                    // Создаем предметы наград
                    ItemStack worldSlashItem = new ItemStack(ModItems.WORLD_SLASH.get());
                    ItemStack ancientScrollItem = new ItemStack(ModItems.ANCIENT_SCROLL.get());
                    
                    // Помечаем, что игрок получил награду
                    killCount.setReceivedWorldSlashReward(true);
                    
                    // Пытаемся добавить предметы в инвентарь
                    boolean worldSlashAdded = serverKiller.getInventory().add(worldSlashItem);
                    boolean ancientScrollAdded = serverKiller.getInventory().add(ancientScrollItem);
                    
                    // Отправляем сообщение игроку
                    serverKiller.sendSystemMessage(Component.literal("You have mastered the art of slashing! You received World Slash!")
                            .withStyle(ChatFormatting.GOLD));
                    
                    // Если инвентарь полон, выбрасываем предметы рядом с игроком
                    if (!worldSlashAdded) {
                        serverKiller.drop(worldSlashItem, false);
                    }
                    
                    if (!ancientScrollAdded) {
                        serverKiller.drop(ancientScrollItem, false);
                    }
                }
            }
        });
    }
} 