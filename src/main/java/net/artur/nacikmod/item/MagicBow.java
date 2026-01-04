package net.artur.nacikmod.item;

import net.artur.nacikmod.entity.projectiles.ManaArrowProjectile;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class MagicBow extends BowItem {
    // Очень быстрое натяжение: 12 тиков (0.6 сек)
    public static final int MAX_DRAW_DURATION = 12;
    private static final int MANA_COST = 5;

    public MagicBow() {
        super(new Item.Properties().stacksTo(1).durability(500));
    }

    public static float getPowerForTime(int charge) {
        float f = (float)charge / (float)MAX_DRAW_DURATION;

        // КУБИЧЕСКАЯ ЗАВИСИМОСТЬ: делает короткие проклики неэффективными.
        // При 0.5 натяжения (6 тиков) сила будет 0.125.
        // При 1.0 натяжения (12 тиков) сила будет 1.0.
        f = f * f * f;

        if (f > 1.0F) f = 1.0F;
        return f;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        int i = this.getUseDuration(stack) - timeLeft;
        float power = getPowerForTime(i);

        // Стрела вылетает, только если есть хоть какой-то заряд
        if (power >= 0.05F) {
            if (!level.isClientSide) {
                boolean hasEnoughMana = player.getCapability(ManaProvider.MANA_CAPABILITY)
                        .map(mana -> mana.getMana() >= MANA_COST).orElse(false);

                if (hasEnoughMana || player.getAbilities().instabuild) {
                    boolean hasMultishot = stack.getEnchantmentLevel(Enchantments.MULTISHOT) > 0;
                    int arrowsToShoot = hasMultishot ? 3 : 1;

                    for (int j = 0; j < arrowsToShoot; j++) {
                        ManaArrowProjectile manaArrow = new ManaArrowProjectile(level, player);

                        // СКОРОСТЬ: При полном заряде 5.0F (очень быстро).
                        // При спаме скорость будет крайне низкой (~0.5F), стрела упадет под ноги.
                        float velocity = power * 5.0F;

                        // Передаем значение power для расчета динамического урона
                        manaArrow.applyEnchantments(stack, power);

                        float yRot = player.getYRot();
                        if (hasMultishot && j > 0) {
                            yRot += (j == 1 ? 10.0F : -10.0F);
                        }

                        // Уменьшили разброс (divergence) до 0.5F для точности
                        manaArrow.shootFromRotation(player, player.getXRot(), yRot, 0.0F, velocity, 0.5F);
                        level.addFreshEntity(manaArrow);
                    }

                    if (!player.getAbilities().instabuild) {
                        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));
                    }

                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));

                    // Звук выстрела становится выше при слабом заряде и ниже/мощнее при полном
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
                } else {
                    player.displayClientMessage(Component.literal("Not enough mana!").withStyle(ChatFormatting.RED), true);
                }
            }
        }
    }
}