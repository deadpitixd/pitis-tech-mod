package com.piti.ptm.event;

import com.piti.ptm.PitisTech;
import com.piti.ptm.capability.PlayerRadiationData;
import com.piti.ptm.item.custom.IRadioactive;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = PitisTech.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RadiationManager {
    static double randomDecayValue(TickEvent.PlayerTickEvent event){
        return ((0.2 + 0.6 * event.player.level().getRandom().nextDouble()) / 5);
    }
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isServer() && event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            if (player.isCreative() || player.isSpectator()) return;
            Inventory inventory = player.getInventory();

            double staticRadiation = 0.0f;
            double dynamicRadiation = 0.0f;

            Set<Item> uniqueStaticItems = new HashSet<>();

            int totalSlots = inventory.getContainerSize();
            for (int slot = 0; slot < totalSlots; slot++) {
                ItemStack stack = inventory.getItem(slot);

                if (!stack.isEmpty() && stack.getItem() instanceof IRadioactive radItem) {
                    if (radItem.staticRadiation()) {
                        if (!uniqueStaticItems.contains(stack.getItem())) {
                            uniqueStaticItems.add(stack.getItem());
                            staticRadiation += radItem.radPerTick();
                        }
                    } else {
                        dynamicRadiation += radItem.radPerTick() * stack.getCount();
                    }
                }
            }

            double totalExposure = staticRadiation + dynamicRadiation;
            if (totalExposure > 0.0001f) {
                player.getCapability(PlayerRadiationData.INSTANCE).ifPresent(data -> {
                    double newRad = data.getRadExposure() + totalExposure;
                    newRad = Math.round(newRad * 10000.0f) / 10000.0f;

                    data.setRadExposure(newRad);
                    if (data.getRadExposure() >= 1000.0){ player.kill(); }
                });
            }
            else
            {
                player.getCapability(PlayerRadiationData.INSTANCE).ifPresent( data -> {
                    // (Value is a random from 0.2 to 0.6)
                    // decreases the exposure value
                    double newValue = data.getRadExposure() - randomDecayValue(event);
                    data.setRadExposure(newValue < 0 ? 0.0f : newValue);
                    if (data.getRadExposure() >= 1000.0){ player.kill(); }
                });}
            }
    }
}