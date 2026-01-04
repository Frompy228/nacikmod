package net.artur.nacikmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.artur.nacikmod.capability.reward.PlayerRewardsProvider;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class GrailReward {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("grailreward")
                .then(Commands.argument("item", ItemArgument.item(context))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            Item item = ItemArgument.getItem(ctx, "item").getItem();
                            return executeGrailReward(ctx.getSource(), player, item);
                        })
                )
        );
    }



    private static int executeGrailReward(CommandSourceStack source, ServerPlayer player, Item chosenItem) {
        player.getCapability(PlayerRewardsProvider.PLAYER_REWARDS_CAPABILITY).ifPresent(rewards -> {

            if (rewards.hasUsedGrailReward()) {
                source.sendFailure(Component.literal("Have you already used the Grail")
                        .withStyle(ChatFormatting.RED));
                return;
            }

            // Проверяем наличие Grail
            boolean hasGrail = player.getInventory().contains(new ItemStack(ModItems.GRAIL.get()));
            if (!hasGrail) {
                source.sendFailure(Component.literal("You need an Grail to use the command")
                        .withStyle(ChatFormatting.RED));
                return;
            }

            // Удаляем один Grail
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() == ModItems.GRAIL.get() && stack.getCount() > 0) {
                    stack.shrink(1);
                    break;
                }
            }

            // Выдаем выбранный предмет в 1 экземпляре
            ItemStack rewardStack = new ItemStack(chosenItem, 1);

            boolean added = player.getInventory().add(rewardStack);
            if (!added) {
                ItemEntity entity = player.drop(rewardStack, false);
                if (entity != null) {
                    entity.setPickUpDelay(0); // можно настроить задержку
                }

            }

            rewards.setUsedGrailReward(true);
        });

        return 1;
    }
}
