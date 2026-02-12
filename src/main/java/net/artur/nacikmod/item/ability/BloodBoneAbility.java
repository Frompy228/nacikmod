package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.item.BloodBone;
import net.artur.nacikmod.network.AbilityStateManager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class BloodBoneAbility {
    private static final String ACTIVE_TAG = "active";

    public static void startBloodBone(Player player) {
        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            if (!mana.isBloodBoneActive()) {
                mana.setBloodBoneActive(true);
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    AbilityStateManager.syncAbilityState(serverPlayer, "blood_bone", true);
                }
            }
        });
    }

    public static void stopBloodBone(Player player) {
        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            if (mana.isBloodBoneActive()) {
                mana.setBloodBoneActive(false);
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    AbilityStateManager.syncAbilityState(serverPlayer, "blood_bone", false);
                }
            }
        });
    }

    public static boolean isBloodBoneActive(Player player) {
        return player.getCapability(ManaProvider.MANA_CAPABILITY)
                .map(mana -> mana.isBloodBoneActive())
                .orElse(false);
    }
}
