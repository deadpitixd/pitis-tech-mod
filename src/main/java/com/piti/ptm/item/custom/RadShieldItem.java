package com.piti.ptm.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RadShieldItem extends BlockItem {


    public RadShieldItem(net.minecraft.world.level.block.Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("[RADIATION SHIELDING]").withStyle(style -> style.withColor(TextColor.fromRgb(0x00FF00))));

        super.appendHoverText(stack, level, tooltip, flag);
    }
}
