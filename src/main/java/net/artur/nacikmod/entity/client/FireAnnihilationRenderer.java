// FireAnnihilationRenderer.java
package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.entity.projectiles.FireAnnihilationEntity; // Импортируйте правильную сущность
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class FireAnnihilationRenderer extends EntityRenderer<FireAnnihilationEntity> { // Измените тип

    public FireAnnihilationRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 4.0F; // Увеличьте радиус тени для большой сущности
    }

    @Override
    public ResourceLocation getTextureLocation(FireAnnihilationEntity entity) {
        // Используйте текстуру огня или прозрачную текстуру
        return new ResourceLocation("minecraft", "textures/block/fire_0.png");
    }
}