package net.artur.nacikmod.item;

import net.artur.nacikmod.entity.projectiles.FireBallEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireBall extends Item {
    public FireBall(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Создаем снаряд
            FireBallEntity fireball = new FireBallEntity(level, player);

            // Ставим перед игроком
            Vec3 pos = player.getEyePosition().add(player.getLookAngle().scale(0.5));
            fireball.setPos(pos.x, pos.y, pos.z);

            // Запускаем прямо вперед
            fireball.shoot(player.getLookAngle(), 1.5f);

            // Добавляем в мир
            level.addFreshEntity(fireball);

            // Кулдаун 1 секунда
            player.getCooldowns().addCooldown(this, 20);

            // Износ
            if (!player.isCreative()) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
        }

        return InteractionResultHolder.success(stack);
    }
}