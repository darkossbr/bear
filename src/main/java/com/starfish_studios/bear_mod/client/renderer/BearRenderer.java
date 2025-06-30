package com.starfish_studios.bear_mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starfish_studios.bear_mod.client.model.BearModel;
import com.starfish_studios.bear_mod.common.entity.Bear;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BearRenderer extends GeoEntityRenderer<Bear> {
    public BearRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BearModel());
        this.shadowRadius = 0.9F;
    }

    @Override
    public RenderType getRenderType(Bear animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    @Override
    public void render(Bear entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity.isSleeping()) {
            poseStack.translate(0.0F, 0.0F, 0.0F);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}

