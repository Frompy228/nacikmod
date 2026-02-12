package net.artur.nacikmod.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.client.KnightModel;
import net.artur.nacikmod.entity.client.ModModelLayers;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Внешний слой для всех рыцарей. Один слой, модель — KnightModel.
 * Knight и Knight Leader используют knight_outer_layer.png; Paladin, Archer, Caster, Boss — свои *_out_layer.png.
 */
@OnlyIn(Dist.CLIENT)
public class KnightOuterLayer<T extends LivingEntity> extends RenderLayer<T, KnightModel<T>> {
    private static final ResourceLocation DEFAULT_OUTER = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/knights/knight_outer_layer.png");

    private final KnightModel<T> model;
    private final ResourceLocation texture;

    /** Knight, Knight Leader — текстура knight_outer_layer.png */
    public KnightOuterLayer(RenderLayerParent<T, KnightModel<T>> parent, EntityModelSet modelSet) {
        this(parent, modelSet, DEFAULT_OUTER);
    }

    /** Paladin, Archer, Caster, Boss — своя текстура (knight_*_out_layer.png) */
    public KnightOuterLayer(RenderLayerParent<T, KnightModel<T>> parent, EntityModelSet modelSet, ResourceLocation outerTexture) {
        super(parent);
        this.model = new KnightModel<>(modelSet.bakeLayer(ModModelLayers.KNIGHT_OUTER_LAYER));
        this.texture = outerTexture;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        
        // Не рендерим внешний слой, если сущность невидима
        if (entity.isInvisible()) {
            return;
        }

        // 1. ПОЛУЧАЕМ РОДИТЕЛЬСКУЮ МОДЕЛЬ
        KnightModel<T> parentModel = this.getParentModel();

        // 2. СИНХРОНИЗИРУЕМ СОСТОЯНИЕ РУК
        // Копируем позы рук (ITEM, BLOCK, TOOT_HORN и т.д.), чтобы setupAnim слоя отработал правильно
        this.model.rightArmPose = parentModel.rightArmPose;
        this.model.leftArmPose = parentModel.leftArmPose;
        this.model.crouching = parentModel.crouching; // На случай, если моб будет приседать

        // 3. ПРИМЕНЯЕМ АНИМАЦИЮ К СЛОЮ
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // 4. КОПИРУЕМ ПАРАМЕТРЫ И ОТРИСОВЫВАЕМ
        // Метод copyPropertiesTo копирует базовые настройки (плавание, падение и т.д.)
        parentModel.copyPropertiesTo(this.model);

        renderColoredCutoutModel(this.model, texture, poseStack, buffer, packedLight, entity, 1.0F, 1.0F, 1.0F);
    }
}
