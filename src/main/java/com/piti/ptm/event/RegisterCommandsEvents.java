package com.piti.ptm.event;

import com.piti.ptm.PitisTech;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PitisTech.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RegisterCommandsEvents {
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        com.piti.ptm.PtmCommands.register(event.getDispatcher());
    }
}
