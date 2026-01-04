package net.artur.nacikmod.network;

import net.artur.nacikmod.registry.ModMessages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.artur.nacikmod.item.ability.ManaRelease;
import net.artur.nacikmod.item.ability.HundredSealAbility;
import net.artur.nacikmod.item.ability.SimpleDomainAbility;
import net.artur.nacikmod.item.ability.DomainAbility;
import net.artur.nacikmod.item.ability.VisionBlessingAbility;
import net.artur.nacikmod.registry.ModEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class AbilityStateManager {
    
    // Реестр способностей для автоматического управления
    private static final Map<String, AbilityInfo> ABILITY_REGISTRY = new HashMap<>();
    
    static {
        // Регистрируем способности
        registerAbility("release", 
            ManaRelease::isReleaseActive,
            player -> getReleaseLevel(player));
            
        registerAbility("last_magic", 
            player -> player.hasEffect(ModEffects.MANA_LAST_MAGIC.get()),
            player -> 0); // LastMagic не имеет уровней
            
        registerAbility("hundred_seal", 
            HundredSealAbility::isHundredSealActive,
            player -> 0); // HundredSeal не имеет уровней
            
        registerAbility("simple_domain", 
            SimpleDomainAbility::isSimpleDomainActive,
            player -> 0); // SimpleDomain не имеет уровней
            
        registerAbility("domain", 
            DomainAbility::isDomainActive,
            player -> 0); // Domain не имеет уровней
            
        registerAbility("kodai", 
            VisionBlessingAbility::isKodaiActive,
            player -> 0); // Kodai не имеет уровней
    }
    
    public static class AbilityInfo {
        public final Function<Player, Boolean> stateGetter;
        public final Function<Player, Integer> levelGetter;
        
        public AbilityInfo(Function<Player, Boolean> stateGetter, Function<Player, Integer> levelGetter) {
            this.stateGetter = stateGetter;
            this.levelGetter = levelGetter;
        }
    }
    
    /**
     * Регистрирует новую способность в системе
     */
    public static void registerAbility(String abilityId, Function<Player, Boolean> stateGetter, Function<Player, Integer> levelGetter) {
        ABILITY_REGISTRY.put(abilityId, new AbilityInfo(stateGetter, levelGetter));
    }
    
    /**
     * Отправляет текущее состояние всех способностей игрока всем игрокам поблизости
     */
    public static void syncPlayerAbilities(ServerPlayer player) {
        Map<String, Boolean> abilityStates = new HashMap<>();
        Map<String, Integer> abilityLevels = new HashMap<>();
        
        // Собираем состояния всех зарегистрированных способностей
        for (Map.Entry<String, AbilityInfo> entry : ABILITY_REGISTRY.entrySet()) {
            String abilityId = entry.getKey();
            AbilityInfo info = entry.getValue();
            
            abilityStates.put(abilityId, info.stateGetter.apply(player));
            abilityLevels.put(abilityId, info.levelGetter.apply(player));
        }
        
        // Отправляем пакет
        ModMessages.sendAbilityStateToNearbyPlayers(player, abilityStates, abilityLevels);
    }
    
    /**
     * Отправляет обновление конкретной способности
     */
    public static void syncAbilityState(ServerPlayer player, String ability, boolean isActive) {
        Map<String, Boolean> abilityStates = new HashMap<>();
        Map<String, Integer> abilityLevels = new HashMap<>();
        
        // Получаем текущие состояния всех способностей
        for (Map.Entry<String, AbilityInfo> entry : ABILITY_REGISTRY.entrySet()) {
            String abilityId = entry.getKey();
            AbilityInfo info = entry.getValue();
            
            abilityStates.put(abilityId, info.stateGetter.apply(player));
            abilityLevels.put(abilityId, info.levelGetter.apply(player));
        }
        
        // Обновляем конкретную способность
        abilityStates.put(ability, isActive);
        
        // Отправляем пакет
        ModMessages.sendAbilityStateToNearbyPlayers(player, abilityStates, abilityLevels);
    }
    
    /**
     * Отправляет обновление уровня способности
     */
    public static void syncAbilityLevel(ServerPlayer player, String ability, int level) {
        Map<String, Boolean> abilityStates = new HashMap<>();
        Map<String, Integer> abilityLevels = new HashMap<>();
        
        // Получаем текущие состояния всех способностей
        for (Map.Entry<String, AbilityInfo> entry : ABILITY_REGISTRY.entrySet()) {
            String abilityId = entry.getKey();
            AbilityInfo info = entry.getValue();
            
            abilityStates.put(abilityId, info.stateGetter.apply(player));
            abilityLevels.put(abilityId, info.levelGetter.apply(player));
        }
        
        // Обновляем конкретный уровень
        abilityLevels.put(ability, level);
        
        // Отправляем пакет
        ModMessages.sendAbilityStateToNearbyPlayers(player, abilityStates, abilityLevels);
    }
    
    private static int getReleaseLevel(Player player) {
        for (var stack : player.getInventory().items) {
            if (stack.getItem() instanceof net.artur.nacikmod.item.Release && 
                stack.hasTag() && 
                stack.getTag().getBoolean("active")) {
                return stack.getTag().getInt("level");
            }
        }
        return 0;
    }
} 