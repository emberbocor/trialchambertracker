package io.github.emberbocor.trialchambertracker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.emberbocor.trialchambertracker.tracker.TrackerSession;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;

@Mixin(TrialSpawner.class)
public abstract class TrialSpawnerMixin {
    @Inject(method = "tickClient", at = @At("HEAD"))
    private void trialchambertracker$observe(Level level, BlockPos pos, boolean isOminous, CallbackInfo ci) {
        TrackerSession.observeSpawner(level, pos, ((TrialSpawner) (Object) this).getState());
    }
}
