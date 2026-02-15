package com.piti.ptm.block.entity.machines;

import com.piti.ptm.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IndustrialFurnacePortBlockEntity extends BlockEntity {
    public IndustrialFurnacePortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INDUSTRIALFURNACEPORT_BE.get(), pos, state);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        IndustrialFurnaceCoreBlockEntity core = findCore();
        if (core != null && core.isFormed()) {
            return core.getCapability(cap, side);
        }
        return super.getCapability(cap, side);
    }

    private IndustrialFurnaceCoreBlockEntity findCore() {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockEntity be = level.getBlockEntity(worldPosition.offset(x, y, z));
                    if (be instanceof IndustrialFurnaceCoreBlockEntity core) return core;
                }
            }
        }
        return null;
    }
}