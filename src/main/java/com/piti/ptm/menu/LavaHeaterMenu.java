package com.piti.ptm.menu;

import com.piti.ptm.block.entity.lavaHeaterBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class LavaHeaterMenu extends AbstractContainerMenu {

    private final lavaHeaterBlockEntity blockEntity;
    private final ContainerData data;

    public LavaHeaterMenu(int id, Inventory playerInventory, lavaHeaterBlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.LAVA_HEATER_MENU.get(), id);
        this.blockEntity = blockEntity;
        this.data = data;

        IItemHandler handler = blockEntity.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).orElseThrow(null);

        // Bottom input: lava bucket (slot 0)
        this.addSlot(new SlotItemHandler(handler, 0, 56, 53));
        // Bottom container: lava (slot 1)
        this.addSlot(new SlotItemHandler(handler, 1, 56, 17));
        // Upper box: water (slot 2)
        this.addSlot(new SlotItemHandler(handler, 2, 80, 17));
        // Right box: steam (slot 3)
        this.addSlot(new SlotItemHandler(handler, 3, 104, 17));
        // Right output: magma (slot 4)
        this.addSlot(new SlotItemHandler(handler, 4, 104, 53));

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }
    public LavaHeaterMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(
                id,
                playerInventory,
                (lavaHeaterBlockEntity) playerInventory.player.level().getBlockEntity(buffer.readBlockPos()),
                new SimpleContainerData(8) // or sync real data
        );
    }

    @Override
    public boolean stillValid(@NotNull net.minecraft.world.entity.player.Player player) {
        return net.minecraft.world.inventory.ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos())
                .evaluate((level, pos) ->
                        level.getBlockState(pos).getBlock() == blockEntity.getBlockState().getBlock(), true);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull net.minecraft.world.entity.player.Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemStack = slot.getItem();
            stack = itemStack.copy();
            if (index < 5) { // block slots
                if (!this.moveItemStackTo(itemStack, 5, this.slots.size(), true)) return ItemStack.EMPTY;
            } else { // player inventory
                if (!this.moveItemStackTo(itemStack, 0, 5, false)) return ItemStack.EMPTY;
            }
            if (itemStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return stack;
    }

    public int getWater() { return data.get(4); }
    public int getSteam() { return data.get(5); }
    public int getTU() { return data.get(6); }
    public int getLava() { return data.get(7); }
}
