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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

enum mode {
    IMPORT, EXPORT, BOTH;

    public mode next() {
        return switch (this) {
            case IMPORT -> EXPORT;
            case EXPORT -> BOTH;
            case BOTH   -> IMPORT;
        };
    }
}

public class BatteryStorageBlockEntity extends BlockEntity implements IEnergyStorage {

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

    mode Mode=mode.BOTH;

    public void toggleMode(){
        this.Mode = this.Mode.next();
    }

    public final EnergyStorage tank = new EnergyStorage(320000, 32000);

    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();



    public BatteryStorageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BARREL_BE.get(), pos, state);

    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyEnergyHandler = LazyOptional.of(() -> tank);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyEnergyHandler.invalidate();
    }

    public void drops() {
        net.minecraft.world.SimpleContainer inventory = new net.minecraft.world.SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        net.minecraft.world.Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BatteryStorageBlockEntity pEntity){
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

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int energyReceived = tank.receiveEnergy(maxReceive, simulate);
        if (energyReceived > 0 && !simulate){
            setChanged();
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = tank.extractEnergy(maxExtract, simulate);
        if (energyExtracted > 0 && !simulate){
            setChanged();
        }
        return energyExtracted;
    }

    @Override
    public boolean canReceive() {
        return tank.getEnergyStored() < getMaxEnergyStored() && ( Mode == mode.IMPORT || Mode == mode.BOTH );
    }

    @Override
    public boolean canExtract() {
        return tank.getEnergyStored() > 0 && ( Mode == mode.EXPORT || Mode == mode.BOTH );
    }

    @Override
    public int getEnergyStored() {
        return tank.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return tank.getMaxEnergyStored();
    }
}
