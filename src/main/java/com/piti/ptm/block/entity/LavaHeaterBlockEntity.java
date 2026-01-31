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

public class LavaHeaterBlockEntity extends BlockEntity implements MenuProvider, IFluidHandlingBlockEntity {
    private final ItemStackHandler itemHandler = new ItemStackHandler(2);
    private static final int LAVA_SLOT = 0;
    private static final int MAGMA_SLOT = 1;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private final IFluidHandler inputHandler = new IFluidHandler() {
        @Override
        public int getTanks() { return 2; }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return tank == 0 ? waterTank.getFluid() : lavaTank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? waterTank.getCapacity() : lavaTank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tank == 0 ? waterTank.isFluidValid(stack) : lavaTank.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.getFluid().isSame(Fluids.WATER)) {
                return waterTank.fill(resource, action);
            }
            if (resource.getFluid().isSame(Fluids.LAVA)) {
                return lavaTank.fill(resource, action);
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

    public final FluidTank waterTank = createTank(4000, Fluids.WATER);
    public final FluidTank lavaTank = createTank(4000, Fluids.LAVA);
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

    private int temp = 0;
    private int maxTemp = 2500;

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

    public LavaHeaterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.LAVA_HEATER_BE.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> lavaTank.getFluidAmount();
                    case 1 -> lavaTank.getCapacity();
                    case 2 -> waterTank.getFluidAmount();
                    case 3 -> waterTank.getCapacity();
                    case 4 -> temp;
                    case 5 -> maxTemp;
                    case 6 -> steamTank.getFluidAmount();
                    case 7 -> steamTank.getCapacity();
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 4 -> temp = pValue;
                    case 5 -> maxTemp = pValue;
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
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (side == Direction.WEST || side == Direction.EAST) {
                return inputCapability.cast();
            }
            if (side == Direction.DOWN) {
                return LazyOptional.of(() -> steamTank).cast();
            }
        }

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
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
            case 0 -> waterTank;
            case 1 -> lavaTank;
            case 2 -> steamTank;
            default -> null;
        };
    }

    @Override
    public String getTankLabel(int index) {
        return switch (index) {
            case 0 -> "Water";
            case 1 -> "Lava";
            case 2 -> "Steam";
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
        return Component.translatable("block.ptm.lava_heater");
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
        tag.put("WaterTank", waterTank.writeToNBT(new CompoundTag()));
        tag.put("LavaTank", lavaTank.writeToNBT(new CompoundTag()));
        tag.put("SteamTank", steamTank.writeToNBT(new CompoundTag()));
        tag.putInt("temp", temp);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        waterTank.readFromNBT(tag.getCompound("WaterTank"));
        lavaTank.readFromNBT(tag.getCompound("LavaTank"));
        steamTank.readFromNBT(tag.getCompound("SteamTank"));
        temp = tag.getInt("temp");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LavaHeaterBlockEntity be) {
        if (level.isClientSide) return;

        boolean changed = false;

        if (be.hasRecipe()) {
            be.exchangeLava();
            changed = true;
        }

        if (be.lavaTank.getFluidAmount() >= 5) {
            be.lavaTank.drain(5, IFluidHandler.FluidAction.EXECUTE);
            be.temp = Math.min(be.temp + 5, be.maxTemp);
            changed = true;
        }

        if (be.temp > 150 && be.waterTank.getFluidAmount() >= 10) {
            FluidStack steamStack = new FluidStack(ModFluids.STEAM_SOURCE.get(), 20);
            int filled = be.steamTank.fill(steamStack, IFluidHandler.FluidAction.SIMULATE);

            if (filled == 20) {
                be.waterTank.drain(10, IFluidHandler.FluidAction.EXECUTE);
                be.steamTank.fill(steamStack, IFluidHandler.FluidAction.EXECUTE);
                be.temp -= 1;
                changed = true;
            }
        }

        if (be.temp > 0 && be.lavaTank.isEmpty()) {
            be.temp = Math.max(be.temp - 1, 0);
            changed = true;
        }

        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private void exchangeLava() {
        this.itemHandler.extractItem(LAVA_SLOT, 1, false);
        this.lavaTank.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE);
        this.itemHandler.setStackInSlot(LAVA_SLOT, new ItemStack(Items.BUCKET, 1));
        ItemStack result = new ItemStack(Items.MAGMA_BLOCK, 1);
        this.itemHandler.setStackInSlot(MAGMA_SLOT, new ItemStack(result.getItem(),
                this.itemHandler.getStackInSlot(MAGMA_SLOT).getCount() + result.getCount()));
    }

    private boolean hasRecipe() {
        return this.itemHandler.getStackInSlot(LAVA_SLOT).getItem() == Items.LAVA_BUCKET &&
                canInsertIntoOutputSlot(1) && canInsertItemIntoOutputSlot(Items.MAGMA_BLOCK) &&
                lavaTank.getFluidAmount() + 1000 <= lavaTank.getCapacity();
    }

    private boolean canInsertIntoOutputSlot(int count) {
        return this.itemHandler.getStackInSlot(MAGMA_SLOT).getCount() + count <= this.itemHandler.getStackInSlot(MAGMA_SLOT).getMaxStackSize();
    }

    private boolean canInsertItemIntoOutputSlot(Item item) {
        return this.itemHandler.getStackInSlot(MAGMA_SLOT).isEmpty() || this.itemHandler.getStackInSlot(MAGMA_SLOT).is(item);
    }
}