package com.piti.ptm.block.entity.machines;

import com.piti.ptm.block.ModBlocks;
import com.piti.ptm.block.custom.machines.IndustrialFurnaceCoreBlock;
import com.piti.ptm.block.custom.machines.IndustrialFurnacePortBlock;
import com.piti.ptm.block.entity.ModBlockEntities;
import com.piti.ptm.item.custom.PunchedCardItem;
import com.piti.ptm.recipe.IndustrialFurnaceRecipe;
import com.piti.ptm.screen.IndustrialFurnaceMenu;
import com.piti.ptm.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
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

import java.util.Optional;

public class IndustrialFurnaceCoreBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler inventory = new ItemStackHandler(14) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final FluidTank inputTank = new FluidTank(16000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final FluidTank outputTank = new FluidTank(16000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final EnergyStorage energy = new EnergyStorage(100000, 10000, 10000);

    private final LazyOptional<IItemHandler> inventoryOptional = LazyOptional.of(() -> inventory);
    private final LazyOptional<IFluidHandler> inputTankOptional = LazyOptional.of(() -> inputTank);
    private final LazyOptional<IFluidHandler> outputTankOptional = LazyOptional.of(() -> outputTank);
    private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> energy);

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 100;
    private boolean isFormed = false;

    public IndustrialFurnaceCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INDUSTRIALFURNACE_BE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> IndustrialFurnaceCoreBlockEntity.this.progress;
                    case 1 -> IndustrialFurnaceCoreBlockEntity.this.maxProgress;
                    case 2 -> IndustrialFurnaceCoreBlockEntity.this.inputTank.getFluidAmount();
                    case 3 -> IndustrialFurnaceCoreBlockEntity.this.outputTank.getFluidAmount();
                    case 4 -> IndustrialFurnaceCoreBlockEntity.this.energy.getEnergyStored();
                    case 5 -> IndustrialFurnaceCoreBlockEntity.this.energy.getMaxEnergyStored();
                    case 6 -> BuiltInRegistries.FLUID.getId(IndustrialFurnaceCoreBlockEntity.this.inputTank.getFluid().getFluid());
                    case 7 -> BuiltInRegistries.FLUID.getId(IndustrialFurnaceCoreBlockEntity.this.outputTank.getFluid().getFluid());
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> IndustrialFurnaceCoreBlockEntity.this.progress = value;
                    case 1 -> IndustrialFurnaceCoreBlockEntity.this.maxProgress = value;
                }
            }

            @Override
            public int getCount() {
                return 8;
            }
        };
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        if (!isFormed) {
            if (level.getGameTime() % 20 == 0) checkStructure(null);
            resetProgress();
            return;
        }

        ItemStack card = inventory.getStackInSlot(13);
        if (card.isEmpty() || !(card.getItem() instanceof PunchedCardItem)) {
            resetProgress();
            return;
        }

        ResourceLocation recipeId = PunchedCardItem.getRecipeId(card);
        if (recipeId == null) {
            resetProgress();
            return;
        }

        Optional<IndustrialFurnaceRecipe> recipe = level.getRecipeManager().byKey(recipeId)
                .filter(r -> r instanceof IndustrialFurnaceRecipe)
                .map(r -> (IndustrialFurnaceRecipe) r);

        if (recipe.isPresent()) {
            IndustrialFurnaceRecipe activeRecipe = recipe.get();
            if (canCraft(activeRecipe)) {
                this.maxProgress = activeRecipe.getTime();
                this.progress++;
                setChanged(level, pos, state);

                if (this.progress >= this.maxProgress) {
                    craftItem(activeRecipe);
                }
            } else {
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    private boolean canCraft(IndustrialFurnaceRecipe recipe) {
        if (energy.getEnergyStored() < recipe.getEnergy()) return false;

        SimpleContainer container = new SimpleContainer(9);
        for (int i = 0; i < 9; i++) container.setItem(i, inventory.getStackInSlot(i));
        if (!recipe.matches(container, level)) return false;

        FluidStack reqFluid = recipe.getInputFluid();
        if (!reqFluid.isEmpty()) {
            if (!inputTank.getFluid().isFluidEqual(reqFluid) || inputTank.getFluidAmount() < reqFluid.getAmount()) {
                return false;
            }
        }

        return canInsertResults(recipe.getOutputItems(), recipe.getOutputFluid());
    }

    private boolean canInsertResults(NonNullList<ItemStack> items, FluidStack fluid) {
        ItemStack[] simulatedSlots = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            simulatedSlots[i] = inventory.getStackInSlot(9 + i).copy();
        }

        for (ItemStack result : items) {
            ItemStack toInsert = result.copy();
            boolean placed = false;

            for (int i = 0; i < 4; i++) {
                ItemStack stackInSlot = simulatedSlots[i];

                if (stackInSlot.isEmpty()) {
                    simulatedSlots[i] = toInsert.copy();
                    placed = true;
                    break;
                } else if (ItemStack.isSameItem(stackInSlot, toInsert) &&
                        stackInSlot.getCount() < stackInSlot.getMaxStackSize()) {

                    int countCanAdd = Math.min(toInsert.getCount(), stackInSlot.getMaxStackSize() - stackInSlot.getCount());
                    stackInSlot.grow(countCanAdd);
                    toInsert.shrink(countCanAdd);

                    if (toInsert.isEmpty()) {
                        placed = true;
                        break;
                    }
                }
            }
            if (!placed) return false;
        }
        if (!fluid.isEmpty()) {
            return outputTank.fill(fluid, IFluidHandler.FluidAction.SIMULATE) == fluid.getAmount();
        }

        return true;
    }

    private void craftItem(IndustrialFurnaceRecipe recipe) {
        energy.extractEnergy(recipe.getEnergy(), false);

        for (int i = 0; i < 9; i++) {
            inventory.extractItem(i, 1, false);
        }

        inputTank.drain(recipe.getInputFluid(), IFluidHandler.FluidAction.EXECUTE);

        NonNullList<ItemStack> results = recipe.getOutputItems();
        for (ItemStack resultStack : results) {
            ItemStack remainder = resultStack.copy();

            for (int slot = 9; slot <= 12; slot++) {
                remainder = inventory.insertItem(slot, remainder, false);
                if (remainder.isEmpty()) break;
            }
        }
        if (!recipe.getOutputFluid().isEmpty()) {
            outputTank.fill(recipe.getOutputFluid(), IFluidHandler.FluidAction.EXECUTE);
        }

        resetProgress();
    }

    private void resetProgress() {
        this.progress = 0;
    }

    public boolean checkStructure(@Nullable Player player) {
        boolean structureValid = true;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos targetPos = worldPosition.offset(x, y, z);
                    if (!isValidPart(targetPos)) {
                        structureValid = false;
                        break;
                    }
                }
            }
        }
        if (structureValid != this.isFormed) {
            this.isFormed = structureValid;
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                updateNeighbors(this.isFormed);
            }
            setChanged();
        }
        return structureValid;
    }

    private boolean isValidPart(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(ModTags.Blocks.FURNACE_REQUIRED) || state.is(ModTags.Blocks.FURNACE_OPTIONAL)) return true;
        Block block = state.getBlock();
        return block == ModBlocks.INDUSTRIAL_STEEL.get() ||
                block == ModBlocks.INDUSTRIAL_FURNACE_ACCESSPORT.get() ||
                block == ModBlocks.INDUSTRIAL_FURNACE_CABLEPORT.get() ||
                block == ModBlocks.INDUSTRIAL_FURNACE_STORAGE.get() ||
                block == ModBlocks.INDUSTRIAL_FURNACE_FLUIDPORT.get();
    }

    private void updateNeighbors(boolean formed) {
        Direction facing = getBlockState().getValue(IndustrialFurnaceCoreBlock.FACING);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos targetPos = relativeToWorld(x, y, z, facing);
                    BlockState state = level.getBlockState(targetPos);
                    if (state.hasProperty(IndustrialFurnacePortBlock.FORMED)) {
                        level.setBlock(targetPos, state.setValue(IndustrialFurnacePortBlock.FORMED, formed), 3);
                    }
                }
            }
        }
    }

    private BlockPos relativeToWorld(int x, int y, int z, Direction facing) {
        return switch (facing) {
            case NORTH -> worldPosition.offset(x, y, z);
            case SOUTH -> worldPosition.offset(-x, y, -z);
            case WEST -> worldPosition.offset(z, y, -x);
            case EAST -> worldPosition.offset(-z, y, x);
            default -> worldPosition.offset(x, y, z);
        };
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return inventoryOptional.cast();
        if (cap == ForgeCapabilities.ENERGY) return energyOptional.cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (side == Direction.DOWN) return outputTankOptional.cast();
            return inputTankOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryOptional.invalidate();
        inputTankOptional.invalidate();
        outputTankOptional.invalidate();
        energyOptional.invalidate();
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
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() { return Component.literal("Industrial Furnace"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new IndustrialFurnaceMenu(id, inv, this, this.data);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        inventory.deserializeNBT(pTag.getCompound("inventory"));
        this.isFormed = pTag.getBoolean("formed");
        this.progress = pTag.getInt("progress");
        this.maxProgress = pTag.getInt("maxProgress");
        inputTank.readFromNBT(pTag.getCompound("inputTank"));
        outputTank.readFromNBT(pTag.getCompound("outputTank"));
        energy.receiveEnergy(pTag.getInt("energy"), false);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", inventory.serializeNBT());
        pTag.putBoolean("formed", this.isFormed);
        pTag.putInt("progress", this.progress);
        pTag.putInt("maxProgress", this.maxProgress);
        pTag.put("inputTank", inputTank.writeToNBT(new CompoundTag()));
        pTag.put("outputTank", outputTank.writeToNBT(new CompoundTag()));
        pTag.putInt("energy", energy.getEnergyStored());
        super.saveAdditional(pTag);
    }

    public boolean isFormed() {
        return isFormed;
    }
    public IItemHandler getItemHandler() {
        return this.inventory;
    }
    public FluidStack getInputFluid() {
        return inputTank.getFluid();
    }

    public FluidStack getOutputFluid() {
        return outputTank.getFluid();
    }
    public FluidTank getInputTank() {
        return this.inputTank;
    }

    public FluidTank getOutputTank() {
        return this.outputTank;
    }

}