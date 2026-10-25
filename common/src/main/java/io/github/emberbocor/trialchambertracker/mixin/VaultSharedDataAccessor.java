package io.github.emberbocor.trialchambertracker.mixin;

import java.util.Set;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.entity.vault.VaultSharedData;

@Mixin(VaultSharedData.class)
public interface VaultSharedDataAccessor {
    @Accessor("connectedPlayers")
    Set<UUID> trialchambertracker$getConnectedPlayers();
}
