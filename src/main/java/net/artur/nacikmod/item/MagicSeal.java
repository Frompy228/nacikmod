package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.network.ModMessages;
import net.artur.nacikmod.network.ManaSyncPacket;
import net.artur.nacikmod.network.PacketSyncEffect;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public class MagicSeal extends Item {
    private static final int MANA_COST = 100;
    private static final int RADIUS = 5;
    private static final int EFFECT_DURATION = 140;

    public MagicSeal(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                if (mana.getMana() >= MANA_COST) {
                    // Расход маны
                    mana.removeMana(MANA_COST);
                    ModMessages.sendManaToClient(serverPlayer, mana.getMana(), mana.getMaxMana());

                    // Применение эффектов
                    applyRootEffect(level, serverPlayer);

                    // Синхронизированное удаление предмета
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                        player.getInventory().setChanged();

                        // Явная синхронизация инвентаря
                        serverPlayer.containerMenu.broadcastChanges();
                    }

                    // Синхронизация звука
                    level.playSound(null,
                            serverPlayer.getX(),
                            serverPlayer.getY(),
                            serverPlayer.getZ(),
                            SoundEvents.ILLUSIONER_CAST_SPELL,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F);
                }
            });
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void applyRootEffect(Level level, ServerPlayer owner) {
        AABB area = new AABB(owner.blockPosition()).inflate(RADIUS);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : entities) {
            if (entity == owner || !entity.isAlive()) continue;

            MobEffectInstance effect = new MobEffectInstance(
                    ModEffects.ROOT.get(),
                    EFFECT_DURATION,
                    0,
                    false,
                    true,
                    true
            );

            if (entity.canBeAffected(effect)) {
                entity.addEffect(effect);

                // **Синхронизация эффекта с клиентами**
                if (entity instanceof Player targetPlayer) {
                    ModMessages.INSTANCE.send(PacketDistributor.ALL.noArg(), new PacketSyncEffect(targetPlayer.getId(), true));
                }
            }
        }
    }
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag flag) {
        tooltip.add(net.minecraft.network.chat.Component.translatable("item.nacikmod.magic_seal.desc1"));
        tooltip.add(net.minecraft.network.chat.Component.translatable("item.nacikmod.magic_seal.desc2")
                .withStyle(style -> style.withColor(0x00FFFF))); // Цвет - голубой
    }

}