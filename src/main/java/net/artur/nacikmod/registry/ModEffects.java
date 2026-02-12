package net.artur.nacikmod.registry;

import net.artur.nacikmod.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.artur.nacikmod.NacikMod;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, NacikMod.MOD_ID);
    // Эффект отключения регенерации
    public static final RegistryObject<MobEffect> NO_REGEN =
            EFFECTS.register("no_regen", NoRegenerationEffect::new);
    // Эффект уменьшения максимального ХП
    public static final RegistryObject<MobEffect> HEALTH_REDUCTION =
            EFFECTS.register("health_reduction", EffectHealthReduction::new);
    public static final RegistryObject<MobEffect> ARMOR_REDUCTION =
            EFFECTS.register("armor_reduction", EffectArmorReduction::new);
    public static final RegistryObject<MobEffect> LOVE =
            EFFECTS.register("love", EffectLove::new);
    public static final RegistryObject<MobEffect> TIME_SLOW =
            EFFECTS.register("time_slow", EffectTimeSlow::new);
    public static final RegistryObject<MobEffect> ROOT =
            EFFECTS.register("root", EffectRoot::new);
    public static final RegistryObject<MobEffect> MANA_LAST_MAGIC =
            EFFECTS.register("mana_last_magic", EffectManaLastMagic::new);
    public static final RegistryObject<MobEffect> BLOOD_EXPLOSION =
            EFFECTS.register("blood_explosion", EffectBloodExplosion::new);
    public static final RegistryObject<MobEffect> EFFECT_MANA_BLESSING =
            EFFECTS.register("effect_mana_blessing", EffectManablessing::new);
    public static final RegistryObject<MobEffect> ROAR =
            EFFECTS.register("roar", EffectRoar::new);
    public static final RegistryObject<MobEffect> ENHANCED_GRAVITY =
            EFFECTS.register("enhanced_gravity", EffectEnhancedGravity::new);
    public static final RegistryObject<MobEffect> TRUE_SIGHT =
            EFFECTS.register("true_sight", EffectTrueSight::new);
    public static final RegistryObject<MobEffect> CURSED_SWORD =
            EFFECTS.register("cursed_sword", EffectCursedSword::new);
    public static final RegistryObject<MobEffect> STRONG_POISON =
            EFFECTS.register("strong_poison", EffectStrongPoison::new);
    public static final RegistryObject<MobEffect> GOD_HAND =
            EFFECTS.register("god_hand", EffectGodHand::new);
    public static final RegistryObject<MobEffect> SUPPRESSING_GATE =
            EFFECTS.register("suppressing_gate", EffectSuppressingGate::new);
    public static final RegistryObject<MobEffect> EFFECT_SIMPLE_DOMAIN =
            EFFECTS.register("effect_simple_domain", EffectSimpleDomain::new);
    public static final RegistryObject<MobEffect> EFFECT_DOMAIN =
            EFFECTS.register("effect_domain", EffectDomain::new);
    public static final RegistryObject<MobEffect> EFFECT_BASE_DOMAIN =
            EFFECTS.register("effect_base_domain", EffectBaseDomain::new);
    public static final RegistryObject<MobEffect> EFFECT_MANA_SEAL =
            EFFECTS.register("effect_mana_seal", EffectManaSeal::new);
    public static final RegistryObject<MobEffect> EFFECT_BLOOD_POISONING =
            EFFECTS.register("blood_poisoning", EffectBloodPoisoning::new);
}
