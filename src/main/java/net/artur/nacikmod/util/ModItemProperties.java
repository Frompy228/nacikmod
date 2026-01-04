package net.artur.nacikmod.util;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.item.MagicBow;

public class ModItemProperties {
    public static void addCustomItemProperties() {
        makeBow(ModItems.MAGIC_BOW.get());
    }

    private static void makeBow(Item item) {
        // Свойство "pull" отвечает за фазу натяжения (0.0, 0.65, 0.9, 1.0)
        ItemProperties.register(item, new ResourceLocation("pull"), (stack, level, living, seed) -> {
            if (living == null) {
                return 0.0F;
            } else {
                return living.getUseItem() != stack ? 0.0F :
                        // Мы ограничиваем (clamp) значение между 0 и 1, чтобы анимация не "ломалась"
                        Math.min(1.0F, (float)(stack.getUseDuration() - living.getUseItemRemainingTicks()) / (float)MagicBow.MAX_DRAW_DURATION);
            }
        });

        // Свойство "pulling" определяет, натянут ли лук в принципе (для смены иконки на активную)
        ItemProperties.register(item, new ResourceLocation("pulling"), (stack, level, living, seed) -> {
            return living != null && living.isUsingItem() && living.getUseItem() == stack ? 1.0F : 0.0F;
        });
    }
}