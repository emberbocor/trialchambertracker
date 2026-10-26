package io.github.emberbocor.trialchambertracker.tracker;

import java.util.OptionalLong;
import java.util.function.LongConsumer;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;

final class SpawnerTracker {
    static final long DEFAULT_COOLDOWN_LENGTH = 36000L;
    private static final long SHUTTER_DELAY = 40L;
    private static final long MAX_GAP = 20L;
    private static final long CLOCK_TOLERANCE = 100L;
    private static final long NEAR_BEFORE_FINISH = 40L;
    private static final long MAX_COOLDOWN_LENGTH = 24000L * 365L;

    private final Long2ObjectMap<Observation> observations = new Long2ObjectOpenHashMap<>();
    private long lastPrune;

    void clear() {
        observations.clear();
    }

    void observe(TrackerStore store, String dimension, BlockPos pos, TrialSpawnerState state, boolean playerNear, long now) {
        Observation previous = observations.get(pos.asLong());
        boolean continuous = previous != null && Math.abs(now - previous.seenAt()) <= MAX_GAP;
        long nearSince = !playerNear ? -1L : continuous && previous.nearSince() >= 0L ? previous.nearSince() : now;
        observations.put(pos.asLong(), new Observation(state, now, nearSince));
        OptionalLong start = store.cooldownStart(dimension, pos);
        if (continuous && previous.state() != state) {
            if (previous.state() == TrialSpawnerState.ACTIVE && state == TrialSpawnerState.WAITING_FOR_REWARD_EJECTION) {
                store.setCooldownStart(dimension, pos, now);
            } else if (previous.state() == TrialSpawnerState.WAITING_FOR_REWARD_EJECTION && state == TrialSpawnerState.EJECTING_REWARD
                    && start.isEmpty()) {
                store.setCooldownStart(dimension, pos, now - SHUTTER_DELAY);
            } else if (previous.state() == TrialSpawnerState.COOLDOWN && isFinished(state) && start.isPresent()
                    && previous.nearSince() >= 0L && now - previous.nearSince() >= NEAR_BEFORE_FINISH) {
                learnLength(store, now - start.getAsLong());
            }
        }
        start = store.cooldownStart(dimension, pos);
        if (start.isPresent() && (!isCoolingDown(state) || start.getAsLong() - now > CLOCK_TOLERANCE)) {
            store.removeCooldown(dimension, pos);
        }
        prune(now);
    }

    OptionalLong cooldownLeft(TrackerStore store, String dimension, BlockPos pos, long now) {
        OptionalLong start = store.cooldownStart(dimension, pos);
        if (start.isEmpty()) {
            return OptionalLong.empty();
        }
        long length = store.cooldownLength().orElse(DEFAULT_COOLDOWN_LENGTH);
        return OptionalLong.of(Math.max(0L, start.getAsLong() + length - now));
    }

    void forEachVisible(long now, LongConsumer action) {
        for (Long2ObjectMap.Entry<Observation> entry : observations.long2ObjectEntrySet()) {
            if (Math.abs(now - entry.getValue().seenAt()) <= MAX_GAP) {
                action.accept(entry.getLongKey());
            }
        }
    }

    private static void learnLength(TrackerStore store, long measured) {
        long length = Math.round(measured / 20.0) * 20L;
        if (length > 0L && length <= MAX_COOLDOWN_LENGTH) {
            store.setCooldownLength(length);
        }
    }

    private static boolean isFinished(TrialSpawnerState state) {
        return state == TrialSpawnerState.WAITING_FOR_PLAYERS || state == TrialSpawnerState.INACTIVE;
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

    private record Observation(TrialSpawnerState state, long seenAt, long nearSince) {
    }
}
