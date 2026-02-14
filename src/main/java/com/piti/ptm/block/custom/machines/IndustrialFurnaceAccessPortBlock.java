package com.piti.ptm.block.custom.machines;

import com.piti.ptm.block.entity.machines.IndustrialFurnaceCoreBlockEntity;
import com.piti.ptm.item.custom.ScrewdriverItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class IndustrialFurnaceAccessPortBlock extends IndustrialFurnacePortBlock {
    public IndustrialFurnaceAccessPortBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof ScrewdriverItem) {
            if (!level.isClientSide) {
                IndustrialFurnaceCoreBlockEntity core = findCore(level, pos);
                if (core != null) {
                    core.checkStructure(player);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Only open the GUI if NOT holding a screwdriver
        if (!level.isClientSide) {
            IndustrialFurnaceCoreBlockEntity core = findCore(level, pos);
            if (core != null && core.isFormed()) {
                NetworkHooks.openScreen((ServerPlayer) player, core, core.getBlockPos());
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private IndustrialFurnaceCoreBlockEntity findCore(Level level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockEntity be = level.getBlockEntity(pos.offset(x, y, z));
                    if (be instanceof IndustrialFurnaceCoreBlockEntity core) return core;
                }
            }
        }
        return null;
    }
}