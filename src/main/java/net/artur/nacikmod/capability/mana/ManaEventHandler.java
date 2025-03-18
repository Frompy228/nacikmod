package net.artur.nacikmod.capability.mana;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.MobClass.HeroSouls;
import net.artur.nacikmod.network.ModMessages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class ManaEventHandler {

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player || event.getObject() instanceof HeroSouls) {
            ManaCapability provider = new ManaCapability();
            event.addCapability(new ResourceLocation(NacikMod.MOD_ID, "mana"), provider);
            event.addListener(provider::invalidate);
        }
    }

    private static void updateManaData(Player player){
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(mana -> {

                CompoundTag playerData = serverPlayer.getPersistentData();


                if (playerData.contains("ManaData")) {
                    CompoundTag manaData = playerData.getCompound("ManaData");
                    mana.setMana(manaData.getInt("Mana"));
                    mana.setMaxMana(manaData.getInt("MaxMana"));
                } else {
                    mana.setMana(100);
                    mana.setMaxMana(100);
                }


                ModMessages.sendManaToClient(serverPlayer, mana.getMana(), mana.getMaxMana());
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag oldData = event.getOriginal().getPersistentData();
        CompoundTag newData = event.getEntity().getPersistentData();
        if (oldData.contains("ManaData")) {
            newData.put("ManaData", oldData.getCompound("ManaData"));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // После респауна загружаем данные из persistentData (уже скопированные onPlayerClone)
            serverPlayer.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(mana -> {
                CompoundTag playerData = serverPlayer.getPersistentData();
                if (playerData.contains("ManaData")) {
                    CompoundTag manaData = playerData.getCompound("ManaData");
                    mana.setMana(manaData.contains("Mana") ? manaData.getInt("Mana") : 100);
                    mana.setMaxMana(manaData.contains("MaxMana") ? manaData.getInt("MaxMana") : 100);
                } else {
                    mana.setMana(100);
                    mana.setMaxMana(100);
                }
                // Синхронизируем с клиентом
                ModMessages.sendManaToClient(serverPlayer, mana.getMana(), mana.getMaxMana());
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(mana -> {

                CompoundTag playerData = serverPlayer.getPersistentData();


                if (playerData.contains("ManaData")) {
                    CompoundTag manaData = playerData.getCompound("ManaData");
                    mana.setMana(manaData.getInt("Mana"));
                    mana.setMaxMana(manaData.getInt("MaxMana"));
                } else {
                    mana.setMana(100);
                    mana.setMaxMana(100);
                }


                ModMessages.sendManaToClient(serverPlayer, mana.getMana(), mana.getMaxMana());
            });
        }
    }



    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(mana -> {
                // Сохраняем данные о мане в NBT
                CompoundTag playerData = serverPlayer.getPersistentData();
                CompoundTag manaData = new CompoundTag();
                manaData.putInt("Mana", mana.getMana());
                manaData.putInt("MaxMana", mana.getMaxMana());
                playerData.put("ManaData", manaData);
            });
        }
    }
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {

    }



    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;

            // Каждую секунду обновляем ману и сохраняем её
            if (player.tickCount % 20 == 0) {
                player.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(mana -> {
                    mana.addMana(1); // Восстанавливаем 1 ману
                });
                // Сохраняем обновленные данные в persistentData
                if (player instanceof ServerPlayer serverPlayer) {
                    // Предположим, что у вас есть доступ к методу сохранения в ManaCapability
                    new ManaCapability().saveManaToPlayerData(serverPlayer);
                }
            }
        }
    }
}
