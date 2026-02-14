package com.piti.ptm.block.entity.machines;

import com.piti.ptm.block.custom.machines.IndustrialFurnacePortBlock;
import com.piti.ptm.block.custom.machines.IndustrialFurnaceCoreBlock;
import com.piti.ptm.block.entity.ModBlockEntities;
import com.piti.ptm.block.ModBlocks;
import com.piti.ptm.screen.IndustrialFurnaceMenu;
import com.piti.ptm.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class IndustrialFurnaceCoreBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler inventory = new ItemStackHandler(14);
    private final FluidTank inputTank = new FluidTank(4000);
    private final FluidTank outputTank = new FluidTank(4000);
    private final ContainerData data;
    private boolean isFormed = false;

    public IndustrialFurnaceCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INDUSTRIALFURNACE_BE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) { return 0; }
            @Override
            public void set(int index, int value) {}
            @Override
            public int getCount() { return 2; }
        };
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        nbt.putBoolean("formed", this.isFormed);
        return nbt;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.isFormed = tag.getBoolean("formed");
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean isFormed() {
        return this.isFormed;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Industrial Furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new IndustrialFurnaceMenu(id, inv, this, this.data);
    }

    public boolean checkStructure(Player player) {
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
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            setChanged();
            updateNeighbors(this.isFormed);
        }

        return structureValid;
    }

    public void tick() {
        if (level != null && !level.isClientSide) {
            if (!isFormed && level.getGameTime() % 20 == 0) {
                checkStructure(null);
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

    private boolean isValidPart(BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.is(ModTags.Blocks.FURNACE_REQUIRED) || state.is(ModTags.Blocks.FURNACE_OPTIONAL)) {
            return true;
        }

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

    public IItemHandler getItemHandler() { return inventory; }
    public FluidTank getInputTank() { return inputTank; }
    public FluidTank getOutputTank() { return outputTank; }
}