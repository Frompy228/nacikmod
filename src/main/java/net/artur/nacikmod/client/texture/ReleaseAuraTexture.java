package net.artur.nacikmod.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.artur.nacikmod.NacikMod;

public class ReleaseAuraTexture extends DynamicTexture {
    private static final int TEXTURE_SIZE = 128;
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/release_aura2.png");

    public ReleaseAuraTexture() {
        super(createAuraImage());
    }

    private static NativeImage createAuraImage() {
        NativeImage image = new NativeImage(TEXTURE_SIZE, TEXTURE_SIZE, false);
        
        // Calculate center point
        float centerX = TEXTURE_SIZE / 2f;
        float centerY = TEXTURE_SIZE / 2f;
        float maxDistance = (float) Math.sqrt(centerX * centerX + centerY * centerY);
        
        // Create uniform white gradient
        for (int x = 0; x < TEXTURE_SIZE; x++) {
            for (int y = 0; y < TEXTURE_SIZE; y++) {
                float distance = (float) Math.sqrt(
                    Math.pow(x - centerX, 2) + 
                    Math.pow(y - centerY, 2)
                );
                
                // Normalize distance to 0-1 range
                float normalizedDistance = distance / maxDistance;
                
                // Create uniform falloff
                float alpha = (float) (0.25 * (1.0 - normalizedDistance));
                
                // Convert to 0-255 range
                int alphaValue = (int) (alpha * 255);
                
                // Set pixel color (pure white with varying alpha)
                image.setPixelRGBA(x, y, (alphaValue << 24) | 0xFFFFFF);
            }
        }
        
        return image;
    }
} 