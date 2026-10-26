package io.github.emberbocor.trialchambertracker.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.emberbocor.trialchambertracker.render.TrackerLabels;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.LevelRenderState;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    @Nullable
    private ClientLevel level;

    @Inject(method = "submitBlockEntities", at = @At("TAIL"))
    private void trialchambertracker$renderLabels(PoseStack poseStack, LevelRenderState renderState, SubmitNodeStorage nodeStorage, CallbackInfo ci) {
        if (level != null) {
            TrackerLabels.render(level, renderState.cameraRenderState, nodeStorage);
        }
    }
}
