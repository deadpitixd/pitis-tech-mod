package com.piti.ptm.screen;

import com.piti.ptm.util.ModGuiUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;

public class IndustrialFurnaceScreen extends AbstractContainerScreen<IndustrialFurnaceMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ptm", "textures/gui/industrial_furnace.png");

    public IndustrialFurnaceScreen(IndustrialFurnaceMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.imageWidth = 176;
        this.imageHeight = 164;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        ModGuiUtils.renderFluidBox(guiGraphics, this.font, mouseX, mouseY, x, y, 94, 55, 48, 14,
                menu.getInputFluidStack(), 16000, "Empty Input");

        ModGuiUtils.renderFluidBox(guiGraphics, this.font, mouseX, mouseY, x, y, 146, 17, 23, 52,
                menu.getOutputFluidStack(), 16000, "Empty Output");

        ModGuiUtils.renderEnergyBar(guiGraphics, this.font, mouseX, mouseY, x, y, 7, 17, 6, 52,
                menu.getEnergyStored(), menu.getMaxEnergyStored());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}