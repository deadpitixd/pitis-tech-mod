package com.piti.ptm.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class FluidRenderer {
    public static void renderFluid(GuiGraphics graphics, FluidStack fluidStack, int x, int y, int width, int height, int capacity) {
        if (fluidStack.isEmpty()) return;

        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation stillTexture = props.getStillTexture(fluidStack);
        if (stillTexture == null) return;

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);

        int color = props.getTintColor(fluidStack);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        RenderSystem.setShaderColor(r, g, b, a);

        int stored = fluidStack.getAmount();
        int renderHeight = (int) Math.min(height, ((long) stored * height) / capacity);
        int yOffset = height - renderHeight;

        graphics.blit(x, y + yOffset, 0, width, renderHeight, sprite);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
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