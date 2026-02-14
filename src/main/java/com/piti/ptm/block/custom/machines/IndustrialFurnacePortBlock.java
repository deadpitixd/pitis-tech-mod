package com.piti.ptm.block.custom.machines;

import com.piti.ptm.block.entity.machines.IndustrialFurnaceCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class IndustrialFurnacePortBlock extends Block {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public IndustrialFurnacePortBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
    }

    protected IndustrialFurnaceCoreBlockEntity getMaster(Level level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockEntity be = level.getBlockEntity(pos.offset(x, y, z));
                    if (be instanceof IndustrialFurnaceCoreBlockEntity core) {
                        return core;
                    }
                }
            }
        }
        return null;
    }
}