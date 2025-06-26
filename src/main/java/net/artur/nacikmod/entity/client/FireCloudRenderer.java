package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.entity.projectiles.FireCloudEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class FireCloudRenderer extends EntityRenderer<FireCloudEntity> {

    public FireCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(FireCloudEntity entity) {
        // Возвращаем пустую текстуру, так как облако невидимо
        return new ResourceLocation("minecraft", "textures/block/air.png");
    }

} 