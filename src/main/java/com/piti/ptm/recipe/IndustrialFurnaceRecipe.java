package com.piti.ptm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class IndustrialFurnaceRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputItems;
    private final FluidStack inputFluid;
    private final NonNullList<ItemStack> outputItems;
    private final FluidStack outputFluid;
    private final int time;
    private final int energy = 10000;

    public IndustrialFurnaceRecipe(ResourceLocation id, NonNullList<Ingredient> inputItems, FluidStack inputFluid,
                                   NonNullList<ItemStack> outputItems, FluidStack outputFluid, int time) {
        this.id = id;
        this.inputItems = inputItems;
        this.inputFluid = inputFluid;
        this.outputItems = outputItems;
        this.outputFluid = outputFluid;
        this.time = time;
    }

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        if (level.isClientSide()) return false;
        for (Ingredient ingredient : inputItems) {
            boolean found = false;
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (ingredient.test(container.getItem(i))) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(SimpleContainer container, RegistryAccess registryAccess) {
        return outputItems.isEmpty() ? ItemStack.EMPTY : outputItems.get(0).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return outputItems.isEmpty() ? ItemStack.EMPTY : outputItems.get(0).copy();
    }

    @Override
    public ResourceLocation getId() { return id; }

    @Override
    public RecipeSerializer<?> getSerializer() { return Serializer.INSTANCE; }

    @Override
    public RecipeType<?> getType() { return Type.INSTANCE; }

    public int getTime() { return time; }
    public int getEnergy() { return energy; }
    public NonNullList<ItemStack> getOutputItems() { return outputItems; }
    public FluidStack getInputFluid() { return inputFluid; }
    public FluidStack getOutputFluid() { return outputFluid; }

    public static class Type implements RecipeType<IndustrialFurnaceRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
    }

    public static class Serializer implements RecipeSerializer<IndustrialFurnaceRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public IndustrialFurnaceRecipe fromJson(ResourceLocation id, JsonObject json) {
            NonNullList<Ingredient> inputs = NonNullList.create();
            if (json.has("ingredients")) {
                JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");
                for (JsonElement element : ingredients) {
                    inputs.add(Ingredient.fromJson(element));
                }
            }

            FluidStack inputFluid = FluidStack.EMPTY;
            if (json.has("fluid_input") && json.get("fluid_input").isJsonObject()) {
                JsonObject fluidInputJson = json.getAsJsonObject("fluid_input");
                ResourceLocation fluidId = parseLocation(GsonHelper.getAsString(fluidInputJson, "fluid"));
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);

                if (fluid != null) {
                    int fluidAmount = GsonHelper.getAsInt(fluidInputJson, "amount", 1000);
                    inputFluid = new FluidStack(fluid, fluidAmount);
                }
            }

            NonNullList<ItemStack> outputItems = NonNullList.create();
            if (json.has("result")) {
                JsonArray results = GsonHelper.getAsJsonArray(json, "result");
                for (JsonElement element : results) {
                    JsonObject entry = element.getAsJsonObject();
                    ResourceLocation itemId = parseLocation(GsonHelper.getAsString(entry, "item"));
                    int count = GsonHelper.getAsInt(entry, "count", 1);
                    outputItems.add(new ItemStack(ForgeRegistries.ITEMS.getValue(itemId), count));
                }
            }

            FluidStack outputFluid = FluidStack.EMPTY;
            if (json.has("fluid_output") && json.get("fluid_output").isJsonObject()) {
                JsonObject fluidOutputJson = json.getAsJsonObject("fluid_output");
                ResourceLocation outFluidId = parseLocation(GsonHelper.getAsString(fluidOutputJson, "fluid"));
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(outFluidId);

                if (fluid != null) {
                    int amount = GsonHelper.getAsInt(fluidOutputJson, "amount", 1000);
                    outputFluid = new FluidStack(fluid, amount);
                }
            }

            int time = GsonHelper.getAsInt(json, "time", 100);
            return new IndustrialFurnaceRecipe(id, inputs, inputFluid, outputItems, outputFluid, time);
        }

        private ResourceLocation parseLocation(String s) {
            if (s.contains(":")) {
                String[] parts = s.split(":", 2);
                return ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
            }
            return ResourceLocation.fromNamespaceAndPath("minecraft", s);
        }

        @Override
        public @Nullable IndustrialFurnaceRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int iSize = buffer.readInt();
            NonNullList<Ingredient> inputs = NonNullList.withSize(iSize, Ingredient.EMPTY);
            for (int i = 0; i < iSize; i++) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
            }
            FluidStack inputFluid = buffer.readFluidStack();
            int oSize = buffer.readInt();
            NonNullList<ItemStack> outputItems = NonNullList.withSize(oSize, ItemStack.EMPTY);
            for (int i = 0; i < oSize; i++) {
                outputItems.set(i, buffer.readItem());
            }
            FluidStack outputFluid = buffer.readFluidStack();
            int time = buffer.readInt();
            return new IndustrialFurnaceRecipe(id, inputs, inputFluid, outputItems, outputFluid, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, IndustrialFurnaceRecipe recipe) {
            buffer.writeInt(recipe.inputItems.size());
            for (Ingredient ing : recipe.inputItems) {
                ing.toNetwork(buffer);
            }
            buffer.writeFluidStack(recipe.inputFluid);
            buffer.writeInt(recipe.outputItems.size());
            for (ItemStack stack : recipe.outputItems) {
                buffer.writeItem(stack);
            }
            buffer.writeFluidStack(recipe.outputFluid);
            buffer.writeInt(recipe.time);
        }
    }
    public NonNullList<Ingredient> getInputItems() {
        return this.inputItems;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.inputItems;
    }
}