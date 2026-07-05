package com.piti.ptm.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IsotopeItem extends RadioactiveItem {
    final private String isotope;
    public IsotopeItem(Properties properties, double radPerSecond, String iso) {
        super(properties, radPerSecond);
        isotope = iso;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Component label = Component.translatable("tooltip.ptm.isotope").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);
        Component name = Component.literal(" " + isotope).withStyle(ChatFormatting.YELLOW);

        tooltip.add(Component.empty().append(label).append(name));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
