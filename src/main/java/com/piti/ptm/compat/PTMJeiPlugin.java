package com.piti.ptm.compat;

import com.piti.ptm.PitisTech;
import com.piti.ptm.recipe.IndustrialFurnaceRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import com.piti.ptm.block.ModBlocks;

import java.util.List;

@JeiPlugin
public class PTMJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(PitisTech.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new IndustrialFurnaceRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Minecraft world = Minecraft.getInstance();
        List<IndustrialFurnaceRecipe> recipes = world.level.getRecipeManager()
                .getAllRecipesFor(IndustrialFurnaceRecipe.Type.INSTANCE);
        registration.addRecipes(IndustrialFurnaceRecipeCategory.TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.INDUSTRIAL_FURNACE_CORE.get()), IndustrialFurnaceRecipeCategory.TYPE);
    }
}