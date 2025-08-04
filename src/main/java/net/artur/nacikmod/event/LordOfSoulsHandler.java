package net.artur.nacikmod.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.LordOfSouls;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.capability.lord_of_souls_summoned_entities.LordOfSoulsSummonedEntityProvider;

import java.util.List;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class LordOfSoulsHandler {

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof Player player)) {
            return;
        }

        // Check if killed entity is a player - don't absorb player souls
        if (event.getEntity() instanceof Player) {
            return;
        }

        // Check if killed entity was a summoned entity - don't absorb summoned entity souls
        boolean isSummonedEntity = false;
        
        // Check capability first (if owner is online)
        for (Player onlinePlayer : event.getEntity().level().players()) {
            if (onlinePlayer.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                    .map(cap -> cap.isTracked(event.getEntity().getUUID())).orElse(false)) {
                isSummonedEntity = true;
                break;
            }
        }
        
        // Check NBT backup if not found (for offline owners)
        if (!isSummonedEntity) {
            var entityData = event.getEntity().getPersistentData();
            if (entityData.contains("lord_of_souls_owner")) {
                isSummonedEntity = true;
            }
        }
        
        if (isSummonedEntity) {
            // This was a summoned entity, don't absorb its soul
            return;
        }

        // Check if player has LordOfSouls item in inventory
        ItemStack lordOfSoulsItem = findLordOfSoulsItem(player);
        if (lordOfSoulsItem.isEmpty()) {
            return;
        }

        // Check if item is active
        if (!LordOfSouls.isActive(lordOfSoulsItem)) {
            return;
        }

        // Check if player has enough mana
        if (!player.getCapability(ManaProvider.MANA_CAPABILITY)
                .map(mana -> mana.getMana() >= LordOfSouls.MANA_COST).orElse(false)) {
            player.sendSystemMessage(Component.literal("Not enough mana!").withStyle(ChatFormatting.RED));
            return;
        }

        // Consume mana and add soul
        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(LordOfSouls.MANA_COST));
        
        int soulsBefore = LordOfSouls.getSoulsCount(lordOfSoulsItem);
        LordOfSouls.addSoul(lordOfSoulsItem, event.getEntity());
        int soulsAfter = LordOfSouls.getSoulsCount(lordOfSoulsItem);
        
        if (soulsAfter > soulsBefore) {
            player.sendSystemMessage(Component.literal("Soul absorbed! Souls: " + soulsAfter + "/10")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            player.sendSystemMessage(Component.literal("Soul limit reached! (10/10)")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Check if damage would be lethal
        float remainingHealth = player.getHealth() - event.getAmount();
        if (remainingHealth > 0) {
            return;
        }

        // Check if player has LordOfSouls item in inventory
        ItemStack lordOfSoulsItem = findLordOfSoulsItem(player);
        if (lordOfSoulsItem.isEmpty()) {
            return;
        }

        // Check if player has souls
        if (LordOfSouls.getSoulsCount(lordOfSoulsItem) <= 0) {
            return;
        }

        // Prevent death and consume soul
        event.setCanceled(true);
        player.setHealth(player.getMaxHealth());
        LordOfSouls.removeSoul(lordOfSoulsItem);

        player.sendSystemMessage(Component.literal("Death prevented! Souls remaining: " + LordOfSouls.getSoulsCount(lordOfSoulsItem))
                .withStyle(ChatFormatting.GREEN));
    }

    private static ItemStack findLordOfSoulsItem(Player player) {
        // Check main inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof LordOfSouls) {
                return stack;
            }
        }

        // Check offhand
        ItemStack offhandStack = player.getOffhandItem();
        if (offhandStack.getItem() instanceof LordOfSouls) {
            return offhandStack;
        }

        return ItemStack.EMPTY;
    }
} 