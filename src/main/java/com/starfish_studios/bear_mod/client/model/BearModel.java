package com.starfish_studios.bear_mod.client.model;

import com.starfish_studios.bear_mod.common.entity.Bear;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class BearModel extends GeoModel<Bear> {
    @Override
    public ResourceLocation getModelResource(Bear bear) {
        return ResourceLocation.fromNamespaceAndPath("bear_mod", "geo/entity/bear/bear.geo.json");
    }

@Override
public ResourceLocation getTextureResource(Bear bear) {
    // Define o caminho base da pasta de texturas
    final String basePath = "textures/entity/bear/";

    // Define a textura conforme o estado do urso
    if (bear.isSleeping()) {
        return ResourceLocation.fromNamespaceAndPath("bear_mod", basePath + "bear_sleeping.png");
    } else if (bear.isAggressive()) {
        return ResourceLocation.fromNamespaceAndPath("bear_mod", basePath + "bear_aggressive.png");
    } else if (bear.isEating()) {
        return ResourceLocation.fromNamespaceAndPath("bear_mod", basePath + "bear_eating.png");
    } else if (bear.hasBerries()) {
        return ResourceLocation.fromNamespaceAndPath("bear_mod", basePath + "bear_berries.png");
    }

    // Textura padrão
    return ResourceLocation.fromNamespaceAndPath("bear_mod", basePath + "bear.png");
}

    @Override
    public ResourceLocation getAnimationResource(Bear bear) {
        return ResourceLocation.fromNamespaceAndPath("bear_mod", "animations/entity/bear/bear.animation.json");
    }

	@Override
	public void setCustomAnimations(Bear animatable, long instanceId, AnimationState<Bear> animationState) {
		super.setCustomAnimations(animatable, instanceId, animationState);
    
    // Substitua pela nova forma de obter ossos
		GeoBone head = this.getAnimationProcessor().getBone("head");
    
		if (head != null) {
			EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
			head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
			head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
		}
	}
}

