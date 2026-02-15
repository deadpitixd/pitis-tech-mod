package com.piti.ptm.recipe;

import com.piti.ptm.PitisTech;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, PitisTech.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, PitisTech.MOD_ID);

    public static final RegistryObject<RecipeSerializer<IndustrialFurnaceRecipe>> INDUSTRIAL_FURNACE_SERIALIZER =
            SERIALIZERS.register("industrial_furnace", () -> IndustrialFurnaceRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeType<IndustrialFurnaceRecipe>> INDUSTRIAL_FURNACE_TYPE =
            TYPES.register("industrial_furnace", () -> IndustrialFurnaceRecipe.Type.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}