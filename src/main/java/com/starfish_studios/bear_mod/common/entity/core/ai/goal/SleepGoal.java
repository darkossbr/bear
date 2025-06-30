package com.starfish_studios.bear_mod.common.entity.core.ai.goal;

import com.starfish_studios.bear_mod.common.entity.core.SleepingAnimal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class SleepGoal extends Goal {
    private final Mob mob;
    private final SleepingAnimal sleepingAnimal;

    public SleepGoal(Mob mob) {
        this.mob = mob;
        this.sleepingAnimal = (SleepingAnimal) mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() == null && !this.mob.level().isDay() && this.mob.getRandom().nextInt(100) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.sleepingAnimal.isSleeping() && this.mob.getTarget() == null && this.mob.level().isNight();
    }

    @Override
    public void start() {
        this.sleepingAnimal.setSleeping(true);
        this.mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.sleepingAnimal.setSleeping(false);
    }

    @Override
    public void tick() {
        this.mob.getNavigation().stop();
        this.mob.getMoveControl().setWantedPosition(this.mob.getX(), this.mob.getY(), this.mob.getZ(), 0.0D);
    }
}

