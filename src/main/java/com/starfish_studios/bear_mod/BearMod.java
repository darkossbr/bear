package com.starfish_studios.bear_mod;

import com.starfish_studios.bear_mod.common.entity.Bear;
import com.starfish_studios.bear_mod.registry.BearEntityTypes;
import com.starfish_studios.bear_mod.registry.BearSoundEvents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod("bear_mod")
public class BearMod {
    public BearMod(IEventBus modEventBus) {
        BearEntityTypes.ENTITY_TYPES.register(modEventBus);
        BearSoundEvents.SOUND_EVENTS.register(modEventBus);
        
        modEventBus.addListener(this::onEntityAttributeCreation);
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(BearEntityTypes.BEAR.get(), Bear.createAttributes().build());
    }
}

