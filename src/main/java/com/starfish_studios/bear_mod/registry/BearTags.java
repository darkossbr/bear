package com.starfish_studios.bear_mod.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;

public class BearTags {
    public static final TagKey<EntityType<?>> BEAR_HUNTABLES = TagKey.create(Registries.ENTITY_TYPE, 
            ResourceLocation.fromNamespaceAndPath("bear_mod", "bear_huntables"));
}

