package net.artur.nacikmod.mixin;

import net.artur.nacikmod.registry.ModItems;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * Mixin для применения скидки 5% от Cross к торговым предложениям
 * Работает аналогично эффекту HERO_OF_THE_VILLAGE
 */
@Mixin(Villager.class)
public class VillagerDiscountMixin {

    @Inject(
        method = "updateSpecialPrices(Lnet/minecraft/world/entity/player/Player;)V",
        at = @At("TAIL")
    )
    private void nacikmod$applyCrossDiscount(Player player, CallbackInfo ci) {
        // Проверяем наличие Cross в слотах Curios
        boolean hasCross = CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                        for (int i = 0; i < stacksHandler.getSlots(); i++) {
                            net.minecraft.world.item.ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                            if (stack.getItem() == ModItems.CROSS.get()) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .orElse(false);

        if (hasCross) {
            Villager villager = (Villager) (Object) this;
            
            // Применяем скидку 5% ко всем предложениям
            // 0.05 = 5% скидка (аналогично эффекту героя деревни)
            for (MerchantOffer offer : villager.getOffers()) {
                if (offer != null) {
                    net.minecraft.world.item.ItemStack baseCostA = offer.getBaseCostA();
                    if (!baseCostA.isEmpty()) {
                        // Вычисляем скидку 10% от базовой цены
                        int discount = (int) Math.floor(0.1 * (double) baseCostA.getCount());
                        if (discount > 0) {
                            // Добавляем скидку к существующей разнице (отрицательное значение для скидки)
                            offer.addToSpecialPriceDiff(-Math.max(discount, 1));
                        }
                    }
                }
            }
        }
    }
}


