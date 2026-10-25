package io.github.emberbocor.trialchambertracker.forge.mixin;

import org.spongepowered.asm.mixin.Mixin;

import io.github.emberbocor.trialchambertracker.render.TrackerLabels;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(VaultBlockEntity.class)
public abstract class VaultBlockEntityMixin {
    public AABB getRenderBoundingBox() {
        return TrackerLabels.renderBounds(((VaultBlockEntity) (Object) this).getBlockPos());
    }
}
