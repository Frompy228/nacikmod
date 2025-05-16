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
    public static final RegistryObject<MobEffect> HIRAISHIN_MARK =
            EFFECTS.register("hiraishin_mark", EffectHiraishinMark::new);
    public static final RegistryObject<MobEffect> ROAR =
            EFFECTS.register("roar", EffectRoar::new);
}
