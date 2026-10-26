package io.github.emberbocor.trialchambertracker.tracker;

import java.util.Set;
import java.util.UUID;
import java.util.function.LongConsumer;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.vault.VaultState;

final class VaultTracker {
    private static final double CHECK_RANGE = 3.5;
    private static final double LIST_RANGE = 4.5;
    private static final long SETTLE_TICKS = 50L;
    private static final long UNLOCK_WINDOW = 2L;
    private static final long MAX_GAP = 20L;
    private static final long NONE = -1L;

    private final Long2ObjectMap<Watch> watches = new Long2ObjectOpenHashMap<>();
    private long lastPrune;

    void clear() {
        watches.clear();
    }

    void observe(TrackerStore store, String dimension, BlockPos pos, VaultState state, Set<UUID> connectedPlayers, Player player, long now) {
        UUID id = player.getUUID();
        boolean listed = connectedPlayers.contains(id);
        Watch previous = watches.get(pos.asLong());
        boolean continuous = previous != null && Math.abs(now - previous.seenAt()) <= MAX_GAP;
        long leftListAt = listed || !continuous ? NONE : previous.listed() ? now : previous.leftListAt();
        long unlockedAt = !isUnlocking(state) || !continuous ? NONE
                : previous.state() == VaultState.ACTIVE ? now : previous.unlockedAt();
        boolean waiting = state == VaultState.INACTIVE || state == VaultState.ACTIVE;
        boolean near = player.isAlive() && !player.isSpectator() && player.blockPosition().closerThan(pos, CHECK_RANGE);
        long nearSince = listed || !waiting || !near ? NONE : continuous && previous.nearSince() != NONE ? previous.nearSince() : now;
        watches.put(pos.asLong(), new Watch(now, state, listed, leftListAt, unlockedAt, nearSince));
        prune(now);
        if (listed) {
            store.setVaultOpened(dimension, id, pos, false);
        } else if (leftListAt != NONE && unlockedAt != NONE && Math.abs(leftListAt - unlockedAt) <= UNLOCK_WINDOW && player.isAlive()
                && !player.isSpectator() && player.blockPosition().closerThan(pos, LIST_RANGE)) {
            store.setVaultOpened(dimension, id, pos, true);
        } else if (nearSince != NONE && now - nearSince >= SETTLE_TICKS) {
            store.setVaultOpened(dimension, id, pos, true);
        }
    }

    void forEachVisible(long now, LongConsumer action) {
        for (Long2ObjectMap.Entry<Watch> entry : watches.long2ObjectEntrySet()) {
            if (Math.abs(now - entry.getValue().seenAt()) <= MAX_GAP) {
                action.accept(entry.getLongKey());
            }
        }
    }

    private static boolean isUnlocking(VaultState state) {
        return state == VaultState.UNLOCKING || state == VaultState.EJECTING;
    }

    private void prune(long now) {
        if (Math.abs(now - lastPrune) > MAX_GAP * 10) {
            lastPrune = now;
            watches.values().removeIf(watch -> Math.abs(now - watch.seenAt()) > MAX_GAP);
        }
    }

    private record Watch(long seenAt, VaultState state, boolean listed, long leftListAt, long unlockedAt, long nearSince) {
    }
}
