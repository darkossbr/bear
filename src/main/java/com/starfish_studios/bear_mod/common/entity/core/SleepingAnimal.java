package com.starfish_studios.bear_mod.common.entity.core;

public interface SleepingAnimal {
    int getLastPose();
    void setLastPose(int pose);
    boolean isSleeping();
    void setSleeping(boolean sleeping);
}

