package com.piti.ptm.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.piti.ptm.PitisTech;
import com.piti.ptm.menu.LavaHeaterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LavaHeaterScreen extends AbstractContainerScreen<LavaHeaterMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(PitisTech.MOD_ID, "textures/gui/lava_heater_gui.png");

    public LavaHeaterScreen(LavaHeaterMenu menu, net.minecraft.world.entity.player.Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);

        int tuHeight = (int) (menu.getTU() * 50f / 250f); // scale TU to 50px
        int waterHeight = (int) (menu.getWater() * 50f / 10000f);
        int steamHeight = (int) (menu.getSteam() * 50f / 10000f);
        int lavaHeight = (int) (menu.getLava() * 50f / 5000f);

        // Example: draw TU bar
        graphics.fill(getGuiLeft() + 10, getGuiTop() + 60 - tuHeight, getGuiLeft() + 20, getGuiTop() + 60, 0xFF0000FF);

        // Add similar bars for water, steam, lava
        graphics.fill(getGuiLeft() + 30, getGuiTop() + 60 - waterHeight, getGuiLeft() + 40, getGuiTop() + 60, 0xFF00FFFF);
        graphics.fill(getGuiLeft() + 50, getGuiTop() + 60 - steamHeight, getGuiLeft() + 60, getGuiTop() + 60, 0xFFFF0000);
        graphics.fill(getGuiLeft() + 70, getGuiTop() + 60 - lavaHeight, getGuiLeft() + 80, getGuiTop() + 60, 0xFFFFFF00);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
