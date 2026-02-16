package com.piti.ptm.network;

import com.piti.ptm.PitisTech;
import com.piti.ptm.block.entity.CableBlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = PitisTech.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NetworkManager {
    private static final List<EnergyNetwork> networks = new ArrayList<>();
    private static final List<CableBlockEntity> cablesToUpdate = new ArrayList<>();

    public static void registerNetwork(EnergyNetwork network) {
        if (!networks.contains(network)) {
            networks.add(network);
        }
    }

    public static void invalidateNetwork(EnergyNetwork network) {
        networks.remove(network);
        for (var pos : network.getCables()) {
            if (network.getLevel().getBlockEntity(pos) instanceof CableBlockEntity cable) {
                cable.setNetwork(null);
                addCable(cable);
            }
        }
    }

    public static void addCable(CableBlockEntity cable) {
        if (!cablesToUpdate.contains(cable)) {
            cablesToUpdate.add(cable);
        }
    }

    public static void removeCable(CableBlockEntity cable) {
        cablesToUpdate.remove(cable);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide) {
            if (!cablesToUpdate.isEmpty()) {
                List<CableBlockEntity> copy = new ArrayList<>(cablesToUpdate);
                cablesToUpdate.clear();

                for (CableBlockEntity cable : copy) {
                    if (!cable.isRemoved()) {
                        cable.updateNetwork();
                    }
                }
            }

            for (EnergyNetwork net : networks) {
                if (net.getLevel() == event.level) {
                    net.tick();
                }
            }
        }
    }
}