package net.artur.nacikmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.artur.nacikmod.capability.killcount.KillCountProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class KillCountCommand {
    private static final int REQUIRED_KILLS_FOR_WORLD_SLASH = 500;
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("slashstats")
                .requires(source -> source.hasPermission(0)) // Разрешаем всем игрокам
                .executes(context -> {
                    // Если команда вызвана без аргументов, показываем статистику текущего игрока
                    if (context.getSource().getEntity() instanceof ServerPlayer player) {
                        return showSlashStats(context.getSource(), player);
                    }
                    return 0;
                })
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
                            return showSlashStats(context.getSource(), targetPlayer);
                        })));
    }

    private static int showSlashStats(CommandSourceStack source, ServerPlayer player) {
        player.getCapability(KillCountProvider.KILL_COUNT_CAPABILITY).ifPresent(killCount -> {
            int currentKills = killCount.getSlashKills();
            boolean hasReceivedReward = killCount.hasReceivedWorldSlashReward();
            
            source.sendSuccess(() -> Component.literal("=== Slash Statistics for " + player.getName().getString() + " ===")
                    .withStyle(ChatFormatting.GOLD), false);
            
            // Показываем статус получения награды в первую очередь
            if (hasReceivedReward) {
                source.sendSuccess(() -> Component.literal("✅ World Slash REWARD ALREADY RECEIVED!")
                        .withStyle(ChatFormatting.GREEN), false);
                source.sendSuccess(() -> Component.literal("Current slash kills: " + currentKills + " (after receiving reward)")
                        .withStyle(ChatFormatting.GRAY), false);
            } else {
                int remainingKills = Math.max(0, REQUIRED_KILLS_FOR_WORLD_SLASH - currentKills);
                source.sendSuccess(() -> Component.literal("❌ World Slash reward NOT RECEIVED yet")
                        .withStyle(ChatFormatting.RED), false);
                source.sendSuccess(() -> Component.literal("Current slash kills: " + currentKills)
                        .withStyle(ChatFormatting.GREEN), false);
                source.sendSuccess(() -> Component.literal("Kills needed for World Slash: " + remainingKills)
                        .withStyle(ChatFormatting.YELLOW), false);
                
                if (currentKills > 0) {
                    double progress = (double) currentKills / REQUIRED_KILLS_FOR_WORLD_SLASH * 100;
                    source.sendSuccess(() -> Component.literal("Progress: " + String.format("%.1f", progress) + "%")
                            .withStyle(ChatFormatting.AQUA), false);
                }
            }
            
            // Дополнительная информация
            source.sendSuccess(() -> Component.literal("Required kills for World Slash: " + REQUIRED_KILLS_FOR_WORLD_SLASH)
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        });
        return 1;
    }
} 