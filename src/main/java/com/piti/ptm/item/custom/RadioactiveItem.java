package com.piti.ptm.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RadioactiveItem extends Item {

    private final double defaultRadPerSecond;

    public RadioactiveItem(Properties properties, double radPerSecond) {
        super(properties);
        this.defaultRadPerSecond = radPerSecond;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("[RADIOACTIVE]").withStyle(style -> style.withColor(TextColor.fromRgb(0x00FF00))));

        double rad = defaultRadPerSecond;
        if (stack.hasTag() && stack.getTag().contains("RadPerSecond")) {
            rad = stack.getTag().getDouble("RadPerSecond");
        }

        tooltip.add(Component.literal(String.format("§2Rad/s: %.2f", rad)));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, net.minecraft.world.entity.player.Player player) {
        if (!stack.hasTag() || !stack.getTag().contains("RadPerSecond")) {
            stack.getOrCreateTag().putDouble("RadPerSecond", defaultRadPerSecond);
        }
        super.onCraftedBy(stack, level, player);
    }
}
