package net.artur.nacikmod.mixin;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@OnlyIn(Dist.CLIENT) // <-- Добавляем аннотацию для клиент-специфичности
@Mixin(ItemInHandRenderer.class)
public abstract class HandMixin {

    @Shadow
    private float offHandHeight;
    @Shadow
    private ItemStack offHandItem;

    @Redirect(method = "tick", at = @At(target = "net/minecraft/util/Mth.clamp (FFF)F", value = "INVOKE", ordinal = 3))
    public float tickHand(float num, float min, float max) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return Mth.clamp(num, min, max);

        ItemStack offhand = player.getOffhandItem();
        ItemStack mainHand = player.getMainHandItem();

        double attackStrength = player.getCurrentItemAttackStrengthDelay();
        float attackProgress = Mth.clamp((float) (attackStrength / player.getAttackStrengthScale(1.0F)), 0.0F, 1.0F);

        boolean reequip = ForgeHooksClient.shouldCauseReequipAnimation(this.offHandItem, offhand, -1);
        return Mth.clamp((!reequip ? (attackProgress * attackProgress * attackProgress) : 0F) - this.offHandHeight, -0.4F, 0.4F);
    }

    @Redirect(method = "renderHandsWithItems", at = @At(target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", value = "INVOKE", ordinal = 1))
    public void renderArmWithItemOff(ItemInHandRenderer instance, AbstractClientPlayer player, float p1, float p2, InteractionHand hand, float swing, ItemStack stack, float p3, PoseStack poseStack, MultiBufferSource buffer, int light) {
        this.renderArmWithItem(player, p1, p2, hand, getOffHandSwing(p1), stack, p3, poseStack, buffer, light);
    }

    @OnlyIn(Dist.CLIENT) // <-- Аннотация для клиент-метода
    private float getOffHandSwing(float partial) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return 0F;

        InteractionHand hand = MoreObjects.firstNonNull(player.swingingArm, InteractionHand.MAIN_HAND);
        return (player.swingingArm == InteractionHand.OFF_HAND)
                ? player.getAttackAnim(partial)
                : hand == InteractionHand.OFF_HAND
                ? player.getAttackAnim(partial)
                : 0F;
    }

    @Shadow
    protected abstract void renderArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j);
}