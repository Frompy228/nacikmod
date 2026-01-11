package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.IMana;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.projectiles.FirePillarEntity;
import net.artur.nacikmod.util.PlayerCooldowns;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class FirePillar extends Item {
    private static final int RANGE = 150;
    private static final int MANA_COST = 400;
    private static final int COOLDOWN_TICKS = 300; // 15 секунд = 20 * 15 тиков

    public FirePillar(Properties properties) {
        super(properties.fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Проверяем КД
            if (PlayerCooldowns.isOnCooldown(player, this)) {
                int left = PlayerCooldowns.getCooldownLeft(player, this);
                player.sendSystemMessage(Component.literal("Fire Pillar is on cooldown! (" + left / 20 + "s left)")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Проверяем ману
            LazyOptional<IMana> manaCap = player.getCapability(ManaProvider.MANA_CAPABILITY);
            if (!manaCap.isPresent()) {
                player.sendSystemMessage(Component.literal("Mana capability not found!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            IMana mana = manaCap.orElseThrow(IllegalStateException::new);
            if (mana.getMana() < MANA_COST) {
                player.sendSystemMessage(Component.literal("Not enough mana! Need " + MANA_COST)
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            double targetX, targetY, targetZ;

            // Сначала проверяем, есть ли цель (сущность)
            LivingEntity targetEntity = getTargetedEntity(player);

            if (targetEntity != null) {
                // Создаем столб прямо на позиции сущности
                targetX = targetEntity.getX();
                targetY = targetEntity.getY() + targetEntity.getBbHeight() / 2.0; // по центру тела
                targetZ = targetEntity.getZ();
            } else {
                // Если цели нет - спавним на блоке, на который смотрим
                HitResult hitResult = player.pick(RANGE, 0.0F, false);

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHit = (BlockHitResult) hitResult;
                    Vec3 blockPos = blockHit.getLocation();
                    targetX = blockPos.x;
                    targetY = blockPos.y;
                    targetZ = blockPos.z;
                } else {
                    // Если не попали в блок, используем точку впереди
                    Vec3 targetPos = player.getLookAngle().scale(50).add(player.getEyePosition());
                    targetX = targetPos.x;
                    targetY = targetPos.y;
                    targetZ = targetPos.z;
                }
            }

            // Тратим ману и ставим КД
            mana.removeMana(MANA_COST);
            PlayerCooldowns.setCooldown(player, this, COOLDOWN_TICKS);

            // Спавним столб прямо на позиции цели
            FirePillarEntity pillar = new FirePillarEntity(level, targetX, targetY, targetZ, player);
            level.addFreshEntity(pillar);
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Nullable
    private static LivingEntity getTargetedEntity(Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle().scale(RANGE);
        Vec3 endPos = eyePos.add(lookVec);
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec).inflate(1.0D);
        LivingEntity bestTarget = null;
        double closestDist = RANGE;

        for (Entity entity : player.level().getEntities(player, searchBox, e -> e instanceof LivingEntity && e.isAlive() && e != player)) {
            AABB targetBox = entity.getBoundingBox().inflate(0.3);
            Optional<Vec3> hit = targetBox.clip(eyePos, endPos);
            if (hit.isPresent()) {
                double dist = eyePos.distanceTo(hit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    bestTarget = (LivingEntity) entity;
                }
            }
        }
        return bestTarget;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.fire_pillar.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.fire_pillar.desc2", MANA_COST)
                .withStyle(style -> style.withColor(0x00FFFF)));
    }
}
