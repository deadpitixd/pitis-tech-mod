package com.piti.ptm.item.custom;

import com.piti.ptm.item.ModItems;
import com.piti.ptm.recipe.IndustrialFurnaceRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PunchedCardItem extends Item {
    public PunchedCardItem(Properties properties) {
        super(properties);
    }

    public static ItemStack of(ResourceLocation recipeId) {
        ItemStack stack = new ItemStack(ModItems.PUNCHED_CARD.get());
        stack.getOrCreateTag().putString("RecipeId", recipeId.toString());
        return stack;
    }

    public static ResourceLocation getRecipeId(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("RecipeId")) {
            return new ResourceLocation(stack.getTag().getString("RecipeId"));
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ResourceLocation id = getRecipeId(stack);
        if (id != null && level != null) {
            // Hides and shows the id based on, if the player has the advanced mode activated
            // (F3 + H)
            if (flag.isAdvanced()) {
                tooltip.add(Component.literal("ID: ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_AQUA)));
            }

            level.getRecipeManager().byKey(id).ifPresent(recipe -> {
                if (recipe instanceof IndustrialFurnaceRecipe furnaceRecipe) {
                    if (!furnaceRecipe.getOutputItems().isEmpty()) {
                        ItemStack result = furnaceRecipe.getOutputItems().get(0);
                        tooltip.add(Component.literal("Produces: ").withStyle(ChatFormatting.GRAY)
                                .append(result.getHoverName().copy().withStyle(ChatFormatting.GOLD)));
                        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()){
                            if (!furnaceRecipe.getIngredients().isEmpty()) {
                                tooltip.add(Component.translatable("tooltip.ptm.punchcardin").withStyle(ChatFormatting.AQUA));
                                for (Ingredient i : furnaceRecipe.getInputItems()) {
                                    tooltip.add(Component.literal(String.valueOf((i.getItems()[0].getCount()))).append(" * ")
                                            .append(i.getItems()[0].getHoverName()));
                                }
                                if (!furnaceRecipe.getInputFluid().isEmpty()){
                                    FluidStack fluid = furnaceRecipe.getInputFluid();
                                    tooltip.add(Component.literal(fluid.getAmount() + "mB of ")
                                            .append(fluid.getDisplayName()));
                                }
                                tooltip.add(Component.translatable("tooltip.ptm.punchcardout").withStyle(ChatFormatting.AQUA));
                                for (ItemStack i : furnaceRecipe.getOutputItems()) {
                                    tooltip.add(Component.literal(String.valueOf((i.getCount()))).append(" * ")
                                            .append(i.getHoverName()));
                                }
                                if (!furnaceRecipe.getOutputFluid().isEmpty()){
                                    FluidStack fluid = furnaceRecipe.getOutputFluid();
                                    tooltip.add(Component.literal(fluid.getAmount() + "mB of ")
                                            .append(fluid.getDisplayName()));
                                }
                                if (flag.isAdvanced())  tooltip.add(Component.translatable("tooltip.ptm.advancedmodereq").withStyle(ChatFormatting.DARK_GRAY));
                            }
                        }
                        else{
                            tooltip.add(Component.translatable("tooltip.ptm.shift").withStyle(ChatFormatting.DARK_AQUA));
                        }
                    }
                }
            });
        } else {
            tooltip.add(Component.literal("Blank Card").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC));
        }
    }
}