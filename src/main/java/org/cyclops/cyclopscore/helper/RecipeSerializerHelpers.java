package org.cyclops.cyclopscore.helper;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Helpers related to recipe serialization.
 * @author rubensworks
 */
public class RecipeSerializerHelpers {

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static Ingredient getJsonIngredient(JsonObject json, String key, boolean required) {
        JsonElement element = json.get(key);
        if (element == null) {
            if (required) {
                throw new JsonSyntaxException("Missing " + key + ", expected to find an ingredient object or string value");
            } else {
                return Ingredient.EMPTY;
            }
        } else if (element.isJsonObject()) {
            return Ingredient.deserialize(JSONUtils.getJsonObject(json, key));
        } else if (element.isJsonArray()) {
            return Ingredient.deserialize(JSONUtils.getJsonArray(json, key));
        } else {
            String itemName = JSONUtils.getString(json, key);
            ResourceLocation resourcelocation = new ResourceLocation(itemName);
            return Ingredient.fromStacks(new ItemStack(Registry.ITEM.getOptional(resourcelocation)
                    .orElseThrow(() -> new JsonSyntaxException("Item: " + itemName + " does not exist"))));
        }
    }

    @Deprecated
    public static ItemStack getJsonItemStackOrTag(JsonObject json, boolean required) {
        return getJsonItemStackOrTag(json, required, Collections.emptyList());
    }

    public static ItemStack getJsonItemStackOrTag(JsonObject json, boolean required, List<String> modPriorities) {
        if (json.has("tag")) {
            return getJsonItemStackFromTag(json, "tag", modPriorities);
        }
        return getJsonItemStack(json, "item", required);
    }

    public static ItemStack getJsonItemStack(JsonObject json, String key, boolean required) {
        JsonElement element = json.get(key);
        if (element == null) {
            if (required) {
                throw new JsonSyntaxException("Missing " + key + ", expected to find an item object or string value");
            } else {
                return ItemStack.EMPTY;
            }
        } else if (element.isJsonObject()) {
            return ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, key));
        } else {
            String itemName = JSONUtils.getString(json, key);
            ResourceLocation resourcelocation = new ResourceLocation(itemName);
            return new ItemStack(Registry.ITEM.getOptional(resourcelocation)
                    .orElseThrow(() -> new JsonSyntaxException("Item: " + itemName + " does not exist")));
        }
    }

    @Deprecated
    public static ItemStack getJsonItemStackFromTag(JsonObject json, String key) {
        return getJsonItemStackFromTag(json, key, Collections.emptyList());
    }

    public static ItemStack getJsonItemStackFromTag(JsonObject json, String key, List<String> modPriorities) {
        // Obtain all stacks for the given tag
        ItemStack[] matchingStacks = Ingredient.deserialize(json).getMatchingStacks();

        // Create a mod id to order index map
        Map<String, Integer> modPriorityIndex = Maps.newHashMap();
        for (int i = 0; i < modPriorities.size(); i++) {
            modPriorityIndex.put(modPriorities.get(i), i);
        }

        // Sort stacks by mod id, and take first
        ItemStack outputStack = Arrays.stream(matchingStacks)
                .min(Comparator.comparingInt(e -> modPriorityIndex.getOrDefault(
                        e.getItem().getRegistryName().getNamespace(),
                        Integer.MAX_VALUE
                )))
                .orElseThrow(() -> new IllegalStateException("No tag value found for " + key + " does not exist"))
                .copy();

        // Determine count
        int count = 1;
        if (json.has("count")) {
            count = json.get("count").getAsInt();
        }
        outputStack.setCount(count);

        return outputStack;
    }

    public static FluidStack deserializeFluidStack(JsonObject json, boolean readNbt) {
        if (json.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
        }

        // Read Fluid
        String fluidName = JSONUtils.getString(json, "fluid");
        ResourceLocation resourcelocation = new ResourceLocation(fluidName);
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourcelocation);
        if (fluid instanceof EmptyFluid) {
            throw new JsonParseException("Unknown fluid '" + fluidName + "'");
        }

        // Read NBT
        CompoundNBT tag = null;
        if (readNbt && json.has("nbt")) {
            try {
                JsonElement element = json.get("nbt");
                if (element.isJsonObject()) {
                    tag = JsonToNBT.getTagFromJson(GSON.toJson(element));
                } else {
                    tag = JsonToNBT.getTagFromJson(JSONUtils.getString(element, "nbt"));
                }
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException("Invalid NBT Entry: " + e.toString());
            }
        }

        // Read amount
        int amount = FluidHelpers.BUCKET_VOLUME;
        if (json.has("amount")) {
            amount = JSONUtils.getInt(json, "amount");
        }

        return new FluidStack(fluid, amount, tag);
    }

    public static FluidStack getJsonFluidStack(JsonObject json, String key, boolean required) {
        JsonElement element = json.get(key);
        if (element == null) {
            if (required) {
                throw new JsonSyntaxException("Missing " + key + ", expected to find a fluid object or string value");
            } else {
                return FluidStack.EMPTY;
            }
        } else if (element.isJsonObject()) {
            return deserializeFluidStack(JSONUtils.getJsonObject(json, key), true);
        } else {
            String fluidName = JSONUtils.getString(json, key);
            ResourceLocation resourcelocation = new ResourceLocation(fluidName);
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourcelocation);
            if (fluid instanceof EmptyFluid) {
                throw new JsonParseException("Unknown fluid '" + fluidName + "'");
            }
            return new FluidStack(fluid, FluidHelpers.BUCKET_VOLUME);
        }
    }

}
