package com.piti.ptm.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.piti.ptm.PitisTech;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BarrelScreen extends AbstractContainerScreen<LavaHeaterMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(PitisTech.MOD_ID, "textures/gui/lava_heater.png");

    public BarrelScreen(LavaHeaterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 1000;
        this.titleLabelY = 1000;
    }


    private static final int ARROW_HEIGHT = 52;
    private static final int ARROW_WIDTH = 5;

    private void renderProgressSquare(GuiGraphics guiGraphics, int x, int y) {
        if (!menu.isCrafting()) return;

        int progress = menu.getScaledProgress();

        guiGraphics.blit(
                TEXTURE,
                x,
                y + (ARROW_HEIGHT - progress),
                176,
                31 + (ARROW_HEIGHT - progress),
                ARROW_WIDTH,
                progress
        );
    }



    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        renderProgressSquare(pGuiGraphics, leftPos + 22, topPos + 17);
    }
}
