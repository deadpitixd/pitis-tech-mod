package com.piti.ptm.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.piti.ptm.PitisTech;
import com.piti.ptm.util.ModGuiUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BarrelScreen extends AbstractContainerScreen<BarrelMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(PitisTech.MOD_ID, "textures/gui/barrel.png");

    public BarrelScreen(BarrelMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 1000;
        this.titleLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);

        int x = leftPos;
        int y = topPos;

        String fluidName = menu.blockEntity.tank.getFluid().isEmpty()
                ? "Empty"
                : menu.blockEntity.tank.getFluid().getDisplayName().getString();

        ModGuiUtils.renderFluidBox(guiGraphics, this.font, mouseX, mouseY, x, y, 70, 10, 45, 59, menu.blockEntity.tank, fluidName);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = leftPos;
        int y = topPos;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }
}