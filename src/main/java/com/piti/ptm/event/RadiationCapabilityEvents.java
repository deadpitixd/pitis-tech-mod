package com.piti.ptm.event;

import com.piti.ptm.PitisTech;
import com.piti.ptm.capability.PlayerRadiationData;
import com.piti.ptm.capability.RadiationDataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PitisTech.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RadiationCapabilityEvents {

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerRadiationData.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(
                    ResourceLocation.fromNamespaceAndPath(PitisTech.MOD_ID, "radiation"),
                    new RadiationDataProvider()
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Keeps the radiation levels intact through dimension changes and respawns
        event.getOriginal().getCapability(PlayerRadiationData.INSTANCE).ifPresent(oldData -> {
            event.getEntity().getCapability(PlayerRadiationData.INSTANCE).ifPresent(newData -> {
                newData.setRadExposure(oldData.getRadExposure());
            });
        });
    }
}