package com.piti.ptm.block.custom.machines;

import com.piti.ptm.block.entity.machines.IndustrialFurnaceCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class IndustrialCablePortBlockIndustrial extends IndustrialFurnacePortBlock {
    public IndustrialCablePortBlockIndustrial(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        IndustrialFurnaceCoreBlockEntity master = getMaster(level, pos);
        if (master != null) {
            master.checkStructure(null);
        }
    }
}