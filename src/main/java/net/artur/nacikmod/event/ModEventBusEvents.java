package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.*;
import net.artur.nacikmod.network.ModMessages;
import net.artur.nacikmod.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PlayMessages;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    // Регистрация атрибутов кастомных мобов
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.LANSER.get(), LanserEntity.createAttributes().build());
        event.put(ModEntities.LEONID.get(), LeonidEntity.createAttributes().build());
        event.put(ModEntities.SPARTAN.get(), SpartanEntity.createAttributes().build());
        event.put(ModEntities.BERSERK.get(), BerserkerEntity.createAttributes().build());
        event.put(ModEntities.RED_BERSERK.get(), RedBerserkerEntity.createAttributes().build());
        event.put(ModEntities.ARCHER.get(), ArcherEntity.createAttributes().build());
        event.put(ModEntities.ASSASSIN.get(), AssassinEntity.createAttributes().build());
        event.put(ModEntities.MYSTERIOUS_TRADER.get(), MysteriousTraderEntity.createAttributes().build());
        event.put(ModEntities.MYSTERIOUS_TRADER_BATTLE_CLONE.get(), MysteriousTraderBattleCloneEntity.createAttributes().build());
        event.put(ModEntities.BLOOD_WARRIOR.get(), BloodWarriorEntity.createAttributes().build());
        event.put(ModEntities.GRAIL.get(), GraalEntity.createAttributes().build());
        event.put(ModEntities.INQUISITOR.get(), InquisitorEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void entitySpawnRestriction(SpawnPlacementRegisterEvent event) {
        event.register(ModEntities.LANSER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.LEONID.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.BERSERK.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.RED_BERSERK.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.ARCHER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.ASSASSIN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.INQUISITOR.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.MYSTERIOUS_TRADER.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MysteriousTraderEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}