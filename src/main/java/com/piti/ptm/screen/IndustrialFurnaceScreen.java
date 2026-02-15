package com.piti.ptm.screen;

import com.piti.ptm.util.FluidRenderer;
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

        FluidStack inputStack = menu.getInputFluidStack();
        if (!inputStack.isEmpty()) {
            FluidRenderer.renderFluid(guiGraphics, inputStack, x + 94, y + 55, 48, 14, 4000);
        }

        FluidStack outputStack = menu.getOutputFluidStack();
        if (!outputStack.isEmpty()) {
            FluidRenderer.renderFluid(guiGraphics, outputStack, x + 146, y + 17, 23, 52, 4000);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        if (isHovering(94, 55, 48, 14, mouseX, mouseY)) {
            guiGraphics.renderComponentTooltip(this.font, FluidRenderer.getFluidTooltip(menu.getInputFluidStack(), 16000), mouseX, mouseY);
        }

        if (isHovering(146, 17, 23, 52, mouseX, mouseY)) {
            guiGraphics.renderComponentTooltip(this.font, FluidRenderer.getFluidTooltip(menu.getOutputFluidStack(), 16000), mouseX, mouseY);
        }
    }
}