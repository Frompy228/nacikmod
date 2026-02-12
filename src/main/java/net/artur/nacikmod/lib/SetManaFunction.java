package net.artur.nacikmod.lib;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.artur.nacikmod.item.MagicCrystal;
import net.artur.nacikmod.registry.ModLootFunctions;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetManaFunction extends LootItemConditionalFunction {
    final NumberProvider range;

    protected SetManaFunction(LootItemCondition[] conditions, NumberProvider range) {
        super(conditions);
        this.range = range;
    }

    @Override
    public LootItemFunctionType getType() {
        return ModLootFunctions.SET_MANA.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        int mana = this.range.getInt(context);
        MagicCrystal.setStoredMana(stack, mana);
        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> setMana(NumberProvider range) {
        return simpleBuilder((conditions) -> new SetManaFunction(conditions, range));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetManaFunction> {
        @Override
        public void serialize(JsonObject json, SetManaFunction function, JsonSerializationContext context) {
            super.serialize(json, function, context);
            json.add("range", context.serialize(function.range));
        }

        // ИСПРАВЛЕНО: Порядок аргументов: JsonObject -> JsonDeserializationContext -> LootItemCondition[]
        @Override
        public SetManaFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            NumberProvider numberprovider = GsonHelper.getAsObject(json, "range", context, NumberProvider.class);
            return new SetManaFunction(conditions, numberprovider);
        }
    }
}