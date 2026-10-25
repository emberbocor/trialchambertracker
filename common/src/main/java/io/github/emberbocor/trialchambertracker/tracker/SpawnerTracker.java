package io.github.emberbocor.trialchambertracker.tracker;

import java.util.OptionalLong;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;

final class SpawnerTracker {
    static final long COOLDOWN_LENGTH = 36000L;
    private static final long SHUTTER_DELAY = 40L;
    private static final long MAX_GAP = 20L;
    private static final long CLOCK_TOLERANCE = 100L;

    private final Long2ObjectMap<Observation> observations = new Long2ObjectOpenHashMap<>();
    private long lastPrune;

    void clear() {
        observations.clear();
    }

    void observe(TrackerStore store, String dimension, BlockPos pos, TrialSpawnerState state, long now) {
        Observation previous = observations.put(pos.asLong(), new Observation(state, now));
        boolean continuous = previous != null && Math.abs(now - previous.seenAt()) <= MAX_GAP;
        if (continuous && previous.state() != state) {
            if (previous.state() == TrialSpawnerState.ACTIVE && state == TrialSpawnerState.WAITING_FOR_REWARD_EJECTION) {
                store.setCooldownEnd(dimension, pos, now + COOLDOWN_LENGTH);
            } else if (previous.state() == TrialSpawnerState.WAITING_FOR_REWARD_EJECTION && state == TrialSpawnerState.EJECTING_REWARD
                    && store.cooldownEnd(dimension, pos).isEmpty()) {
                store.setCooldownEnd(dimension, pos, now - SHUTTER_DELAY + COOLDOWN_LENGTH);
            }
        }
        OptionalLong end = store.cooldownEnd(dimension, pos);
        if (end.isPresent() && (!isCoolingDown(state) || end.getAsLong() - now > COOLDOWN_LENGTH + CLOCK_TOLERANCE)) {
            store.removeCooldown(dimension, pos);
        }
        prune(now);
    }

    OptionalLong cooldownLeft(TrackerStore store, String dimension, BlockPos pos, long now) {
        OptionalLong end = store.cooldownEnd(dimension, pos);
        return end.isPresent() ? OptionalLong.of(Math.max(0L, end.getAsLong() - now)) : OptionalLong.empty();
    }

    private static boolean isCoolingDown(TrialSpawnerState state) {
        return state == TrialSpawnerState.WAITING_FOR_REWARD_EJECTION
                || state == TrialSpawnerState.EJECTING_REWARD
                || state == TrialSpawnerState.COOLDOWN;
    }

    private void prune(long now) {
        if (Math.abs(now - lastPrune) > MAX_GAP * 10) {
            lastPrune = now;
            observations.values().removeIf(observation -> Math.abs(now - observation.seenAt()) > MAX_GAP);
        }
    }

    private record Observation(TrialSpawnerState state, long seenAt) {
    }
}
