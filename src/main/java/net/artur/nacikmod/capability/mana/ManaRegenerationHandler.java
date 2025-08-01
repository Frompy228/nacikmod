package net.artur.nacikmod.capability.mana;

import net.artur.nacikmod.capability.reward.PlayerRewardsProvider;
import net.artur.nacikmod.network.ModMessages;
import net.artur.nacikmod.registry.ModEffects;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

@Mod.EventBusSubscriber
public class ManaRegenerationHandler {
    private static final int BASE_REGEN_AMOUNT = 1; // Базовая регенерация маны
    private static final int REGEN_INTERVAL = 20; // Интервал регенерации в тиках (1 секунда)
    private static final int DARK_SPHERE_REGEN = 2; // Регенерация от Dark Sphere
    private static final int MANA_BLESSING_REGEN = 1; // Регенерация от Mana Blessing

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;

        // Регенерация происходит каждый REGEN_INTERVAL тиков
        if (player.tickCount % REGEN_INTERVAL == 0) {
            LazyOptional<IMana> manaCap = player.getCapability(ManaProvider.MANA_CAPABILITY);
            manaCap.ifPresent(mana -> {
                int currentMana = mana.getMana();
                int maxMana = mana.getMaxMana();

                // Синхронизируем с клиентом
                if (player instanceof ServerPlayer serverPlayer) {
                    ModMessages.sendManaToClient(serverPlayer, currentMana, maxMana);
                }

                // Регенерируем ману только если она не полная
                if (currentMana < maxMana) {
                    final int regenAmount = getRegenAmount(player);
                    mana.regenerateMana(regenAmount);
                }
            });
        }
    }

    private static int getRegenAmount(Player player) {
        final int[] regenAmount = {BASE_REGEN_AMOUNT};

        // Проверяем награду за 24 часа игры
        player.getCapability(PlayerRewardsProvider.PLAYER_REWARDS_CAPABILITY).ifPresent(rewards -> {
            if (rewards.hasReceived24hReward()) {
                regenAmount[0] += 2; // Добавляем +2 к регенерации
            }
        });

        if (player.hasEffect(ModEffects.EFFECT_MANA_BLESSING.get())) {
            regenAmount[0] += MANA_BLESSING_REGEN;
        }

        // Проверяем наличие Dark Sphere
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    if (stacksHandler.getStacks().getStackInSlot(i).getItem() == ModItems.DARK_SPHERE.get()) {
                        regenAmount[0] += DARK_SPHERE_REGEN;
                        return;
                    }
                }
            }
        });

        return regenAmount[0];
    }
}
