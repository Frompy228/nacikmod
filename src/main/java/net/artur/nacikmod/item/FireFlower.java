package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.projectiles.FireCloudEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FireFlower extends Item {
    private static final int MANA_COST = 150;
    private static final double CLOUD_DISTANCE = 5; // Расстояние от игрока до облаков

    public FireFlower(Properties properties) {
        super(properties.fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Проверяем, достаточно ли маны
            if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                player.sendSystemMessage(Component.literal("Not enough mana!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Тратим ману
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));

            // Создаем 4 облака вокруг игрока
            createCloudsAroundPlayer(level, player);

            // Устанавливаем кулдаун
            player.getCooldowns().addCooldown(this, 120);
        }

        return InteractionResultHolder.success(itemStack);
    }

    private void createCloudsAroundPlayer(Level level, Player player) {
        double playerX = player.getX();
        double playerY = player.getY() + 1.0; // Немного выше уровня глаз
        double playerZ = player.getZ();

        // Создаем 4 облака в направлениях: север, восток, юг, запад
        double[][] directions = {
            {0, 0, -1},  // Север
            {1, 0, 0},   // Восток
            {0, 0, 1},   // Юг
            {-1, 0, 0}   // Запад
        };

        for (double[] direction : directions) {
            double cloudX = playerX + direction[0] * CLOUD_DISTANCE;
            double cloudY = playerY;
            double cloudZ = playerZ + direction[2] * CLOUD_DISTANCE;

            // Создаем облако огня
            FireCloudEntity fireCloud = new FireCloudEntity(level, player, cloudX, cloudY, cloudZ);
            level.addFreshEntity(fireCloud);

            // Создаем эффект частиц при создании облака
            for (int i = 0; i < 15; i++) {
                double particleX = cloudX + (level.getRandom().nextDouble() - 0.5) * 2;
                double particleY = cloudY + (level.getRandom().nextDouble() - 0.5) * 2;
                double particleZ = cloudZ + (level.getRandom().nextDouble() - 0.5) * 2;

                level.addParticle(
                    ParticleTypes.FLAME,
                    particleX, particleY, particleZ,
                    0.1, 0.1, 0.1
                );
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("item.nacikmod.fire_flower.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.fire_flower.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));
    }
}
