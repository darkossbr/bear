package com.starfish_studios.bear_mod.common.entity;

import com.starfish_studios.bear_mod.common.entity.core.NaturalistAnimal;
import com.starfish_studios.bear_mod.common.entity.core.SleepingAnimal;
import com.starfish_studios.bear_mod.common.entity.core.ai.goal.*;
import com.starfish_studios.bear_mod.common.entity.core.ai.navigation.MMPathNavigatorGround;
import com.starfish_studios.bear_mod.common.entity.core.ai.navigation.SmartBodyHelper;
import com.starfish_studios.bear_mod.registry.BearEntityTypes;
import com.starfish_studios.bear_mod.registry.BearSoundEvents;
import com.starfish_studios.bear_mod.registry.BearTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class Bear extends Animal implements GeoEntity, SleepingAnimal {
    private static final EntityDataAccessor<Integer> DATA_LAST_POSE = SynchedEntityData.defineId(Bear.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_SLEEPING = SynchedEntityData.defineId(Bear.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_STANDING = SynchedEntityData.defineId(Bear.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_EATING = SynchedEntityData.defineId(Bear.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_BERRIES = SynchedEntityData.defineId(Bear.class, EntityDataSerializers.BOOLEAN);
    private static final UniformInt REMAINS_DEAD_TICKS = TimeUtil.rangeOfSeconds(20, 39);
    private int warningSoundTicks;
    private int standingAnimationTicks;
    private int eatingAnimationTicks;
    private int berriesDropped;
    private int lastPose;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new Bear.BearPanicGoal());
        this.goalSelector.addGoal(1, new Bear.BearMeleeAttackGoal());
        this.goalSelector.addGoal(2, new Bear.BearEatBerriesGoal(1.0D, 12, 4));
        this.goalSelector.addGoal(3, new Bear.BearSleepGoal());
        this.goalSelector.addGoal(4, new Bear.BearBreedGoal(1.0D));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(6, new Bear.BearGoToBerriesGoal(1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isPlayerAggressive));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Animal.class, 10, true, false, (entity) -> {
            return entity.getType().is(BearTags.BEAR_HUNTABLES);
        }));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, (entity) -> {
            return entity.getType().is(BearTags.BEAR_HUNTABLES);
        }));
    }

    public static AttributeSupplier.@org.jetbrains.annotations.NotNull Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ATTACK_SPEED, 1.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new MMPathNavigatorGround(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_LAST_POSE, 0);
        builder.define(DATA_IS_SLEEPING, false);
        builder.define(DATA_IS_STANDING, false);
        builder.define(DATA_IS_EATING, false);
        builder.define(DATA_HAS_BERRIES, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("LastPose", this.entityData.get(DATA_LAST_POSE));
        compound.putBoolean("IsSleeping", this.entityData.get(DATA_IS_SLEEPING));
        compound.putBoolean("IsStanding", this.entityData.get(DATA_IS_STANDING));
        compound.putBoolean("IsEating", this.entityData.get(DATA_IS_EATING));
        compound.putBoolean("HasBerries", this.entityData.get(DATA_HAS_BERRIES));
    }

public Bear(EntityType<? extends Animal> entityType, Level level) {
    super(entityType, level);
    this.setPathfindingMalus(PathType.LEAVES, -1.0F);
    this.setPathfindingMalus(PathType.POWDER_SNOW, -1.0F);
    this.setPathfindingMalus(PathType.UNPASSABLE_RAIL, -1.0F);
    this.moveControl = new Bear.BearMoveControl();
    this.lookControl = new Bear.BearLookControl();
    this.setEating(false);  // Força eating false ao criar
}

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setLastPose(compound.getInt("LastPose"));
        this.setSleeping(compound.getBoolean("IsSleeping"));
        this.setStanding(compound.getBoolean("IsStanding"));
        this.setEating(compound.getBoolean("IsEating"));
        this.setHasBerries(compound.getBoolean("HasBerries"));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (this.isStanding()) {
                this.standingAnimationTicks++;
            } else {
                this.standingAnimationTicks = 0;
            }
            if (this.isEating()) {
                this.eatingAnimationTicks++;
            } else {
                this.eatingAnimationTicks = 0;
            }
        }
        if (this.warningSoundTicks > 0) {
            this.warningSoundTicks = this.warningSoundTicks - 1;
        }
        if (this.isSleeping() && this.isAggressive()) {
            this.setSleeping(false);
        }
        if (this.isAggressive() && this.getTarget() != null) {
            this.setSleeping(false);
            this.setStanding(true);
        } else if (this.isStanding() && this.getTarget() == null) {
            this.setStanding(false);
        }
        if (this.isEating() && this.eatingAnimationTicks > 60) {
            this.setEating(false);
            this.setHasBerries(false);
            this.eatingAnimationTicks = 0;
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        } else {
            this.setSleeping(false);
            if (damageSource.getEntity() instanceof LivingEntity) {
                this.setTarget((LivingEntity)damageSource.getEntity());
            }
            return super.hurt(damageSource, amount);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (!this.level().isClientSide && this.random.nextFloat() < 0.25F) {
            Containers.dropItemStack(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(Items.SWEET_BERRIES, this.random.nextInt(1) + 1));
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.is(Items.SWEET_BERRIES) && !this.isSleeping() && !this.isEating()) {
            this.setEating(true);
            this.setHasBerries(true);
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else if (itemstack.is(Items.HONEYCOMB) && !this.isSleeping() && !this.isEating()) {
            this.heal(5.0F);
            this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.HONEYCOMB_WAX_ON, SoundSource.NEUTRAL, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else if (itemstack.is(Items.HONEY_BOTTLE) && !this.isSleeping() && !this.isEating()) {
            this.heal(10.0F);
            this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.HONEY_DRINK, SoundSource.NEUTRAL, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            return super.mobInteract(player, hand);
        }
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.SWEET_BERRIES);
    }

    public void warningSound() {
        if (this.warningSoundTicks <= 0) {
            this.playSound(BearSoundEvents.BEAR_WARNING.get(), 1.0F, this.getVoicePitch());
            this.warningSoundTicks = 40;
        }
    }

    @Override
    public void playAmbientSound() {
        if (!this.isSleeping()) {
            super.playAmbientSound();
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return BearSoundEvents.BEAR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return BearSoundEvents.BEAR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return BearSoundEvents.BEAR_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 0.15F, 1.0F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate)
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("attack"))
                .triggerableAnim("stand", RawAnimation.begin().thenPlay("stand"))
                .triggerableAnim("eat", RawAnimation.begin().thenPlay("eat")));
        controllers.add(new AnimationController<>(this, "sleepController", 5, this::sleepPredicate));
    }

    private <E extends GeoAnimatable> PlayState predicate(AnimationState<E> event) {
        if (this.isEating()) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("eat"));
        } else if (this.isStanding()) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("stand"));
        } else if (event.isMoving()) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
        } else if (this.isAggressive()) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("aggressive_idle"));
        } else {
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }
    }

    private <E extends GeoAnimatable> PlayState sleepPredicate(AnimationState<E> event) {
        if (this.isSleeping()) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("sleep"));
        } else {
            return PlayState.STOP;
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public int getLastPose() {
        return this.entityData.get(DATA_LAST_POSE);
    }

    @Override
    public void setLastPose(int pose) {
        this.entityData.set(DATA_LAST_POSE, pose);
    }

    @Override
    public boolean isSleeping() {
        return this.entityData.get(DATA_IS_SLEEPING);
    }

    @Override
    public void setSleeping(boolean sleeping) {
        this.entityData.set(DATA_IS_SLEEPING, sleeping);
    }

    public boolean isStanding() {
        return this.entityData.get(DATA_IS_STANDING);
    }

    public void setStanding(boolean standing) {
        this.entityData.set(DATA_IS_STANDING, standing);
    }

    public boolean isEating() {
        return this.entityData.get(DATA_IS_EATING);
    }

    public void setEating(boolean eating) {
        this.entityData.set(DATA_IS_EATING, eating);
    }

    public boolean hasBerries() {
        return this.entityData.get(DATA_HAS_BERRIES);
    }

    public void setHasBerries(boolean hasBerries) {
        this.entityData.set(DATA_HAS_BERRIES, hasBerries);
    }

    public int getBerriesDropped() {
        return this.berriesDropped;
    }

    public void setBerriesDropped(int berriesDropped) {
        this.berriesDropped = berriesDropped;
    }

    private boolean isPlayerAggressive(LivingEntity livingEntity) {
        return !(livingEntity instanceof Player) || !((Player)livingEntity).isCreative();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return BearEntityTypes.BEAR.get().create(level);
    }

    class BearMoveControl extends MoveControl {
        public BearMoveControl() {
            super(Bear.this);
        }

        @Override
        public void tick() {
            if (Bear.this.isSleeping()) {
                this.mob.setDeltaMovement(Vec3.ZERO);
            } else {
                super.tick();
            }
        }
    }

    class BearLookControl extends LookControl {
        public BearLookControl() {
            super(Bear.this);
        }

        @Override
        public void tick() {
            if (Bear.this.isSleeping()) {
                this.mob.setXRot(0.0F);
                this.mob.setYRot(this.mob.getYRot() + 0.2F);
            } else {
                super.tick();
            }
        }
    }

    class BearPanicGoal extends PanicGoal {
        public BearPanicGoal() {
            super(Bear.this, 2.0D);
        }

        @Override
        public boolean canUse() {
            return Bear.this.isBaby() && super.canUse();
        }
    }

    class BearMeleeAttackGoal extends MeleeAttackGoal {
        public BearMeleeAttackGoal() {
            super(Bear.this, 1.25D, true);
        }

// @Override removido (não existe mais no supertipo)
        protected void performAttack(LivingEntity livingEntity) {
            double d0 = this.getAttackReachSqr(livingEntity);
            double distance = this.mob.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
            if (distance <= d0 && this.isTimeToAttack()) {
                this.resetAttackCooldown();
                this.mob.doHurtTarget(livingEntity);
                Bear.this.triggerAnim("controller", "attack");
            }
        }

        @Override
        protected int getAttackInterval() {
            return 20;
        }

// @Override removido (não existe mais no supertipo)
        protected double getAttackReachSqr(LivingEntity livingEntity) {
            return (double)(4.0F + livingEntity.getBbWidth());
        }
    }

    class BearEatBerriesGoal extends Goal {
        private final Bear bear;
        private final double speedModifier;
        private final int searchRange;
        private final int verticalSearchRange;

        public BearEatBerriesGoal(double speedModifier, int searchRange, int verticalSearchRange) {
            this.bear = Bear.this;
            this.speedModifier = speedModifier;
            this.searchRange = searchRange;
            this.verticalSearchRange = verticalSearchRange;
        }

@Override
public boolean canUse() {
    return this.bear.tickCount > 20 && !this.bear.isSleeping() && !this.bear.isEating();
}

        @Override
        public void start() {
            this.bear.setEating(true);
        }

        @Override
        public void stop() {
            this.bear.setEating(false);
        }

        @Override
        public void tick() {
            if (this.bear.getRandom().nextFloat() < 0.1F) {
                this.bear.level().addParticle(ParticleTypes.ITEM_SLIME, 
                    this.bear.getX() + (this.bear.getRandom().nextDouble() - 0.5D) * (double)this.bear.getBbWidth(), 
                    this.bear.getY() + this.bear.getEyeHeight() + 0.5D, 
                    this.bear.getZ() + (this.bear.getRandom().nextDouble() - 0.5D) * (double)this.bear.getBbWidth(), 
                    0.0D, 0.0D, 0.0D);
            }
        }
    }

    class BearSleepGoal extends Goal {
        public BearSleepGoal() {
        }

        @Override
        public boolean canUse() {
            return Bear.this.getTarget() == null && !Bear.this.level().isDay();
        }

        @Override
        public void start() {
            Bear.this.setSleeping(true);
        }

        @Override
        public void stop() {
            Bear.this.setSleeping(false);
        }
    }

    class BearBreedGoal extends BreedGoal {
        public BearBreedGoal(double speedModifier) {
            super(Bear.this, speedModifier);
        }

        @Nullable
// @Override removido (não existe mais no supertipo)
        public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
            return BearEntityTypes.BEAR.get().create(level);
        }

        @Override
        public boolean canUse() {
            return !Bear.this.isSleeping() && super.canUse();
        }
    }

    class BearGoToBerriesGoal extends MoveToBlockGoal {
        private final Bear bear;

        public BearGoToBerriesGoal(double speedModifier) {
            super(Bear.this, speedModifier, 12, 4);
            this.bear = Bear.this;
        }

        @Override
        public boolean canUse() {
            return !this.bear.isSleeping() && !this.bear.isEating() && this.bear.hasBerries() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.bear.isSleeping() && !this.bear.isEating() && this.bear.hasBerries() && super.canContinueToUse();
        }

        @Override
        public void tick() {
            super.tick();
            this.bear.getLookControl().setLookAt(this.blockPos.getX() + 0.5D, this.blockPos.getY() + 1, this.blockPos.getZ() + 0.5D, 10.0F, (float)this.bear.getMaxHeadXRot());
            if (this.isReachedTarget()) {
                this.bear.setEating(true);
                this.bear.setHasBerries(false);
                this.bear.level().broadcastEntityEvent(this.bear, (byte)10);
                this.bear.level().playSound((Player)null, this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ(), SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.NEUTRAL, 1.0F, 1.0F);
                this.bear.setBerriesDropped(this.bear.getBerriesDropped() + 1);
                if (this.bear.getBerriesDropped() >= 3) {
                    this.bear.setBerriesDropped(0);
                    this.stop();
                }
            }
        }

        @Override
        protected boolean isValidTarget(net.minecraft.world.level.LevelReader level, BlockPos pos) {
            return level.getBlockState(pos).getBlock() == net.minecraft.world.level.block.Blocks.SWEET_BERRY_BUSH;
        }
    }
}