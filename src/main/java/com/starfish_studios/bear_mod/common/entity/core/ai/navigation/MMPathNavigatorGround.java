package com.starfish_studios.bear_mod.common.entity.core.ai.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;

public class MMPathNavigatorGround extends GroundPathNavigation {
    public MMPathNavigatorGround(Mob mob, Level level) {
        super(mob, level);
    }
}

