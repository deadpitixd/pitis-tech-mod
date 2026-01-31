package com.piti.ptm.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class InfiniteWaterItem extends Item {
    public InfiniteWaterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ptm.infinite_water.description")
                .withStyle(ChatFormatting.BLUE));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> new FluidHandlerItemStack(stack, 1000) {
                @NotNull
                @Override
                public FluidStack getFluidInTank(int tank) {
                    return new FluidStack(Fluids.WATER, 100000000);
                }

                @Override
                public int fill(FluidStack resource, FluidAction action) {
                    return 0;
                }

                @NotNull
                @Override
                public FluidStack drain(int maxDrain, FluidAction action) {
                    return new FluidStack(Fluids.WATER, Math.min(maxDrain, 100000000));
                }
            });

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, holder);
            }
        };
    }
}