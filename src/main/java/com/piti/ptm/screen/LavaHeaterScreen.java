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

public class LavaHeaterScreen extends AbstractContainerScreen<LavaHeaterMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(PitisTech.MOD_ID, "textures/gui/lava_heater.png");

    public LavaHeaterScreen(LavaHeaterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 1000;
        this.titleLabelY = 1000;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        ModGuiUtils.renderFluidBox(pGuiGraphics, font, pMouseX, pMouseY, x, y, 56, 17, 55, 16, menu.blockEntity.waterTank, "Water");
        ModGuiUtils.renderFluidBox(pGuiGraphics, font, pMouseX, pMouseY, x, y, 56, 41, 38, 16, menu.blockEntity.lavaTank, "Lava");
        ModGuiUtils.renderFluidBox(pGuiGraphics, font, pMouseX, pMouseY, x, y, 127, 17, 36, 17, menu.blockEntity.steamTank, "Steam");

        renderTemperatureBar(pGuiGraphics, x + 22, y + 17);
    }

    private void renderTemperatureBar(GuiGraphics guiGraphics, int x, int y) {
        int scaledTemp = menu.getScaledProgress();
        guiGraphics.blit(TEXTURE, x, y + (52 - scaledTemp), 176, 31 + (52 - scaledTemp), 5, scaledTemp);
    }
}