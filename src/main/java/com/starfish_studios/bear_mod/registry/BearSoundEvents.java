package com.starfish_studios.bear_mod.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BearSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, "bear_mod");

    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_AMBIENT = SOUND_EVENTS.register("bear_ambient",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("bear_mod", "bear_ambient")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_WARNING = SOUND_EVENTS.register("bear_warning",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("bear_mod", "bear_warning")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_HURT = SOUND_EVENTS.register("bear_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("bear_mod", "bear_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BEAR_DEATH = SOUND_EVENTS.register("bear_death",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("bear_mod", "bear_death")));
}

