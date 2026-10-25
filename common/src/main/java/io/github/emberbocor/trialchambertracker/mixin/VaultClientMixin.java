package io.github.emberbocor.trialchambertracker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.emberbocor.trialchambertracker.tracker.TrackerSession;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultClientData;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(VaultBlockEntity.Client.class)
public abstract class VaultClientMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private static void trialchambertracker$observe(Level level, BlockPos pos, BlockState state, VaultClientData clientData,
            VaultSharedData sharedData, CallbackInfo ci) {
        TrackerSession.observeVault(level, pos, state.getValue(VaultBlock.STATE),
                ((VaultSharedDataAccessor) sharedData).trialchambertracker$getConnectedPlayers());
    }
}
