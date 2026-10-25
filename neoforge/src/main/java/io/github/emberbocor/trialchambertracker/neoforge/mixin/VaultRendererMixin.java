package io.github.emberbocor.trialchambertracker.neoforge.mixin;

import org.spongepowered.asm.mixin.Mixin;

import io.github.emberbocor.trialchambertracker.render.TrackerLabels;
import net.minecraft.client.renderer.blockentity.VaultRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(VaultRenderer.class)
public abstract class VaultRendererMixin {
    public AABB getRenderBoundingBox(BlockEntity blockEntity) {
        return TrackerLabels.renderBounds(blockEntity.getBlockPos());
    }
}
