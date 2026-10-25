package io.github.emberbocor.trialchambertracker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.emberbocor.trialchambertracker.render.TrackerLabels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.TrialSpawnerRenderer;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;

@Mixin(TrialSpawnerRenderer.class)
public abstract class TrialSpawnerRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/world/level/block/entity/TrialSpawnerBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"))
    private void trialchambertracker$renderTimer(TrialSpawnerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay, CallbackInfo ci) {
        TrackerLabels.renderSpawner(blockEntity, poseStack, bufferSource);
    }
}
