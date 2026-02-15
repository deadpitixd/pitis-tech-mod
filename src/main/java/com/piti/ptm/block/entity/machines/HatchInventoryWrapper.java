package com.piti.ptm.block.entity.machines;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class HatchInventoryWrapper implements IItemHandler {
    private final IItemHandler coreInventory;
    private final boolean isOutputMode;

    public HatchInventoryWrapper(IItemHandler coreInventory, boolean isOutputMode) {
        this.coreInventory = coreInventory;
        this.isOutputMode = isOutputMode;
    }

    @Override
    public int getSlots() { return coreInventory.getSlots(); }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) { return coreInventory.getStackInSlot(slot); }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!isOutputMode && slot < 9) {
            return coreInventory.insertItem(slot, stack, simulate);
        }
        return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (isOutputMode && slot >= 9 && slot <= 12) {
            return coreInventory.extractItem(slot, amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) { return coreInventory.getSlotLimit(slot); }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return !isOutputMode && slot < 9 && coreInventory.isItemValid(slot, stack);
    }
}