package com.piti.ptm.block.custom;

import com.piti.ptm.block.entity.PipeBlockEntity;
import com.piti.ptm.item.custom.FluidTemplateItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class PipeBlock extends Block implements EntityBlock {
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof FluidTemplateItem) {
            if (!level.isClientSide) {
                String newFluid = "";
                if (stack.hasTag() && stack.getTag().contains("FluidID")) {
                    newFluid = stack.getTag().getString("FluidID");
                }

                if (player.isShiftKeyDown()) {
                    updateConnectedPipes(level, pos, newFluid, new HashSet<>());
                } else {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof PipeBlockEntity pipeBE) {
                        pipeBE.setFilterFluidID(newFluid);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void updateConnectedPipes(Level level, BlockPos pos, String fluidId, Set<BlockPos> visited) {
        if (visited.contains(pos) || visited.size() > 512) return;
        visited.add(pos);

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PipeBlockEntity pipeBE) {
            pipeBE.setFilterFluidID(fluidId);
            for (Direction dir : Direction.values()) {
                BlockPos nextPos = pos.relative(dir);
                if (level.getBlockState(nextPos).getBlock() instanceof PipeBlock) {
                    updateConnectedPipes(level, nextPos, fluidId, visited);
                }
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            BlockState newState = makeConnections(level, pos, state);
            if (newState != state) {
                level.setBlock(pos, newState, 3);
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return makeConnections(context.getLevel(), context.getClickedPos(), this.defaultBlockState());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return makeConnections(level, pos, state);
    }

    public BlockState makeConnections(LevelAccessor level, BlockPos pos, BlockState state) {
        return state
                .setValue(NORTH, canConnect(level, pos.north(), Direction.SOUTH))
                .setValue(EAST,  canConnect(level, pos.east(),  Direction.WEST))
                .setValue(SOUTH, canConnect(level, pos.south(), Direction.NORTH))
                .setValue(WEST,  canConnect(level, pos.west(),  Direction.EAST))
                .setValue(UP,    canConnect(level, pos.above(), Direction.DOWN))
                .setValue(DOWN,  canConnect(level, pos.below(), Direction.UP));
    }

    private boolean canConnect(LevelAccessor level, BlockPos pos, Direction dir) {
        BlockPos otherPos = pos.relative(dir);

        BlockState otherState = level.getBlockState(otherPos);
        if (!(otherState.getBlock() instanceof PipeBlock)) return false;

        if (!(level instanceof Level realLevel)) return false;

        BlockEntity be1 = realLevel.getBlockEntity(pos);
        BlockEntity be2 = realLevel.getBlockEntity(otherPos);

        if (!(be1 instanceof PipeBlockEntity pipe1)) return false;
        if (!(be2 instanceof PipeBlockEntity pipe2)) return false;

        String id1 = pipe1.getFilterFluidID();
        String id2 = pipe2.getFilterFluidID();

        if (id1.isEmpty() || id2.isEmpty()) {
            return id1.isEmpty() && id2.isEmpty();
        }

        return id1.equals(id2);
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

    private BlockState updateConnections(LevelAccessor level, BlockPos pos, BlockState state) {
        return state
                .setValue(NORTH, canConnect(level, pos.north(), Direction.SOUTH))
                .setValue(EAST,  canConnect(level, pos.east(),  Direction.WEST))
                .setValue(SOUTH, canConnect(level, pos.south(), Direction.NORTH))
                .setValue(WEST,  canConnect(level, pos.west(),  Direction.EAST))
                .setValue(UP,    canConnect(level, pos.above(), Direction.DOWN))
                .setValue(DOWN,  canConnect(level, pos.below(), Direction.UP));
    }

    private static void refreshPipe(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof PipeBlock pipe)) return;

        BlockState newState = pipe.updateConnections(level, pos, state);

        if (newState != state) {
            level.setBlock(pos, newState, 3);
        } else {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);

        if (!level.isClientSide) {
            for (Direction d : Direction.values()) {
                refreshPipe(level, pos.relative(d));
            }
        }
    }
}