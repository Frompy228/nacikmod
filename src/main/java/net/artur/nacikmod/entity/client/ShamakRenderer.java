package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.entity.projectiles.ShamakEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ShamakRenderer extends EntityRenderer<ShamakEntity> {

    public ShamakRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ShamakEntity entity) {
        // Возвращаем пустую текстуру, так как облако невидимо
        return new ResourceLocation("minecraft", "textures/block/air.png");
    }

}

