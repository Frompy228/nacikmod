package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.projectiles.SuppressingGate;
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
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class AncientSeal extends Item {
    private static final int MANA_COST = 900;
    private static final int COOLDOWN_TICKS = 340;

    public static final Rarity ANCIENT_GOLD = Rarity.create("ANCIENT_GOLD", ChatFormatting.GOLD);

    public AncientSeal(Properties properties) {
        super(new Item.Properties().rarity(ANCIENT_GOLD).stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Check if player has enough mana
            if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                player.sendSystemMessage(Component.literal("Not enough mana!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Check cooldown using our custom system
            if (PlayerCooldowns.isOnCooldown(player, this)) {
                int left = PlayerCooldowns.getCooldownLeft(player, this);
                player.sendSystemMessage(Component.literal("Item is on cooldown! (" + left / 20 + "s left)")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Get target position where player is looking
            // Сначала ищем сущность в поле зрения
            LivingEntity targetEntity = getTargetedEntity(player, 150.0D);
            Vec3 targetPos;

            if (targetEntity != null) {
                targetPos = new Vec3(targetEntity.getX(), targetEntity.getY() + targetEntity.getBbHeight() / 2.0, targetEntity.getZ());
            } else {
                HitResult hitResult = player.pick(150.0D, 0.0F, false);
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    targetPos = ((BlockHitResult) hitResult).getLocation();
                } else {
                    targetPos = player.getLookAngle().scale(50).add(player.getEyePosition());
                }
            }


            // Create SuppressingGate at high altitude above target
            double spawnY = (targetPos.y + 50);
            
            // Calculate the direction from player to target
            Vec3 playerPos = player.getEyePosition();
            Vec3 direction = targetPos.subtract(playerPos).normalize();
            float yaw = (float) (Math.atan2(-direction.x, direction.z) * 180.0 / Math.PI);

            SuppressingGate gate = new SuppressingGate(level, targetPos.x, spawnY, targetPos.z, yaw, player.getUUID());
            level.addFreshEntity(gate);

            // Consume mana and set cooldown using our custom system
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));
            PlayerCooldowns.setCooldown(player, this, COOLDOWN_TICKS);
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.ancient_seal.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.ancient_seal.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Nullable
    private static LivingEntity getTargetedEntity(Player player, double range) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle().scale(range);
        Vec3 endPos = eyePos.add(lookVec);
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec).inflate(1.0D);
        LivingEntity bestTarget = null;
        double closestDist = range;

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

}
