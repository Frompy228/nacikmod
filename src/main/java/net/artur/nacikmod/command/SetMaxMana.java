package net.artur.nacikmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;


import net.artur.nacikmod.capability.mana.ManaProvider;

public class SetMaxMana {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("setmaxmana")
                        .requires(source -> source.hasPermission(2)) // Только админ
                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    int value = IntegerArgumentType.getInteger(ctx, "value");
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                                        mana.setMaxMana(value);
                                    });
                                    ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Max mana set: " + value), true);
                                    return 1;
                                })
                        )
        );
    }
}
