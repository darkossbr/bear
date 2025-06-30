package com.starfish_studios.bear_mod.registry;

import com.starfish_studios.bear_mod.common.entity.Bear;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BearEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, "bear_mod");

    public static final DeferredHolder<EntityType<?>, EntityType<Bear>> BEAR = ENTITY_TYPES.register("bear",
            () -> EntityType.Builder.of(Bear::new, MobCategory.CREATURE)
                    .sized(1.0F, 1.0F)
                    .build("bear"));
}

