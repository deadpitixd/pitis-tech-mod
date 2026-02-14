package com.piti.ptm.block.entity;

import com.piti.ptm.fluid.IFluidHandlingBlockEntity;
import com.piti.ptm.fluid.ModFluids;
import com.piti.ptm.screen.LavaHeaterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SteamTurbineBlockEntity extends BlockEntity implements MenuProvider, IFluidHandlingBlockEntity {
    private final ItemStackHandler itemHandler = new ItemStackHandler(2);
    private static final int LPSTEAM_SLOT = 0;
    private static final int STEAM_SLOT = 1;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private final IFluidHandler inputHandler = new IFluidHandler() {
        @Override
        public int getTanks() { return 2; }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return tank == 0 ? lpsteamTank.getFluid() : lpsteamTank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? lpsteamTank.getCapacity() : lpsteamTank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tank == 0 ? lpsteamTank.isFluidValid(stack) : lpsteamTank.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.getFluid().isSame(ModFluids.STEAM_SOURCE.get())) {
                return steamTank.fill(resource, action);
            }
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    };

    private final LazyOptional<IFluidHandler> inputCapability = LazyOptional.of(() -> inputHandler);

    protected final ContainerData data;

    public final FluidTank lpsteamTank = createTank(4000, Fluids.WATER);
    public final FluidTank steamTank = new FluidTank(8000,
            stack -> stack.getFluid().isSame(ModFluids.STEAM_SOURCE.get())) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private FluidTank createTank(int capacity, Fluid fluidType) {
        return new FluidTank(capacity) {
            @Override
            protected void onContentsChanged() {
                setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }

            @Override
            public boolean isFluidValid(FluidStack stack) {
                return !stack.isEmpty() && stack.getFluid().isSame(fluidType);
            }
        };
    }

    public SteamTurbineBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.LAVA_HEATER_BE.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> lpsteamTank.getFluidAmount();
                    case 1 -> lpsteamTank.getCapacity();
                    case 2 -> steamTank.getFluidAmount();
                    case 3 -> steamTank.getCapacity();
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                }
            }

            @Override
            public int getCount() {
                return 8;
            }
        };
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            if (side == null) return inputCapability.cast();

            Direction localSide = rotateSide(facing, side);
            if (localSide == Direction.EAST || localSide == Direction.WEST) {
                return inputCapability.cast();
            }
        }

        return super.getCapability(cap, side);
    }

    private Direction rotateSide(Direction facing, Direction side) {
        if (side.getAxis().isVertical()) return side;

        return Direction.from2DDataValue((side.get2DDataValue() - facing.get2DDataValue() + 4) % 4);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        inputCapability.invalidate();
    }

    @Override
    public FluidTank getTank(int index) {
        return switch (index) {
            case 0 -> lpsteamTank;
            case 1 -> steamTank;
            default -> null;
        };
    }

    @Override
    public String getTankLabel(int index) {
        return switch (index) {
            case 0 -> "Water";
            case 1 -> "Steam";
            default -> "";
        };
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ptm.steam_turbine");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        saveAdditional(nbt);
        return nbt;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new LavaHeaterMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.put("WaterTank", lpsteamTank.writeToNBT(new CompoundTag()));
        tag.put("SteamTank", steamTank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        lpsteamTank.readFromNBT(tag.getCompound("WaterTank"));
        steamTank.readFromNBT(tag.getCompound("SteamTank"));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SteamTurbineBlockEntity be) {
        if (level.isClientSide) return;

        boolean changed = false;



        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }


    private boolean hasSteam() {
        return this.itemHandler.getStackInSlot(LPSTEAM_SLOT).getItem() == Items.LAVA_BUCKET &&
                canInsertIntoOutputSlot(1) && canInsertItemIntoOutputSlot(Items.MAGMA_BLOCK) &&
                steamTank.getFluidAmount() + 1000 <= steamTank.getCapacity();
    }
    private boolean hasWater() {
        return this.itemHandler.getStackInSlot(LPSTEAM_SLOT).getItem() == Items.WATER_BUCKET &&
                lpsteamTank.getFluidAmount() + 1000 <= lpsteamTank.getCapacity();
    }

    private boolean canInsertIntoOutputSlot(int count) {
        return this.itemHandler.getStackInSlot(STEAM_SLOT).getCount() + count <= this.itemHandler.getStackInSlot(STEAM_SLOT).getMaxStackSize();
    }

    private boolean canInsertItemIntoOutputSlot(Item item) {
        return this.itemHandler.getStackInSlot(STEAM_SLOT).isEmpty() || this.itemHandler.getStackInSlot(STEAM_SLOT).is(item);
    }
}