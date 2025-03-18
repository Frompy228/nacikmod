package net.artur.nacikmod.item;

import net.artur.nacikmod.registry.ModAttributes; // Класс, где зарегистрированы атрибуты маны
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MagicCircuit extends Item {

    public MagicCircuit(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) { // Выполняем только на сервере
            ItemStack stack = player.getItemInHand(hand);

            // Получаем атрибут максимальной маны и увеличиваем его
            AttributeInstance maxManaAttribute = player.getAttribute(ModAttributes.MAX_MANA.get());
            if (maxManaAttribute != null) {
                double newMaxMana = maxManaAttribute.getBaseValue() + 50;
                maxManaAttribute.setBaseValue(newMaxMana);
            }


            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
