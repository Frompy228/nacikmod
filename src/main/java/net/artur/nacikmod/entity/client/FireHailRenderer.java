package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.entity.projectiles.FireHailEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class FireHailRenderer extends EntityRenderer<FireHailEntity> {
    public FireHailRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(FireHailEntity entity) {
        // Возвращаем пустую текстуру, так как сущность невидима
        return new ResourceLocation("minecraft", "textures/block/air.png");
    }
} 