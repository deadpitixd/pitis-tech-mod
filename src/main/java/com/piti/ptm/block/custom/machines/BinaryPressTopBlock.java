package com.piti.ptm.block.custom.machines;

import com.piti.ptm.block.ModBlocks;
import com.piti.ptm.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

public class BinaryPressTopBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final Map<Direction, VoxelShape> SHAPES = new HashMap<>();

    static {
        SHAPES.put(Direction.SOUTH, Shapes.or(Block.box(0, 2, 4, 10, 3, 16), Block.box(8, 1, 11, 10, 2, 16), Block.box(0, 0, 4, 10, 1, 16), Block.box(0, 1, 11, 2, 2, 16), Block.box(0, 1, 4, 10, 2, 11), Block.box(1, 3, 2, 15, 16, 4), Block.box(0, 0, 1, 16, 3, 4), Block.box(11, 0, 7, 15, 2, 11), Block.box(13, 0, 3, 15, 2, 5)));
        SHAPES.put(Direction.NORTH, Shapes.or(Block.box(6, 2, 0, 16, 3, 12), Block.box(6, 1, 0, 8, 2, 5), Block.box(6, 0, 0, 16, 1, 12), Block.box(14, 1, 0, 16, 2, 5), Block.box(6, 1, 5, 16, 2, 12), Block.box(1, 3, 12, 15, 16, 14), Block.box(0, 0, 12, 16, 3, 15), Block.box(1, 0, 5, 5, 2, 9), Block.box(1, 0, 11, 3, 2, 13)));
        SHAPES.put(Direction.WEST, Shapes.or(Block.box(0, 2, 0, 12, 3, 10), Block.box(0, 1, 8, 5, 2, 10), Block.box(0, 0, 0, 12, 1, 10), Block.box(0, 1, 0, 5, 2, 2), Block.box(5, 1, 0, 12, 2, 10), Block.box(12, 3, 1, 14, 16, 15), Block.box(12, 0, 0, 15, 3, 16), Block.box(5, 0, 11, 9, 2, 15), Block.box(11, 0, 13, 13, 2, 15)));
        SHAPES.put(Direction.EAST, Shapes.or(Block.box(4, 2, 6, 16, 3, 16), Block.box(11, 1, 6, 16, 2, 8), Block.box(4, 0, 6, 16, 1, 16), Block.box(11, 1, 14, 16, 2, 16), Block.box(4, 1, 6, 11, 2, 16), Block.box(2, 3, 1, 4, 16, 15), Block.box(1, 0, 0, 4, 3, 16), Block.box(7, 0, 1, 11, 2, 5), Block.box(3, 0, 1, 5, 2, 3)));
    }

    public BinaryPressTopBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public @org.jetbrains.annotations.Nullable PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }


    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return new ItemStack(ModItems.BINARY_PRESS.get());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.getOrDefault(state.getValue(FACING), SHAPES.get(Direction.NORTH));
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos belowPos = pos.below();
        if (level.getBlockState(belowPos).is(ModBlocks.BINARY_PRESS_BOTTOM.get())) {
            level.destroyBlock(belowPos, !player.isCreative());
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}