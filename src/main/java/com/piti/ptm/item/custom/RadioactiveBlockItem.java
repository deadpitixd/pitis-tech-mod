package com.piti.ptm.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

public class RadioactiveBlockItem extends BlockItem implements IRadioactive {

    private final double defaultRadPerSecond;

    public RadioactiveBlockItem(net.minecraft.world.level.block.Block block, Properties properties, double radPerSecond) {
        super(block, properties);
        this.defaultRadPerSecond = radPerSecond;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        this.addRadioactiveTooltip(tooltip);
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onCraftedBy(ItemStack stack, Level level, net.minecraft.world.entity.player.Player player) {
        if (!stack.hasTag() || !Objects.requireNonNull(stack.getTag()).contains("RadPerSecond")) {
            stack.getOrCreateTag().putDouble("RadPerSecond", defaultRadPerSecond);
        }
        super.onCraftedBy(stack, level, player);
    }

    @Override
    public double radPerTick() {
        return defaultRadPerSecond / 20;
    }
}
