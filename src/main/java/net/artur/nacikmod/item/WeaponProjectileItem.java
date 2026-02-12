package net.artur.nacikmod.item;

import net.artur.nacikmod.registry.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.artur.nacikmod.entity.projectiles.WeaponProjectile;

public class WeaponProjectileItem extends Item {

    public WeaponProjectileItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        
        if (!world.isClientSide) {
            // Создаём копию меча (как в примере через copy())
            ItemStack sword = new ItemStack(Items.IRON_SWORD);
            
            // Создаём снаряд
            WeaponProjectile proj = new WeaponProjectile(ModEntities.WEAPON_PROJECTILE.get(), world);
            proj.setStack(sword.copy()); // устанавливаем копию меча в снаряд
            proj.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
            proj.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            world.addFreshEntity(proj);
        }

        return InteractionResultHolder.success(stackInHand);
    }
}