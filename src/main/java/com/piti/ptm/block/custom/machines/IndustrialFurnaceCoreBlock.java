package com.piti.ptm.block.custom.machines;

import com.piti.ptm.block.entity.machines.IndustrialFurnaceCoreBlockEntity;
import com.piti.ptm.item.custom.ScrewdriverItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;

public class IndustrialFurnaceCoreBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public IndustrialFurnaceCoreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false));
    }

    @Override
    public void appendHoverText(@Nullable ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, @Nullable TooltipFlag pFlag) {
        pTooltip.add(Component.literal("§6Requires a 3x3x3 structure to function."));
        if (Screen.hasShiftDown()) {
            pTooltip.add(Component.literal("§eStructure Components:"));
            pTooltip.add(Component.literal("§7- 1 Core"));
            pTooltip.add(Component.literal("§7- 2 Access Ports"));
            pTooltip.add(Component.literal("§7- 1 Cable Port"));
            pTooltip.add(Component.literal("§7- Cover with Industrial Steel (or other parts)"));
        } else {
            pTooltip.add(Component.literal("§8Hold §f[Shift] §8for more info"));
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nullable BlockPos pos, @Nullable BlockState state) {
        return new IndustrialFurnaceCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @Nullable BlockState state, @Nullable BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof IndustrialFurnaceCoreBlockEntity core) {
                core.tick();
            }
        };
    }

    @Override
    public void neighborChanged(@Nullable BlockState state, @Nullable Level level, @Nullable BlockPos pos, @Nullable Block block, @Nullable BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IndustrialFurnaceCoreBlockEntity core && core.isFormed()) {
            core.checkStructure(null);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IndustrialFurnaceCoreBlockEntity core) {
            if (player.getItemInHand(hand).getItem() instanceof ScrewdriverItem) {
                boolean success = core.checkStructure(null);
                player.displayClientMessage(Component.literal(success ? "Structure Formed!" : "Structure Incomplete"), true);
            } else if (state.getValue(FORMED)) {
            }
        }
        return InteractionResult.CONSUME;
    }
}