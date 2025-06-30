package com.starfish_studios.bear_mod.client;

import com.starfish_studios.bear_mod.client.renderer.BearRenderer;
import com.starfish_studios.bear_mod.registry.BearEntityTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = "bear_mod", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BearModClient {
    
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BearEntityTypes.BEAR.get(), BearRenderer::new);
    }
}

