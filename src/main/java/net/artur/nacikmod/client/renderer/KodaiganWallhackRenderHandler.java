package net.artur.nacikmod.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.item.ability.VisionBlessingAbility;
import net.artur.nacikmod.network.PlayerManaCache;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = NacikMod.MOD_ID)
public class KodaiganWallhackRenderHandler {

    private static final double RENDER_RADIUS = 64.0;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!VisionBlessingAbility.isKodaiActive(mc.player)) return;

        Camera camera = event.getCamera();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Vec3 cameraPos = camera.getPosition();
        float partialTick = event.getPartialTick();

        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        AABB boundingBox = mc.player.getBoundingBox().inflate(RENDER_RADIUS);

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        for (LivingEntity entity : mc.level.getEntitiesOfClass(LivingEntity.class, boundingBox)) {
            if (!isValidTarget(entity, mc.player)) continue;
            renderEntityModel(dispatcher, entity, poseStack, bufferSource, partialTick);
        }
        bufferSource.endBatch();
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        float viewY = camera.getYRot();
        float viewX = camera.getXRot();
        for (LivingEntity entity : mc.level.getEntitiesOfClass(LivingEntity.class, boundingBox)) {
            if (!isValidTarget(entity, mc.player)) continue;
            renderEntityHUD(mc, entity, poseStack, partialTick, viewY, viewX);
        }
        poseStack.popPose();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static boolean isValidTarget(LivingEntity entity, Player localPlayer) {
        if (entity == localPlayer) return false;
        if (!entity.isAlive() || entity.isRemoved()) return false;
        if (entity instanceof ArmorStand) return false;
        String entityName = entity.getType().getDescriptionId().toLowerCase();
        if (entityName.contains("cloud") || entityName.contains("effect") || entityName.contains("projectile")) return false;
        return (entity instanceof Player) || (entity instanceof net.minecraft.world.entity.Mob);
    }

    private static void renderEntityModel(EntityRenderDispatcher dispatcher, LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, float partialTick) {
        EntityRenderer<? super LivingEntity> renderer = dispatcher.getRenderer(entity);
        if (renderer == null) return;
        double x = entity.xo + (entity.getX() - entity.xo) * partialTick;
        double y = entity.yo + (entity.getY() - entity.yo) * partialTick;
        double z = entity.zo + (entity.getZ() - entity.zo) * partialTick;
        float yRot = entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTick;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        renderer.render(entity, yRot, partialTick, poseStack, buffer, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    private static void renderEntityHUD(Minecraft mc, LivingEntity entity, PoseStack poseStack, float partialTick, float viewY, float viewX) {
        double x = entity.xo + (entity.getX() - entity.xo) * partialTick;
        double y = entity.yo + (entity.getY() - entity.yo) * partialTick;
        double z = entity.zo + (entity.getZ() - entity.zo) * partialTick;

        poseStack.pushPose();
        // Позиция над головой
        poseStack.translate(x, y + entity.getBbHeight() + 0.6, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(-viewY));
        poseStack.mulPose(Axis.XP.rotationDegrees(viewX));

        float scale = 0.025f;
        poseStack.scale(-scale, -scale, scale);

        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        Font font = mc.font;

        // --- БЛОК HP ---
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float hpPercent = Math.max(0, Math.min(health / maxHealth, 1.0f));

        // 1. Текст HP (СВЕРХУ полоски)
        String hpText = String.format("%.0f / %.0f", health, maxHealth);
        font.drawInBatch(hpText, -font.width(hpText) / 2f, -10, 0xFFFFFFFF, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, 15728880);

        // 2. Полоска HP (Центральная)
        drawRect(buffer, matrix, -20, 0, 20, 2, 0xAA000000);
        int hpColor = hpPercent > 0.5 ? 0xFF00FF00 : (hpPercent > 0.2 ? 0xFFFFFF00 : 0xFFFF0000);
        drawRect(buffer, matrix, -20, 0, -20 + (40 * hpPercent), 2, hpColor);

        // --- БЛОК МАНЫ ---
        int currentMana = 0;
        int maxMana = 0;

        if (entity == mc.player) {
            var cap = entity.getCapability(ManaProvider.MANA_CAPABILITY).orElse(null);
            if (cap != null) {
                currentMana = cap.getMana();
                maxMana = cap.getMaxMana();
            }
        } else {
            PlayerManaCache.ManaData manaData = PlayerManaCache.getPlayerMana(entity.getUUID());
            if (manaData != null) {
                currentMana = manaData.mana;
                maxMana = manaData.maxMana;
            }
        }

        if (maxMana > 0) {
            float manaPercent = (float) currentMana / maxMana;

            // 3. Полоска маны (СНИЗУ под HP, с отступом)
            // Координаты 6 и 8 создают зазор в 4 пикселя от полоски HP
            drawRect(buffer, matrix, -20, 6, 20, 8, 0xAA000000);
            drawRect(buffer, matrix, -20, 6, -20 + (40 * manaPercent), 8, 0xFF00AAFF);

            // 4. Текст маны (СНИЗУ под полоской маны)
            // Координата 11 опускает текст прямо под синюю линию
            String manaText = currentMana + " / " + maxMana;
            font.drawInBatch(manaText, -font.width(manaText) / 2f, 11, 0xFF00E5FF, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, 15728880);
        }

        buffer.endBatch();
        poseStack.popPose();
    }

    private static void drawRect(MultiBufferSource.BufferSource buffer, Matrix4f matrix, float x1, float y1, float x2, float y2, int color) {
        VertexConsumer builder = buffer.getBuffer(RenderType.gui());
        float a = (color >> 24 & 255) / 255.0f;
        float r = (color >> 16 & 255) / 255.0f;
        float g = (color >> 8 & 255) / 255.0f;
        float b = (color & 255) / 255.0f;
        builder.vertex(matrix, x1, y2, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x2, y2, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x2, y1, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x1, y1, 0).color(r, g, b, a).endVertex();
    }
}