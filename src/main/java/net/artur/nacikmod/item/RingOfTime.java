package net.artur.nacikmod.item;

import net.artur.nacikmod.event.KeyBindings;
import net.artur.nacikmod.network.CooldownSyncPacket;
import net.artur.nacikmod.registry.ModMessages;
import net.artur.nacikmod.network.TimeStopPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RingOfTime extends Item implements ICurioItem {
    private static final HashMap<UUID, Integer> cooldowns = new HashMap<>();

    public RingOfTime(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        if (entity instanceof Player player) {
            UUID playerId = player.getUUID();
            cooldowns.putIfAbsent(playerId, 0);

            if (!player.level().isClientSide) { // Серверная логика
                int cooldown = cooldowns.get(playerId);
                if (cooldown > 0) {
                    cooldowns.put(playerId, cooldown - 1);
                    syncCooldown(player, cooldown - 1);
                }
            } else { // Клиентская логика
                checkKeyPress(player);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void checkKeyPress(Player player) {
        if (KeyBindings.ABILITY_KEY.isDown() && getCooldown(player) == 0) {
            ModMessages.sendToServer(new TimeStopPacket());
        }
    }

    public static void setCooldown(Player player, int cooldown) {
        cooldowns.put(player.getUUID(), cooldown);
        syncCooldown(player, cooldown);
    }

    public static int getCooldown(Player player) {
        return cooldowns.getOrDefault(player.getUUID(), 0);
    }

    private static void syncCooldown(Player player, int cooldown) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModMessages.sendToClient(serverPlayer, new CooldownSyncPacket(player.getUUID(), cooldown));
        }
    }
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(net.minecraft.network.chat.Component.translatable("item.nacikmod.ring_of_time.desc1"));
        tooltip.add(net.minecraft.network.chat.Component.translatable("item.nacikmod.ring_of_time.desc2")
                .withStyle(style -> style.withColor(0x00FFFF))); // Цвет - голубой
    }
}