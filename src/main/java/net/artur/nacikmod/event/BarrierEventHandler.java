package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.BarrierSeal;
import net.artur.nacikmod.item.BarrierWall;
import net.artur.nacikmod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class BarrierEventHandler {

    // Сейчас этот обработчик отвечает только за очистку слотов предметов,
    // когда блок барьера разрушается.

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        BlockState state = event.getState();
        if (!state.is(ModBlocks.BARRIER.get())) return;

        BlockPos pos = event.getPos();

        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof BarrierSeal) {
                    BarrierSeal.removeBarrier(stack, pos);
                }
                if (stack.getItem() instanceof BarrierWall) {
                    BarrierWall.removeBarrier(stack, pos);
                }
            }
        }
    }
}
