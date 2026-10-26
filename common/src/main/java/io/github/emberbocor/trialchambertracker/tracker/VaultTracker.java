package io.github.emberbocor.trialchambertracker.tracker;

import java.util.Set;
import java.util.UUID;
import java.util.function.LongConsumer;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
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
    private final Long2LongMap seen = new Long2LongOpenHashMap();
    private long lastPrune;

    void clear() {
        visits.clear();
        seen.clear();
    }

    void observe(TrackerStore store, String dimension, BlockPos pos, VaultState state, Set<UUID> connectedPlayers, Player player, long now) {
        seen.put(pos.asLong(), now);
        prune(now);
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

    void forEachVisible(long now, LongConsumer action) {
        for (Long2LongMap.Entry entry : seen.long2LongEntrySet()) {
            if (Math.abs(now - entry.getLongValue()) <= MAX_GAP) {
                action.accept(entry.getLongKey());
            }
        }
    }

    private void prune(long now) {
        if (Math.abs(now - lastPrune) > MAX_GAP * 10) {
            lastPrune = now;
            seen.values().removeIf((long seenAt) -> Math.abs(now - seenAt) > MAX_GAP);
        }
    }

    private record Visit(long since, long seenAt) {
    }
}
