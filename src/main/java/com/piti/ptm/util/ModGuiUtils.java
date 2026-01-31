package com.piti.ptm.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.joml.Matrix4f;

import java.util.List;

public class ModGuiUtils {
    public static void renderFluidBox(GuiGraphics graphics, Font font, int mouseX, int mouseY,
                                      int guiX, int guiY, int x, int y,
                                      int width, int height,
                                      FluidTank tank, String emptyLabel) {

        if (!tank.isEmpty()) {
            FluidStack stack = tank.getFluid();
            IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(stack.getFluid());

            TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(props.getStillTexture(stack));

            int color = props.getTintColor(stack);
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            float a = ((color >> 24) & 0xFF) / 255f;

            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // We bake color into vertices below

            long stored = tank.getFluidAmount();
            long capacity = tank.getCapacity();
            int renderHeight = (int) Math.min(height, (stored * height) / capacity);
            int yOffset = height - renderHeight;

            Matrix4f matrix = graphics.pose().last().pose();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            for (int i = 0; i < width; i += 16) {
                for (int j = 0; j < renderHeight; j += 16) {

                    int drawWidth = Math.min(width - i, 16);
                    int drawHeight = Math.min(renderHeight - j, 16);

                    float u0 = sprite.getU0();
                    float u1 = sprite.getU(drawWidth);
                    float v0 = sprite.getV0();
                    float v1 = sprite.getV(drawHeight);

                    int screenX = guiX + x + i;
                    int screenY = guiY + y + yOffset + j;

                    buffer.vertex(matrix, screenX, screenY + drawHeight, 0).uv(u0, v1).color(r, g, b, a).endVertex();
                    buffer.vertex(matrix, screenX + drawWidth, screenY + drawHeight, 0).uv(u1, v1).color(r, g, b, a).endVertex();
                    buffer.vertex(matrix, screenX + drawWidth, screenY, 0).uv(u1, v0).color(r, g, b, a).endVertex();
                    buffer.vertex(matrix, screenX, screenY, 0).uv(u0, v0).color(r, g, b, a).endVertex();
                }
            }
            tesselator.end();
        }

        if (mouseX >= (guiX + x) && mouseX <= (guiX + x + width) &&
                mouseY >= (guiY + y) && mouseY <= (guiY + y + height)) {

            Component fluidName = tank.isEmpty() ? Component.literal(emptyLabel) : tank.getFluid().getDisplayName();
            graphics.renderComponentTooltip(font, List.of(
                    fluidName,
                    Component.literal(Component.literal(String.valueOf(tank.getFluidAmount())).withStyle(ChatFormatting.GOLD) + " / " + Component.literal(String.valueOf(tank.getCapacity())).withStyle(ChatFormatting.DARK_GRAY) + Component.literal(" mB").withStyle(ChatFormatting.GRAY))
            ), mouseX, mouseY);
        }
    }
}