package com.piti.ptm.block.custom.machines;

import com.piti.ptm.block.entity.machines.IndustrialFurnaceCoreBlockEntity;
import com.piti.ptm.block.entity.machines.IndustrialFurnacePortBlockEntity;
import com.piti.ptm.item.custom.ScrewdriverItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class IndustrialFluidPortBlock extends IndustrialFurnacePortBlock implements EntityBlock {
    public static final BooleanProperty IS_OUTPUT_TANK = BooleanProperty.create("is_output_tank");

    public IndustrialFluidPortBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FORMED, false)
                .setValue(IS_OUTPUT_TANK, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FORMED, IS_OUTPUT_TANK);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof ScrewdriverItem) {
            if (!level.isClientSide) {
                boolean current = state.getValue(IS_OUTPUT_TANK);
                level.setBlock(pos, state.setValue(IS_OUTPUT_TANK, !current), 3);
                player.displayClientMessage(Component.literal("Fluid Port: " + (!current ? "Output Tank" : "Input Tank")), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IndustrialFurnacePortBlockEntity(pos, state);
    }
}