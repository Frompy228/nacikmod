package net.artur.nacikmod.capability.mana;

import net.artur.nacikmod.network.ModMessages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ManaRegenerationHandler {
    private static final int MANA_REGEN_AMOUNT = 100; // Сколько маны восстанавливаем
    private static final int TICKS_PER_SECOND = 20; // Количество тиков в секунду
    private static final Map<UUID, Integer> tickCounter = new HashMap<>(); // Отслеживаем тики для каждого игрока

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        UUID playerId = player.getUUID();
        int ticks = tickCounter.getOrDefault(playerId, 0) + 1;

        if (ticks >= TICKS_PER_SECOND) {
            // Раз в секунду
            LazyOptional<IMana> manaCap = player.getCapability(ManaProvider.MANA_CAPABILITY);
            manaCap.ifPresent(mana -> {
                int currentMana = mana.getMana();
                int maxMana = mana.getMaxMana();
                if (player instanceof ServerPlayer serverPlayer) {
                    ModMessages.sendManaToClient(serverPlayer, mana.getMana(), maxMana); // Синхронизация
                }
                if (currentMana < maxMana) {
                    mana.setMana(Math.min(currentMana + MANA_REGEN_AMOUNT, maxMana)); // Восстанавливаем ману


                }
            });

            ticks = 0; // Сбрасываем счётчик
        }

        tickCounter.put(playerId, ticks);
    }
}
