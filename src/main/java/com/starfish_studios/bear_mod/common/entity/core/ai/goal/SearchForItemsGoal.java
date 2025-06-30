package com.starfish_studios.bear_mod.common.entity.core.ai.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class SearchForItemsGoal extends Goal {
    private final Mob mob;
    private final Predicate<ItemStack> itemPredicate;
    private final double speedModifier;
    private final int searchRange;
    private ItemEntity targetItem;

    public SearchForItemsGoal(Mob mob, double speedModifier, int searchRange, Predicate<ItemStack> itemPredicate) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.searchRange = searchRange;
        this.itemPredicate = itemPredicate;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTarget() != null) {
            return false;
        }

        List<ItemEntity> items = this.mob.level().getEntitiesOfClass(ItemEntity.class, 
            this.mob.getBoundingBox().inflate(this.searchRange), 
            (itemEntity) -> this.itemPredicate.test(itemEntity.getItem()));

        if (items.isEmpty()) {
            return false;
        }

        this.targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetItem != null && this.targetItem.isAlive() && 
               this.mob.distanceToSqr(this.targetItem) < this.searchRange * this.searchRange;
    }

    @Override
    public void start() {
        if (this.targetItem != null) {
            this.mob.getNavigation().moveTo(this.targetItem, this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.targetItem != null && this.targetItem.isAlive()) {
            double distance = this.mob.distanceToSqr(this.targetItem);
            
            if (distance < 2.0D) {
                // Pick up the item
                this.targetItem.discard();
                this.stop();
            } else {
                this.mob.getNavigation().moveTo(this.targetItem, this.speedModifier);
            }
        }
    }
}

