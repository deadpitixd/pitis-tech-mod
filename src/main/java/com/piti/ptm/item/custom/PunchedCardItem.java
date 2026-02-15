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
import net.minecraft.world.level.Level;
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
            tooltip.add(Component.literal("ID: ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_AQUA)));

            level.getRecipeManager().byKey(id).ifPresent(recipe -> {
                if (recipe instanceof IndustrialFurnaceRecipe furnaceRecipe) {
                    if (!furnaceRecipe.getOutputItems().isEmpty()) {
                        ItemStack result = furnaceRecipe.getOutputItems().get(0);
                        tooltip.add(Component.literal("Produces: ").withStyle(ChatFormatting.GRAY)
                                .append(result.getHoverName().copy().withStyle(ChatFormatting.GOLD)));
                    }
                }
            });
        } else {
            tooltip.add(Component.literal("Blank Card").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC));
        }
    }
}