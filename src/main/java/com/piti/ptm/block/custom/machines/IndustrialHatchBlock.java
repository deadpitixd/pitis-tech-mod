package com.piti.ptm.block.custom.machines;

import com.piti.ptm.block.entity.machines.IndustrialHatchBlockEntity;
import com.piti.ptm.item.custom.ScrewdriverItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IndustrialHatchBlock extends IndustrialFurnacePortBlock implements EntityBlock {
    public static final BooleanProperty IS_OUTPUT = BooleanProperty.create("is_output");

    public IndustrialHatchBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FORMED, false)
                .setValue(IS_OUTPUT, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IndustrialHatchBlockEntity(pos, state);
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

    @Override
    public void appendHoverText(@javax.annotation.Nullable ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> pTooltip, @javax.annotation.Nullable TooltipFlag pFlag) {
        pTooltip.add(Component.literal("§6Toggle import/export using a screwdriver."));
    }
}