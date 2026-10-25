package io.github.emberbocor.trialchambertracker.tracker;

import java.util.Set;
import java.util.UUID;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.vault.VaultState;

final class VaultTracker {
    private static final double CHECK_RANGE = 3.5;
    private static final long SETTLE_TICKS = 50L;
    private static final long MAX_GAP = 20L;

    private final Long2ObjectMap<Visit> visits = new Long2ObjectOpenHashMap<>();

    void clear() {
        visits.clear();
    }

    void observe(TrackerStore store, String dimension, BlockPos pos, VaultState state, Set<UUID> connectedPlayers, Player player, long now) {
        UUID id = player.getUUID();
        if (connectedPlayers.contains(id)) {
            visits.remove(pos.asLong());
            store.setVaultOpened(dimension, id, pos, false);
            return;
        }
        boolean waiting = state == VaultState.INACTIVE || state == VaultState.ACTIVE;
        if (!waiting || !player.isAlive() || player.isSpectator() || !player.blockPosition().closerThan(pos, CHECK_RANGE)) {
            visits.remove(pos.asLong());
            return;
        }
        Visit previous = visits.get(pos.asLong());
        long since = previous != null && Math.abs(now - previous.seenAt()) <= MAX_GAP ? previous.since() : now;
        visits.put(pos.asLong(), new Visit(since, now));
        if (now - since >= SETTLE_TICKS) {
            store.setVaultOpened(dimension, id, pos, true);
        }
    }

    private record Visit(long since, long seenAt) {
    }
}
