package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.artur.nacikmod.capability.mana.ManaProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WorldSlash extends Item {
    private static final int MANA_COST = 6500;
    private static final int COOLDOWN_TICKS = 200;
    private static final float DAMAGE = 500.0F;
    private static final int RANGE = 50;
    private static final double HALF_WIDTH = 25.0;
    private static final double HALF_HEIGHT = 25.0;

    public WorldSlash(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                player.sendSystemMessage(Component.literal("Not enough mana!").withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

            List<LivingEntity> targets = getEntitiesInSlashArea(level, player);

            for (LivingEntity target : targets) {
                target.hurt(level.damageSources().indirectMagic(player, player), DAMAGE);
                spawnHitParticles(target.position(), (ServerLevel) level);
            }

            showDebugParticles(player);
            
            // Удаляем предмет после использования
            itemStack.shrink(1);
        }

        return InteractionResultHolder.success(itemStack);
    }

    private List<LivingEntity> getEntitiesInSlashArea(Level level, Player player) {
        Vec3 origin = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 look = player.getLookAngle().normalize();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
        Vec3 up = new Vec3(0, 1, 0);

        AABB bigBox = new AABB(origin.subtract(RANGE, RANGE, RANGE), origin.add(RANGE, RANGE, RANGE));

        return level.getEntitiesOfClass(LivingEntity.class, bigBox, entity -> {
            if (entity == player || !entity.isAlive() || entity.isInvulnerable()) return false;

            Vec3 toTarget = entity.getBoundingBox().getCenter().subtract(origin);
            double forwardDist = toTarget.dot(look);
            if (forwardDist < 0 || forwardDist > RANGE) return false;

            double sideDist = toTarget.dot(right);
            if (Math.abs(sideDist) > HALF_WIDTH) return false;

            double verticalDist = toTarget.dot(up);
            return Math.abs(verticalDist) <= HALF_HEIGHT;
        });
    }

    private void spawnHitParticles(Vec3 position, ServerLevel level) {
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, position.x, position.y, position.z, 15, 1.0, 1.0, 1.0, 0.0);
    }

    private void showDebugParticles(Player player) {
        if (!(player.level() instanceof ServerLevel level)) return;

        Vec3 look = player.getLookAngle().normalize();
        Vec3 origin = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
        Vec3 up = new Vec3(0, 1, 0);

        double step = 5.0;

        for (double f = 0; f <= RANGE; f += step) {
            for (double x = -HALF_WIDTH; x <= HALF_WIDTH; x += step) {
                for (double y = -HALF_HEIGHT; y <= HALF_HEIGHT; y += step) {
                    Vec3 offset = look.scale(f).add(right.scale(x)).add(up.scale(y));
                    Vec3 pos = origin.add(offset);

                    level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                            pos.x, pos.y, pos.z,
                            1, 0, 0, 0, 0);
                }
            }
        }
    }


    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("item.nacikmod.world_slash.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.world_slash.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));
        tooltipComponents.add(Component.translatable("item.disappears")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
