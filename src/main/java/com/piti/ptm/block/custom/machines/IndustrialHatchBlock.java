package com.piti.ptm.block.custom.machines;

import com.piti.ptm.item.custom.ScrewdriverItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class IndustrialHatchBlock extends IndustrialFurnacePortBlock {
    public static final BooleanProperty IS_OUTPUT = BooleanProperty.create("is_output");

    public IndustrialHatchBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FORMED, false)
                .setValue(IS_OUTPUT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FORMED, IS_OUTPUT);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof ScrewdriverItem) {
            if (!level.isClientSide) {
                boolean current = state.getValue(IS_OUTPUT);
                level.setBlock(pos, state.setValue(IS_OUTPUT, !current), 3);
                player.displayClientMessage(Component.literal("Hatch Mode: " + (!current ? "Output" : "Input")), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}