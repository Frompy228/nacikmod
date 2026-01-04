package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.entity.projectiles.FirePillarEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class FirePillarRenderer extends EntityRenderer<FirePillarEntity> {

    public FirePillarRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(FirePillarEntity entity) {
        // Возвращаем пустую текстуру, так как столб огня состоит только из частиц
        return new ResourceLocation("minecraft", "textures/block/air.png");
    }

}

