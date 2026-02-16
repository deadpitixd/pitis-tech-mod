package com.piti.ptm.compat;

import com.piti.ptm.PitisTech;
import com.piti.ptm.block.ModBlocks;
import com.piti.ptm.recipe.IndustrialFurnaceRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class IndustrialFurnaceRecipeCategory implements IRecipeCategory<IndustrialFurnaceRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(PitisTech.MOD_ID, "industrial_furnace");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(PitisTech.MOD_ID, "textures/gui/industrial_furnace_jei.png");
    public static final RecipeType<IndustrialFurnaceRecipe> TYPE = new RecipeType<>(UID, IndustrialFurnaceRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public IndustrialFurnaceRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 85);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.INDUSTRIAL_FURNACE_CORE.get()));
    }

    @Override
    public RecipeType<IndustrialFurnaceRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Industrial Furnace");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IndustrialFurnaceRecipe recipe, IFocusGroup focuses) {
        var inputs = recipe.getInputItems();
        for (int i = 0; i < inputs.size(); i++) {
            int x = 21 + (i % 3) * 18;
            int y = 17 + (i / 3) * 18;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addIngredients(inputs.get(i));
        }

        if (!recipe.getInputFluid().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 94, 55)
                    .setFluidRenderer(16000, false, 48, 14)
                    .addIngredient(ForgeTypes.FLUID_STACK, recipe.getInputFluid());
        }

        var outputs = recipe.getOutputItems();
        for (int i = 0; i < outputs.size(); i++) {
            int x = 98 + (i % 2) * 18;
            int y = 17 + (i / 2) * 18;
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y).addItemStack(outputs.get(i));
        }

        if (!recipe.getOutputFluid().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 146, 17)
                    .setFluidRenderer(16000, false, 23, 52)
                    .addIngredient(ForgeTypes.FLUID_STACK, recipe.getOutputFluid());
        }
    }
}