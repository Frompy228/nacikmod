package net.artur.nacikmod.mixin;

import net.artur.nacikmod.client.MoonTextureManager;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public class SkyRendererMixin {
    private static final ResourceLocation CUSTOM_MOON_TEXTURE = new ResourceLocation("nacikmod", "textures/environment/moon_vision.png");

    // Перехватываем доступ к MOON_LOCATION и подменяем на нашу текстуру
    @Redirect(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;MOON_LOCATION:Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation redirectMoonLocation() {
        if (MoonTextureManager.shouldUseCustomMoon()) {
            return CUSTOM_MOON_TEXTURE;
        }
        return new ResourceLocation("textures/environment/moon_phases.png");
    }
} 