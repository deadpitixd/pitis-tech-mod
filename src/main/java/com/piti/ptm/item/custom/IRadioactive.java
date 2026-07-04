package com.piti.ptm.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import java.util.List;

public interface IRadioactive {
    // radiation per tick
    default double radPerTick(){
        return 0.5f;
    }
    // Static radiation means that radPerTick() won't change
    default boolean staticRadiation(){
        return false;
    }
    // Should be placed at the end
    default void addRadioactiveTooltip(List<Component> tooltip){
        tooltip.add(Component.translatable("tooltip.ptm.radioactive.tooltip")
                .withStyle(style -> style.withColor(TextColor.fromRgb(0x00FF00))));
        tooltip.add(Component.literal((radPerTick()*20 + " Rad/s")).withStyle(ChatFormatting.DARK_GREEN));
    }
}
