package com.piti.ptm.item.custom;

import com.piti.ptm.capability.PlayerRadiationData;
import com.piti.ptm.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GeigerCounterItem extends Item {

    public GeigerCounterItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof Player player) {
            if (!(isSelected || player.getOffhandItem() == stack)) return;
            player.getCapability(PlayerRadiationData.INSTANCE).ifPresent(data -> {
                ChatFormatting color;
                if (data.getRadExposure() < 50){ color = ChatFormatting.GREEN; }
                else if (data.getRadExposure() <= 100) {
                    color = ChatFormatting.YELLOW;
                } else if (data.getRadExposure() <= 250){
                    color = ChatFormatting.RED;
                } else if (data.getRadExposure() <= 500) {
                    color = ChatFormatting.DARK_RED;
                } else { color = ChatFormatting.BLACK; }
                Component formattedRads = Component.literal(String.format("%.2f",data.getRadExposure()) + " Rads");
                player.displayClientMessage(
                        Component.translatable("tooltip.ptm.currentExposure").withStyle(color)
                                .append(formattedRads),
                        true
                );
                final double rad = data.getRadExposure();
                if (rad > 0.0f) {
                    double clickChance = Math.min(rad * 0.1, 0.8);

                    if (level.random.nextDouble() < clickChance) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, SoundSource.PLAYERS,
                                0.6f, 1.6f + (level.random.nextFloat() * 0.4f));
                    }
                }
            });
        }
    }
}
