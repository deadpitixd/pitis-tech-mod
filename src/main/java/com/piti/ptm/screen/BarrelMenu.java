package com.piti.ptm.screen;

import com.piti.ptm.block.entity.BarrelBlockEntity;
import com.piti.ptm.block.modBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class BarrelMenu extends AbstractContainerMenu {

    public final BarrelBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public BarrelMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(2));
    }

    public BarrelMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.BARREL_MENU.get(), containerId);

        this.blockEntity = (BarrelBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, 0, 8, 10));
            this.addSlot(new SlotItemHandler(handler, 1, 8, 53));
            this.addSlot(new SlotItemHandler(handler, 2, 38, 10));
            this.addSlot(new SlotItemHandler(handler, 3, 38, 53));
            this.addSlot(new SlotItemHandler(handler, 4, 137, 10));
            this.addSlot(new SlotItemHandler(handler, 5, 137, 53));
        });

        addDataSlots(data);
    }

    public int getFluidAmount() {
        return data.get(0);
    }

    public int getMaxFluid() {
        return data.get(1);
    }

    public int getScaledFluid(int height) {
        return getMaxFluid() != 0 ? getFluidAmount() * height / getMaxFluid() : 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, modBlocks.BARREL_STEEL.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copy = sourceStack.copy();

        int VANILLA_FIRST_SLOT_INDEX = 0;
        int VANILLA_SLOT_COUNT = 36;
        int THIS_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
        int THIS_INVENTORY_SLOT_COUNT = 6;

        if (index < THIS_INVENTORY_FIRST_SLOT_INDEX) {
            if (!moveItemStackTo(sourceStack, THIS_INVENTORY_FIRST_SLOT_INDEX, THIS_INVENTORY_FIRST_SLOT_INDEX + THIS_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < THIS_INVENTORY_FIRST_SLOT_INDEX + THIS_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(playerIn, sourceStack);
        return copy;
    }
}