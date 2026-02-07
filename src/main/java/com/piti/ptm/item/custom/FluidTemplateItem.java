package com.piti.ptm.item.custom;

import com.piti.ptm.block.entity.ModBlockEntities;
import com.piti.ptm.block.entity.PipeBlockEntity;
import com.piti.ptm.fluid.BaseFluidType;
import com.piti.ptm.fluid.IFluidHandlingBlockEntity;
import com.piti.ptm.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

public class FluidTemplateItem extends Item {

    public FluidTemplateItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        ItemStack stack = context.getItemInHand();

        if (!level.isClientSide && be instanceof IFluidHandlingBlockEntity) {
            CompoundTag nbt = stack.getTag();
            if (nbt != null && nbt.contains("FluidID")) {
                String fluidId = nbt.getString("FluidID");

                BlockState state = level.getBlockState(pos);

                if (be instanceof PipeBlockEntity pipe) {
                    pipe.setFilterFluidID(fluidId);
                    System.out.println("[SERVER] Right-click applied: " + fluidId + " to " + pos);
                    level.updateNeighborsAt(pos, state.getBlock());
                }

                level.sendBlockUpdated(pos, state, state, 3);
                level.blockUpdated(pos, state.getBlock());
                level.blockEntityChanged(pos);

                context.getPlayer().sendSystemMessage(Component.literal("Applied filter: " + fluidId));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("FluidID")) {
            String id = stack.getTag().getString("FluidID");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(id));
            if (fluid != null && fluid != Fluids.EMPTY) {
                return Component.translatable(this.getDescriptionId())
                        .append(" (")
                        .append(fluid.getFluidType().getDescription())
                        .append(")");
            }
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.hasTag() && stack.getTag().contains("FluidID")) {
            tooltip.add(Component.literal("Fluid: " + stack.getTag().getString("FluidID")).withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.literal("Empty Template").withStyle(ChatFormatting.GRAY));
        }
    }
}