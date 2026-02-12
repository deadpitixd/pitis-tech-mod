package com.piti.ptm.renderer;

import com.piti.ptm.block.entity.ModBlockEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class ClientSetup {

    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.PIPE.get(), PipeBlockRenderer::new);
    }
}