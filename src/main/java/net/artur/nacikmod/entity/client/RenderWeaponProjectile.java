package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.artur.nacikmod.entity.projectiles.WeaponProjectile;
import org.joml.Quaternionf;

public class RenderWeaponProjectile extends EntityRenderer<WeaponProjectile> {

    public RenderWeaponProjectile(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(WeaponProjectile weapon, float entityYaw, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int packedLight) {
        matrix.pushPose();
        
        float scale = 2.5F; // Масштаб как в примере
        
        // Интерполяция углов как в примере (используем старые значения для плавности)
        float ryaw = 90.0F + weapon.yRotO + (weapon.getYRot() - weapon.yRotO) * partialTicks;
        float rpitch = 135.0F - (weapon.xRotO + (weapon.getXRot() - weapon.xRotO) * partialTicks);
        
        // Вращение по Y (горизонтальное)
        matrix.mulPose(new Quaternionf().rotateY((float)Math.toRadians(ryaw)));
        // Вращение по Z (вертикальное)
        matrix.mulPose(new Quaternionf().rotateZ((float)Math.toRadians(rpitch)));
        
        // Сдвиг для правильного позиционирования модели
        matrix.translate(-0.59D, -0.59D, 0.0D);
        
        // Масштабирование
        matrix.scale(scale, scale, scale);
        
        // Рендер предмета
        ItemStack stack = weapon.getStack();
        if (!stack.isEmpty()) {
            try {
                BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, weapon.tickCount);
                Minecraft.getInstance().getItemRenderer().render(
                    stack, 
                    ItemDisplayContext.GROUND, 
                    false, 
                    matrix, 
                    buffer, 
                    packedLight, 
                    OverlayTexture.NO_OVERLAY, 
                    bakedModel
                );
            } catch (Exception exception) {
                // Игнорируем ошибки рендеринга
            }
        }
        
        matrix.popPose();
        
        super.render(weapon, entityYaw, partialTicks, matrix, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(WeaponProjectile entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}