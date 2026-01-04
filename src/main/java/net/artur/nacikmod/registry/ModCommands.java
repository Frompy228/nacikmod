package net.artur.nacikmod.registry;

import net.artur.nacikmod.command.GrailReward;
import net.artur.nacikmod.command.KillCountCommand;
import net.artur.nacikmod.command.SetMana;
import net.artur.nacikmod.command.SetMaxMana;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class ModCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SetMana.register(event.getDispatcher());
        SetMaxMana.register(event.getDispatcher());
        KillCountCommand.register(event.getDispatcher());
        GrailReward.register(event.getDispatcher(), event.getBuildContext());

    }
}