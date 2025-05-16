package net.artur.nacikmod.client.renderer;

import net.artur.nacikmod.armor.models.LeonidHelmetModel;
import net.artur.nacikmod.entity.client.ModModelLayers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class LeonidHelmetRenderer implements IClientItemExtensions {
    private static boolean init;
    private static LeonidHelmetModel HELMET_MODEL;

    public static void initializeModels() {
        init = true;
        HELMET_MODEL = new LeonidHelmetModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModModelLayers.LEONID_HELMET_LAYER));
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> _default) {
        if (!init) {
            initializeModels();
        }
        return HELMET_MODEL;
    }
} 