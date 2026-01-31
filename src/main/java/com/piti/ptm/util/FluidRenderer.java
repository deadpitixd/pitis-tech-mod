package com.piti.ptm.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class FluidRenderer {
    public static void renderFluid(GuiGraphics graphics, FluidStack fluidStack, int x, int y, int width, int height, int capacity) {
        if (fluidStack.isEmpty() || capacity <= 0) return; // Safety check for capacity

        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation stillTexture = props.getStillTexture(fluidStack);
        if (stillTexture == null) return;

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);

        int color = props.getTintColor(fluidStack);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // Water often has 0 in the alpha bits; force to 1.0f if it's 0
        float a = ((color >> 24) & 0xFF) / 255f;
        if (a <= 0f) a = 1.0f;

        // Use graphics.setColor for 1.20.1 standards
        graphics.setColor(r, g, b, a);

        int stored = fluidStack.getAmount();
        int renderHeight = (int) Math.min(height, ((long) stored * height) / capacity);
        int yOffset = height - renderHeight;

        // Enable blending for transparent fluids (like water)
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (int i = 0; i < width; i += 16) {
            for (int j = 0; j < renderHeight; j += 16) {
                int drawWidth = Math.min(width - i, 16);
                int drawHeight = Math.min(renderHeight - j, 16);

                // Note: Use the blit overload that accepts a sprite
                graphics.blit(x + i, y + yOffset + j, 0, drawWidth, drawHeight, sprite);
            }
        }

        RenderSystem.disableBlend();
        graphics.setColor(1f, 1f, 1f, 1f);
    }

    public static void renderFluidStack(GuiGraphics guiGraphics, FluidStack stack, int x, int y, int width, int height) {
        if (stack.isEmpty()) return;

        Fluid fluid = stack.getFluid();
        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation texture = props.getStillTexture(stack);

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(texture);

        int color = props.getTintColor(stack);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        guiGraphics.setColor(r, g, b, 1.0f);

        guiGraphics.blit(x, y, 0, width, height, sprite);

        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static List<Component> getFluidTooltip(FluidStack fluidStack, int capacity) {
        List<Component> tooltip = new ArrayList<>();
        if (fluidStack.isEmpty()) {
            tooltip.add(Component.translatable("gui.ptm.empty").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(fluidStack.getDisplayName());
            tooltip.add(Component.literal(fluidStack.getAmount() + " / " + capacity + " mB").withStyle(ChatFormatting.GOLD));
        }
        return tooltip;
    }
}