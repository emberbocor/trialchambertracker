package io.github.emberbocor.trialchambertracker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.emberbocor.trialchambertracker.render.TrackerLabels;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.VaultRenderer;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;

@Mixin(VaultRenderer.class)
public abstract class VaultRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/world/level/block/entity/vault/VaultBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"))
    private void trialchambertracker$renderMarker(VaultBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay, CallbackInfo ci) {
        TrackerLabels.renderVault(blockEntity, poseStack, bufferSource);
    }
}
