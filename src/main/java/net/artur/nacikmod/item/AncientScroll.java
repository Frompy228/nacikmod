package net.artur.nacikmod.item;

import net.artur.nacikmod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class AncientScroll extends Item {

    // Используем RegistryObject напрямую, так как они реализуют Supplier<Item>
    private static final List<RegistryObject<Item>> COMMON_POOL = List.of(
            ModItems.SLASH,
            ModItems.EARTH_STEP,
            ModItems.MAGIC_CIRCUIT
    );
    private static final List<RegistryObject<Item>> RARE_POOL = List.of(
            ModItems.DOMAIN,
            ModItems.DOUBLE_SLASH,
            ModItems.FIRE_PILLAR,
            ModItems.MANA_SEAL,
            ModItems.MANA_BLESSING,
            ModItems.BLOOD_CONTRACT
    );
    private static final List<RegistryObject<Item>> EPIC_POOL = List.of(
            ModItems.FIRE_ANNIHILATION,
            ModItems.TRIPLE_SLASH,
            ModItems.HUNDRED_SEAL,
            ModItems.VISION_BLESSING,
            ModItems.BREAKING_BODY_LIMIT,
            ModItems.BLOOD_CIRCLE_ITEM
    );

    public AncientScroll(Properties properties) {
        super(properties.stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack scroll = player.getItemInHand(hand);

        if (!level.isClientSide) {
            RandomSource random = level.getRandom();
            double roll = random.nextDouble();

            List<RegistryObject<Item>> selectedPool;
            ChatFormatting color;

            if (roll < 0.60) {
                selectedPool = COMMON_POOL;
                color = ChatFormatting.WHITE;
            } else if (roll < 0.90) {
                selectedPool = RARE_POOL;
                color = ChatFormatting.BLUE;
            } else {
                selectedPool = EPIC_POOL;
                color = ChatFormatting.LIGHT_PURPLE;
            }

            Item rewardItem = selectedPool.get(random.nextInt(selectedPool.size())).get();
            ItemStack rewardStack = new ItemStack(rewardItem);

            // --- ЛОГИКА РАНДОМНОГО КОЛИЧЕСТВА ---
            if (rewardItem == ModItems.MAGIC_CIRCUIT.get()) {
                // random.nextInt(20) выдает от 0 до 19, прибавляем 1 -> получаем от 1 до 20
                int count = random.nextInt(20) + 1;
                rewardStack.setCount(count);
            }

            if (rewardStack.isEmpty()) {
                return InteractionResultHolder.fail(scroll);
            }

            // Выдача предмета
            if (!player.getInventory().add(rewardStack)) {
                player.drop(rewardStack, false);
            }

            // --- ОБНОВЛЕННОЕ СООБЩЕНИЕ С КОЛИЧЕСТВОМ ---
            Component rewardName = Component.translatable(rewardItem.getDescriptionId());

            // Если предметов больше 1, добавляем множитель в текст (например, 15x)
            Component finalMessage = rewardStack.getCount() > 1
                    ? Component.literal(rewardStack.getCount() + "x ").append(rewardName)
                    : rewardName;

            player.displayClientMessage(
                    Component.literal("You received: ")
                            .append(finalMessage)
                            .withStyle(color),
                    true
            );

            // Звук
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!player.getAbilities().instabuild) {
                scroll.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(scroll, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("item.nacikmod.ancient_scroll.desc1"));
        tooltipComponents.add(Component.translatable("item.disappears").withStyle(ChatFormatting.GRAY));
    }
}