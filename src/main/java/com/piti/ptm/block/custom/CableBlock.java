package com.piti.ptm.block.custom;

import com.piti.ptm.block.entity.CableBlockEntity;
import com.piti.ptm.item.custom.ScrewdriverItem;
import com.piti.ptm.network.NetworkManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CableBlock extends Block implements SimpleWaterloggedBlock, EntityBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected static final VoxelShape CORE = Block.box(5.5, 5.5, 5.5, 10.5, 10.5, 10.5);

    protected static final Map<Direction, VoxelShape> EXTENSIONS = Map.of(
            Direction.NORTH, Block.box(5.5, 5.5, 0, 10.5, 10.5, 5.5),
            Direction.SOUTH, Block.box(5.5, 5.5, 10.5, 10.5, 16, 10.5),
            Direction.EAST, Block.box(10.5, 5.5, 5.5, 16, 10.5, 10.5),
            Direction.WEST, Block.box(0, 5.5, 5.5, 5.5, 10.5, 10.5),
            Direction.UP, Block.box(5.5, 10.5, 5.5, 10.5, 16, 10.5),
            Direction.DOWN, Block.box(5.5, 0, 5.5, 10.5, 5.5, 10.5)
    );

    public CableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    @MethodsReturnNonnullByDefault
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof ScrewdriverItem) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof CableBlockEntity cable) {
                    Direction side = hit.getDirection();
                    cable.toggleSideMode(side);
                    player.displayClientMessage(Component.literal("Cable Side " + side.getName() + ": " + cable.getSideMode(side).name()), true);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, EXTENSIONS.get(Direction.NORTH));
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, EXTENSIONS.get(Direction.SOUTH));
        if (state.getValue(EAST)) shape = Shapes.or(shape, EXTENSIONS.get(Direction.EAST));
        if (state.getValue(WEST)) shape = Shapes.or(shape, EXTENSIONS.get(Direction.WEST));
        if (state.getValue(UP)) shape = Shapes.or(shape, EXTENSIONS.get(Direction.UP));
        if (state.getValue(DOWN)) shape = Shapes.or(shape, EXTENSIONS.get(Direction.DOWN));
        return shape;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return this.defaultBlockState()
                .setValue(WATERLOGGED, world.getFluidState(pos).getType() == Fluids.WATER)
                .setValue(NORTH, canConnectTo(world, pos.north(), Direction.NORTH))
                .setValue(SOUTH, canConnectTo(world, pos.south(), Direction.SOUTH))
                .setValue(EAST, canConnectTo(world, pos.east(), Direction.EAST))
                .setValue(WEST, canConnectTo(world, pos.west(), Direction.WEST))
                .setValue(UP, canConnectTo(world, pos.above(), Direction.UP))
                .setValue(DOWN, canConnectTo(world, pos.below(), Direction.DOWN));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CableBlockEntity cable) {
                cable.scanNeighbors();
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return state.setValue(getPropForDirection(direction), canConnectTo(world, neighborPos, direction));
    }

    private boolean canConnectTo(LevelAccessor world, BlockPos pos, Direction direction) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof CableBlock) {
            return true;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            return blockEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).isPresent();
        }
        return false;
    }
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CableBlockEntity cable) {
                NetworkManager.addCable(cable);
                System.out.println("Cable placed and added to update queue");
            }
        }
    }

    private BooleanProperty getPropForDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }
}