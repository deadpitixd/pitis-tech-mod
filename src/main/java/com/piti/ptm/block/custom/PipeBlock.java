package com.piti.ptm.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class PipeBlock extends Block {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    public PipeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false)
                .setValue(WEST, false).setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return makeConnections(context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return makeConnections(level, pos);
    }

    public BlockState makeConnections(LevelAccessor level, BlockPos pos) {
        return this.defaultBlockState()
                .setValue(NORTH, canConnect(level, pos.north(), Direction.SOUTH))
                .setValue(EAST,  canConnect(level, pos.east(),  Direction.WEST))
                .setValue(SOUTH, canConnect(level, pos.south(), Direction.NORTH))
                .setValue(WEST,  canConnect(level, pos.west(),  Direction.EAST))
                .setValue(UP,    canConnect(level, pos.above(), Direction.DOWN))
                .setValue(DOWN,  canConnect(level, pos.below(), Direction.UP));
    }

    private boolean canConnect(LevelAccessor level, BlockPos neighborPos, Direction side) {
        BlockEntity be = level.getBlockEntity(neighborPos);
        if (be != null) {
            return be.getCapability(ForgeCapabilities.FLUID_HANDLER, side).isPresent();
        }
        return level.getBlockState(neighborPos).getBlock() instanceof PipeBlock;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }
    private static final VoxelShape CORE_SHAPE = Block.box(5.5, 5.5, 5.5, 10.5, 10.5, 10.5);
    private static final VoxelShape NORTH_SHAPE = Block.box(5.5, 5.5, 0.0, 10.5, 10.5, 5.5);
    private static final VoxelShape SOUTH_SHAPE = Block.box(5.5, 5.5, 10.5, 10.5, 10.5, 16.0);
    private static final VoxelShape EAST_SHAPE = Block.box(10.5, 5.5, 5.5, 16.0, 10.5, 10.5);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0, 5.5, 5.5, 5.5, 10.5, 10.5);
    private static final VoxelShape UP_SHAPE = Block.box(5.5, 10.5, 5.5, 10.5, 16.0, 10.5);
    private static final VoxelShape DOWN_SHAPE = Block.box(5.5, 0.0, 5.5, 10.5, 5.5, 10.5);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE_SHAPE;

        if (state.getValue(NORTH)) shape = Shapes.joinUnoptimized(shape, NORTH_SHAPE, BooleanOp.OR);
        if (state.getValue(SOUTH)) shape = Shapes.joinUnoptimized(shape, SOUTH_SHAPE, BooleanOp.OR);
        if (state.getValue(EAST))  shape = Shapes.joinUnoptimized(shape, EAST_SHAPE, BooleanOp.OR);
        if (state.getValue(WEST))  shape = Shapes.joinUnoptimized(shape, WEST_SHAPE, BooleanOp.OR);
        if (state.getValue(UP))    shape = Shapes.joinUnoptimized(shape, UP_SHAPE, BooleanOp.OR);
        if (state.getValue(DOWN))  shape = Shapes.joinUnoptimized(shape, DOWN_SHAPE, BooleanOp.OR);

        return shape;
    }

    // This ensures light passes through the pipe properly
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}