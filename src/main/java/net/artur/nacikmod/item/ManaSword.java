package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.player.Player;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;

public class ManaSword extends SwordItem {
    private static final int MAX_DURABILITY = 3;
    private static final float BASE_DAMAGE = 2f;
    private static final float ATTACK_SPEED = -2.4f;
    private static final String MANA_DAMAGE_TAG = "ManaDamage";

    public ManaSword(Tier tier, Properties properties) {
        super(tier, (int)BASE_DAMAGE, ATTACK_SPEED, properties.durability(MAX_DURABILITY));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return MAX_DURABILITY;
    }

    @Override
    public float getDamage() {
        return 0; // Базовый урон всегда 0
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // Получаем сохраненный урон из NBT
            int damageBonus = stack.getOrCreateTag().getInt("DamageBonus");
            
            // Наносим урон
            target.hurt(target.level().damageSources().playerAttack((Player) attacker), damageBonus);
            
            // Уменьшаем прочность меча
            stack.hurtAndBreak(1, attacker, (entity) -> {});
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.mana_sword.desc1"));

        // Добавляем информацию о дополнительном уроне
        if (stack.hasTag() && stack.getTag().contains(MANA_DAMAGE_TAG)) {
            int manaDamage = stack.getTag().getInt(MANA_DAMAGE_TAG);
            if (manaDamage > 0) {
                tooltipComponents.add(Component.translatable("item.nacikmod.mana_sword.bonus_damage", manaDamage)
                        .withStyle(ChatFormatting.BLUE));
            }
        }
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        // Устанавливаем полную прочность
        stack.setDamageValue(0);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        // Разрешаем только зачарование "Небесная кара", которое добавляется при создании
        return enchantment == Enchantments.SMITE;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false; // Запрещаем зачарование через книги
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false; // Запрещаем починку
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false; // Запрещаем починку через наковальню
    }

    @Override
    public boolean canBeDepleted() {
        return true; // Разрешаем уменьшение прочности
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false; // Запрещаем зачарование через книги
    }
}
