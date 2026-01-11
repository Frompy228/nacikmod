package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.custom.GraalEntity;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.worldgen.dimension.ModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Pocket extends Item {
    private static final BlockPos POCKET_POS = new BlockPos(4, 108, 4);
    private static final int MANA_COST = 750;
    private static final double RANGE = 5.0;

    public Pocket(Properties properties) {
        super(properties.fireResistant());
    }

    private static class SimpleTeleporter implements ITeleporter {
        private final BlockPos targetPos;

        public SimpleTeleporter(BlockPos targetPos) {
            this.targetPos = targetPos;
        }

        @Override
        public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld,
                                float yaw, Function<Boolean, Entity> repositionEntity) {
            entity = repositionEntity.apply(false);
            entity.teleportTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            return entity;
        }
    }

    private void teleportEntity(Entity entity, Level level, ItemStack itemStack, Player teleporter) {
        // Запрещаем телепортировать эндер дракона и его части
        if (entity instanceof EnderDragon || entity instanceof EnderDragonPart) {
            teleporter.sendSystemMessage(Component.literal("You cannot teleport the Ender Dragon!").withStyle(ChatFormatting.RED));
            return;
        }
        // Запрещаем телепортировать Graal Entity
        if (entity instanceof GraalEntity || entity.getType() == ModEntities.GRAIL.get()) {
            teleporter.sendSystemMessage(Component.literal("You cannot teleport the Grail!").withStyle(ChatFormatting.RED));
            return;
        }
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (level.dimension().equals(ModDimensions.POCKET_LEVEL_KEY)) {
                // Если в Спарте, возвращаем в предыдущее измерение
                String sourceDim = itemStack.getOrCreateTag().getString("SourceDimension");
                if (!sourceDim.isEmpty()) {
                    ResourceKey<Level> sourceDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(sourceDim));
                    ServerLevel targetLevel = serverLevel.getServer().getLevel(sourceDimension);
                    if (targetLevel != null) {
                        BlockPos returnPos = new BlockPos(
                            itemStack.getOrCreateTag().getInt("ReturnX"),
                            itemStack.getOrCreateTag().getInt("ReturnY"),
                            itemStack.getOrCreateTag().getInt("ReturnZ")
                        );
                        entity.changeDimension(targetLevel, new SimpleTeleporter(returnPos));
                    }
                }
            } else {
                // Телепортируем в Спарту
                ServerLevel spartaLevel = serverLevel.getServer().getLevel(ModDimensions.POCKET_LEVEL_KEY);
                if (spartaLevel != null) {
                    // Сохраняем текущее измерение и позицию
                    itemStack.getOrCreateTag().putString("SourceDimension", level.dimension().location().toString());
                    itemStack.getOrCreateTag().putInt("ReturnX", entity.blockPosition().getX());
                    itemStack.getOrCreateTag().putInt("ReturnY", entity.blockPosition().getY());
                    itemStack.getOrCreateTag().putInt("ReturnZ", entity.blockPosition().getZ());
                    
                    entity.changeDimension(spartaLevel, new SimpleTeleporter(POCKET_POS));
                }
            }
        }
    }

    @Nullable
    private Entity getTargetedEntity(Player player) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.x * RANGE, lookVec.y * RANGE, lookVec.z * RANGE);
        
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec.x * RANGE, lookVec.y * RANGE, lookVec.z * RANGE).inflate(1.0D);
        
        Entity targetEntity = null;
        double minDistance = RANGE;
        
        for (Entity entity : player.level().getEntities(player, searchBox, entity -> 
            entity != player && entity.isAlive() && entity.isPickable())) {
            // Пропускаем эндер дракона и его части
            if (entity instanceof EnderDragon || entity instanceof EnderDragonPart) continue;
            // Пропускаем Graal Entity
            if (entity instanceof GraalEntity || entity.getType() == ModEntities.GRAIL.get()) continue;
            AABB entityBox = entity.getBoundingBox().inflate(0.3D);
            Optional<Vec3> hitVec = entityBox.clip(eyePos, endPos);
            
            if (hitVec.isPresent()) {
                double distance = eyePos.distanceTo(hitVec.get());
                if (distance < minDistance) {
                    minDistance = distance;
                    targetEntity = entity;
                }
            }
        }
        
        return targetEntity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // Проверяем наличие Dark Sphere в слотах Curios используя новый API
            boolean hasDarkSphere = CuriosApi.getCuriosInventory(player)
                    .map(handler -> {
                        for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                            for (int i = 0; i < stacksHandler.getSlots(); i++) {
                                ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                                if (stack.getItem() == ModItems.DARK_SPHERE.get()) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    })
                    .orElse(false);

            if (!hasDarkSphere) {
                player.sendSystemMessage(Component.literal("You need Dark Sphere to use this ability!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }
            
            if (player.isShiftKeyDown()) {
                // Проверяем наличие маны
                if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                    player.sendSystemMessage(Component.literal("Not enough mana!")
                            .withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.fail(itemStack);
                }

                // Используем ману
                player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));
                
                // Телепортируем игрока
                teleportEntity(player, level, itemStack, player);
                
                player.sendSystemMessage(Component.literal(level.dimension().equals(ModDimensions.POCKET_LEVEL_KEY) 
                    ? "Returned to previous dimension!" 
                    : "Teleported to Pocket!")
                        .withStyle(ChatFormatting.GREEN));
                
                player.getCooldowns().addCooldown(this, 20);
            } else {
                // Проверяем, смотрит ли игрок на сущность
                Entity targetEntity = getTargetedEntity(player);
                
                if (targetEntity != null) {
                    // Проверяем наличие маны
                    if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                        player.sendSystemMessage(Component.literal("Not enough mana!")
                                .withStyle(ChatFormatting.RED));
                        return InteractionResultHolder.fail(itemStack);
                    }

                    // Используем ману
                    player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));
                    
                    // Телепортируем сущность
                    teleportEntity(targetEntity, level, itemStack, player);
                    
                    player.sendSystemMessage(Component.literal("Teleported " + targetEntity.getName().getString() + "!")
                            .withStyle(ChatFormatting.GREEN));
                    
                    player.getCooldowns().addCooldown(this, 60);
                }
            }
        }
        
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("item.nacikmod.pocket.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.pocket.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));
        tooltipComponents.add(Component.translatable("item.nacikmod.pocket.desc3")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.nacikmod.pocket.desc4")
                .withStyle(ChatFormatting.GRAY));
    }
}
