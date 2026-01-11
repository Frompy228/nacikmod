package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.projectiles.FireAnnihilationEntity;
import net.artur.nacikmod.registry.ModMessages;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.artur.nacikmod.entity.projectiles.FireAnnihilationEntity.DAMAGE;

public class FireAnnihilation extends Item {

    private static final int MANA_COST = 250;
    private static final double SPAWN_DISTANCE = 2.5D;

    public FireAnnihilation(Properties properties) {
        super(properties.stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!tryConsumeMana(player, MANA_COST)) {
            return InteractionResultHolder.fail(stack);
        }

        // 🔥 направление взгляда
        Vec3 look = player.getLookAngle();

        // 🔥 позиция СПЕРЕДИ игрока
        double x = player.getX() + look.x * SPAWN_DISTANCE;
        double y = player.getEyeY() - 0.2; // как в оригинале
        double z = player.getZ() + look.z * SPAWN_DISTANCE;

        FireAnnihilationEntity entity =
                new FireAnnihilationEntity(level, player, x, y, z);

        level.addFreshEntity(entity);

        return InteractionResultHolder.success(stack);
    }

    private boolean tryConsumeMana(Player player, int amount) {
        return player.getCapability(ManaProvider.MANA_CAPABILITY)
                .map(m -> {
                    if (m.getMana() < amount) return false;
                    m.removeMana(amount);
                    if (player instanceof ServerPlayer sp) {
                        ModMessages.sendManaToClient(sp, m.getMana(), m.getMaxMana());
                    }
                    return true;
                }).orElse(false);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.fire_annihilation.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.fire_annihilation.desc2", MANA_COST, DAMAGE)
                .withStyle(style -> style.withColor(0x00FFFF)));
    }
}
