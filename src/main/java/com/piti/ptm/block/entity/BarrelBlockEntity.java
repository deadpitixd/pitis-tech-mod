package com.piti.ptm.block.entity;

import com.piti.ptm.network.IFluidReceiver;
import com.piti.ptm.screen.BarrelMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BarrelBlockEntity extends BlockEntity implements MenuProvider, IFluidReceiver {

    private final ItemStackHandler itemHandler = new ItemStackHandler(6){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public final FluidTank tank = new FluidTank(16000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    private int fluidID = 0; // ID of the fluid in tank
    private int maxFluid = 16000;

    public final ContainerData data;

    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();



    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BARREL_BE.get(), pos, state);

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> tank.getFluidAmount();
                    case 1 -> tank.getCapacity();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> {
                        if (!tank.getFluid().isEmpty()) {
                            tank.getFluid().setAmount(value);
                        }
                    }
                    case 1 -> tank.setCapacity(value);
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ptm.barrel");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BarrelMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(() -> tank);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put("inventory", itemHandler.serializeNBT());

        tag.put("fluid", tank.writeToNBT(new CompoundTag()));
    }
    public void drops() {
        net.minecraft.world.SimpleContainer inventory = new net.minecraft.world.SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        net.minecraft.world.Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        tank.readFromNBT(tag.getCompound("fluid"));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BarrelBlockEntity pEntity) {
        if (level.isClientSide) return;

        if (hasFluidItemInInputSlot(pEntity)) {
            if (fillTankFromItem(pEntity, pEntity.itemHandler.getStackInSlot(2))) {
                pEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        if (hasOutputItemInInputSlot(pEntity)) {
            if (fillItemFromTank(pEntity, pEntity.itemHandler.getStackInSlot(4))) {
                pEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }

    public FluidStack getFluidStack() {
        return tank.getFluid();
    }


    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        saveAdditional(nbt);
        return nbt;
    }
    private static boolean hasFluidItemInInputSlot(BarrelBlockEntity pEntity) {
        return pEntity.itemHandler.getStackInSlot(2).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
    }

    private static boolean hasOutputItemInInputSlot(BarrelBlockEntity pEntity) {
        return pEntity.itemHandler.getStackInSlot(4).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
    }

    private static boolean fillTankFromItem(BarrelBlockEntity be, ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(handler -> {
            FluidStack drainable = handler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
            if (!drainable.isEmpty() && be.tank.isFluidValid(drainable)) {
                int filled = be.tank.fill(drainable, IFluidHandler.FluidAction.EXECUTE);
                if (filled > 0) {
                    handler.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                    be.itemHandler.setStackInSlot(2, handler.getContainer());
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    private static boolean fillItemFromTank(BarrelBlockEntity be, ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(handler -> {
            if (!be.tank.getFluid().isEmpty()) {
                int filled = handler.fill(be.tank.getFluid(), IFluidHandler.FluidAction.EXECUTE);
                if (filled > 0) {
                    be.tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                    be.itemHandler.setStackInSlot(4, handler.getContainer());
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    @Override
    public boolean canAcceptFluid(String fluidId) {
        try {
            int id = Integer.parseInt(fluidId);
            return tank.getFluid().isEmpty() || fluidID == 0 || fluidID == id;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
