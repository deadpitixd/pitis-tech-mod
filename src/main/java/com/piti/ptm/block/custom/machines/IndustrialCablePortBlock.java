package com.piti.ptm.block.custom.machines;

import com.piti.ptm.block.entity.machines.IndustrialFurnaceCoreBlockEntity;
import com.piti.ptm.block.entity.machines.IndustrialFurnacePortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class IndustrialCablePortBlock extends IndustrialFurnacePortBlock implements EntityBlock {
    public IndustrialCablePortBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            IndustrialFurnaceCoreBlockEntity core = findCore(level, pos);
            if (core != null) core.checkStructure(null);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IndustrialFurnacePortBlockEntity(pos, state);
    }

    private IndustrialFurnaceCoreBlockEntity findCore(Level level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockEntity be = level.getBlockEntity(pos.offset(x, y, z));
                    if (be instanceof IndustrialFurnaceCoreBlockEntity core) return core;
                }
            }
        }
        return null;
    }
}