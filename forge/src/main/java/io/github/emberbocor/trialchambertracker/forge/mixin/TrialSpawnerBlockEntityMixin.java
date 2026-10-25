package io.github.emberbocor.trialchambertracker.forge.mixin;

import org.spongepowered.asm.mixin.Mixin;

import io.github.emberbocor.trialchambertracker.render.TrackerLabels;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(TrialSpawnerBlockEntity.class)
public abstract class TrialSpawnerBlockEntityMixin {
    public AABB getRenderBoundingBox() {
        return TrackerLabels.renderBounds(((TrialSpawnerBlockEntity) (Object) this).getBlockPos());
    }
}
