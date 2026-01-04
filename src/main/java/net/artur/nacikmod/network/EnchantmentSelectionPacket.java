package net.artur.nacikmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class EnchantmentSelectionPacket {
    private final Enchantment enchantment;
    private final boolean selected;

    public EnchantmentSelectionPacket(Enchantment enchantment) {
        this.enchantment = enchantment;
        this.selected = true; // По умолчанию выбираем
    }

    public EnchantmentSelectionPacket(Enchantment enchantment, boolean selected) {
        this.enchantment = enchantment;
        this.selected = selected;
    }

    public EnchantmentSelectionPacket(FriendlyByteBuf buf) {
        ResourceLocation enchantmentId = buf.readResourceLocation();
        this.enchantment = ForgeRegistries.ENCHANTMENTS.getValue(enchantmentId);
        this.selected = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(ForgeRegistries.ENCHANTMENTS.getKey(enchantment));
        buf.writeBoolean(selected);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Обработка на сервере
            if (context.getSender() != null) {
                var menu = context.getSender().containerMenu;
                if (menu instanceof net.artur.nacikmod.gui.EnchantmentLimitTableMenu enchantmentMenu) {
                    // ✅ вместо toggle — ставим конкретное состояние
                    enchantmentMenu.setEnchantmentSelection(enchantment, selected);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
