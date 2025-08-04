package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.projectiles.SuppressingGate;
import net.artur.nacikmod.util.PlayerCooldowns;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AncientSeal extends Item {
    private static final int MANA_COST = 900;
    private static final int COOLDOWN_TICKS = 340;

    public static final Rarity ANCIENT_GOLD = Rarity.create("ANCIENT_GOLD", ChatFormatting.GOLD);

    public AncientSeal(Properties properties) {
        super(new Item.Properties().rarity(ANCIENT_GOLD).stacksTo(1)
        );
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
            HitResult hitResult = player.pick(150.0D, 0.0F, false);
            Vec3 targetPos;
            
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hitResult;
                targetPos = blockHit.getLocation();
            } else if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) hitResult;
                targetPos = entityHit.getLocation();
            } else {
                // If no block or entity hit, use a point 50 blocks away
                targetPos = player.getLookAngle().scale(50).add(player.getEyePosition());
            }

            // Create SuppressingGate at high altitude above target
            double spawnY = (targetPos.y + 50);
            
            // Calculate the direction from player to target
            Vec3 playerPos = player.getEyePosition();
            Vec3 direction = targetPos.subtract(playerPos).normalize();
            float yaw = (float) (Math.atan2(-direction.x, direction.z) * 180.0 / Math.PI);
            
            SuppressingGate gate = new SuppressingGate(level, targetPos.x, spawnY, targetPos.z, yaw, player);
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
    
}
