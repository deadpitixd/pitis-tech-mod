package com.piti.ptm.block.custom;

import com.piti.ptm.block.entity.PipeBlockEntity;
import com.piti.ptm.item.custom.FluidTemplateItem;
import com.piti.ptm.item.custom.ScrewdriverItem;
import com.piti.ptm.network.IFluidReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class PipeBlock extends Block implements EntityBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
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
                .setValue(WEST, false).setValue(UP, false).setValue(DOWN, false)
                .setValue(WATERLOGGED, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(pos, state);
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);

            if (stack.getItem() instanceof FluidTemplateItem && be instanceof PipeBlockEntity pipeBE) {
                String newFluid = "";
                if (stack.hasTag() && stack.getTag().contains("FluidID")) {
                    newFluid = stack.getTag().getString("FluidID");
                }

                if (player.isShiftKeyDown()) {
                    updateConnectedPipes(level, pos, newFluid, new HashSet<>());
                } else {
                    pipeBE.setFilterFluidID(newFluid);
                }

                return InteractionResult.SUCCESS;
            }

            if (stack.getItem() instanceof ScrewdriverItem && be instanceof PipeBlockEntity pipe) {
                PipeBlockEntity.PipeMode next = switch (pipe.getMode()) {
                    case NEUTRAL -> PipeBlockEntity.PipeMode.IMPORT;
                    case IMPORT -> PipeBlockEntity.PipeMode.EXPORT;
                    case EXPORT -> PipeBlockEntity.PipeMode.NEUTRAL;
                };
                pipe.setMode(next);

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    private void updateConnectedPipes(Level level, BlockPos pos, String fluidId, Set<BlockPos> visited) {
        if (visited.contains(pos) || visited.size() > 512) return;
        visited.add(pos);

        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof PipeBlockEntity pipeBE) {
            pipeBE.setFilterFluidID(fluidId);
        }

        for (Direction d : Direction.values()) {
            BlockPos neighbor = pos.relative(d);
            if (visited.contains(neighbor)) continue;

            BlockEntity neighborBE = level.getBlockEntity(neighbor);
            boolean shouldVisit = false;

            if (neighborBE instanceof PipeBlockEntity pipeNeighbor) {
                shouldVisit = pipeNeighbor.getFilterFluidID().isEmpty() || pipeNeighbor.getFilterFluidID().equals(fluidId);
            } else if (neighborBE instanceof IFluidReceiver receiver) {
                shouldVisit = receiver.canAcceptFluid(fluidId);
            }

            if (shouldVisit) {
                updateConnectedPipes(level, neighbor, fluidId, visited);
            }
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        boolean water = level.getFluidState(pos).getType() == Fluids.WATER;

        return makeConnections(level, pos, this.defaultBlockState())
                .setValue(WATERLOGGED, water);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {

        if (level instanceof Level lvl && !lvl.isClientSide) {
            System.out.println("[PIPE DEBUG] updateShape at " + pos
                    + " dir=" + direction
                    + " neighbor=" + neighborPos
                    + " state=" + state);
        }

        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return makeConnections(level, pos, defaultBlockState());
    }

    public BlockState makeConnections(LevelAccessor level, BlockPos pos, BlockState state) {
        return state
                .setValue(NORTH, canConnect(level, pos, Direction.NORTH))
                .setValue(EAST,  canConnect(level, pos, Direction.EAST))
                .setValue(SOUTH, canConnect(level, pos, Direction.SOUTH))
                .setValue(WEST,  canConnect(level, pos, Direction.WEST))
                .setValue(UP,    canConnect(level, pos, Direction.UP))
                .setValue(DOWN,  canConnect(level, pos, Direction.DOWN));
    }

    private boolean canConnect(LevelAccessor level, BlockPos selfPos, Direction dirToNeighbor) {
        BlockEntity selfBE = level.getBlockEntity(selfPos);
        if (!(selfBE instanceof PipeBlockEntity selfPipe)) return false;

        BlockPos neighborPos = selfPos.relative(dirToNeighbor);
        BlockEntity neighborBE = level.getBlockEntity(neighborPos);

        if (neighborBE instanceof PipeBlockEntity otherPipe) {
            String id1 = selfPipe.getFilterFluidID();
            String id2 = otherPipe.getFilterFluidID();

            if (id1.isEmpty() || id2.isEmpty()) return true;
            return id1.equals(id2);
        }
        if (neighborBE != null) {
            return neighborBE.getCapability(ForgeCapabilities.FLUID_HANDLER, dirToNeighbor.getOpposite())
                    .isPresent();
        }

        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, WATERLOGGED);
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

    public BlockState updateConnections(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockState newState = state
                .setValue(NORTH, canConnect(level, pos, Direction.NORTH))
                .setValue(EAST,  canConnect(level, pos, Direction.EAST))
                .setValue(SOUTH, canConnect(level, pos, Direction.SOUTH))
                .setValue(WEST,  canConnect(level, pos, Direction.WEST))
                .setValue(UP,    canConnect(level, pos, Direction.UP))
                .setValue(DOWN,  canConnect(level, pos, Direction.DOWN));
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            newState = newState.setValue(BlockStateProperties.WATERLOGGED,
                    state.getValue(BlockStateProperties.WATERLOGGED));
        }

        return newState;
    }

    private static void refreshPipe(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof PipeBlock pipe)) return;

        pipe.makeConnections(level, pos, state);
        BlockState newState = pipe.updateConnections(level, pos, state);
        if (!newState.equals(state)) {
            level.setBlock(pos, newState, 3); // triggers render
        }
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {

        if (!level.isClientSide) {
            System.out.println("[PIPE DEBUG] REMOVED at " + pos
                    + " old=" + state.getBlock().getName().getString()
                    + " new=" + newState.getBlock().getName().getString()
                    + " oldState=" + state
                    + " newState=" + newState);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private boolean supportsFluid(BlockEntity be, String fluidId) {
        if (be instanceof PipeBlockEntity pipe) {
            String id = pipe.getFilterFluidID();
            return id.isEmpty() || id.equals(fluidId);
        }

        return false;
    }
}