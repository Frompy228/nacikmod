package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.AssassinEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.artur.nacikmod.entity.layers.AssassinOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AssassinRender extends HumanoidMobRenderer<AssassinEntity, AssassinModel<AssassinEntity>> {

    // Массив доступных текстур ассасина
    // Каждый ассасин получает случайный, но постоянный скин при спавне
    private static final ResourceLocation[] ASSASSIN_TEXTURES = {
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/assassin.png"),
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/assassin_5.png"),
    };

    public AssassinRender(EntityRendererProvider.Context context) {
        super(context, new AssassinModel<>(context.bakeLayer(ModModelLayers.ASSASSIN_LAYER)), 0.5f);

        // Слой оружия в руках, с проверкой на невидимость
        this.addLayer(new AssassinItemInHandLayer(this, context.getItemInHandRenderer()));

        // Слой брони, который тоже учитывает невидимость
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AssassinEntity entity,
                               float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                               float netHeadYaw, float headPitch) {
                if (entity.isInvisible()) return; // Пропускаем рендер брони
                super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount,
                        partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
        });
        
        // Добавляем слой внешней текстуры
        this.addLayer(new AssassinOuterLayer<>(this, context.getModelSet()));
    }



    @Override
    public ResourceLocation getTextureLocation(AssassinEntity entity) {
        // Используем ID сущности для выбора текстуры, чтобы у каждого ассасина был постоянный скин
        int textureIndex = Math.abs(entity.getId()) % ASSASSIN_TEXTURES.length;
        return ASSASSIN_TEXTURES[textureIndex];
    }

    @Override
    public boolean shouldRender(AssassinEntity entity, Frustum frustum, double x, double y, double z) {
        return !entity.isInvisible() && super.shouldRender(entity, frustum, x, y, z);
    }

    // Кастомный слой оружия
    private static class AssassinItemInHandLayer extends ItemInHandLayer<AssassinEntity, AssassinModel<AssassinEntity>> {
        public AssassinItemInHandLayer(HumanoidMobRenderer<AssassinEntity, AssassinModel<AssassinEntity>> renderer, ItemInHandRenderer itemRenderer) {
            super(renderer, itemRenderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AssassinEntity entity,
                           float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                           float netHeadYaw, float headPitch) {
            if (entity.isInvisible()) return; // Не рендерим оружие
            super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks,
                    ageInTicks, netHeadYaw, headPitch);
        }
    }
}
