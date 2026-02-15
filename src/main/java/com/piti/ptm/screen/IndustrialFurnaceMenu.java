package com.piti.ptm.screen;

import com.piti.ptm.block.ModBlocks;
import com.piti.ptm.block.entity.machines.IndustrialFurnaceCoreBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class IndustrialFurnaceMenu extends AbstractContainerMenu {
    private final IndustrialFurnaceCoreBlockEntity blockEntity;
    private final ContainerData data;
    private final Level level;

    public IndustrialFurnaceMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(8));
    }

    public IndustrialFurnaceMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.INDUSTRIAL_FURNACE_MENU.get(), id);
        checkContainerDataCount(data, 8);
        this.blockEntity = (IndustrialFurnaceCoreBlockEntity) entity;
        this.data = data;
        this.level = inv.player.level();

        IItemHandler handler = blockEntity.getItemHandler();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addSlot(new SlotItemHandler(handler, j + i * 3, 21 + j * 18, 17 + i * 18));
            }
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                addSlot(new SlotItemHandler(handler, 9 + (j + i * 2), 98 + j * 18, 17 + i * 18));
            }
        }

        addSlot(new SlotItemHandler(handler, 13, 79, 17));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }

        addDataSlots(data);
    }

    public int getEnergyStored() {
        return this.data.get(4);
    }

    public int getMaxEnergyStored() {
        return this.data.get(5);
    }

    public FluidStack getInputFluidStack() {
        Fluid fluid = BuiltInRegistries.FLUID.byId(this.data.get(6));
        return new FluidStack(fluid, this.data.get(2));
    }

    public FluidStack getOutputFluidStack() {
        Fluid fluid = BuiltInRegistries.FLUID.byId(this.data.get(7));
        return new FluidStack(fluid, this.data.get(3));
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            ItemStack copy = stack.copy();
            if (index < 14) {
                if (!this.moveItemStackTo(stack, 14, 50, true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(stack, 0, 14, false)) return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
            return copy;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.INDUSTRIAL_FURNACE_CORE.get());
    }
}