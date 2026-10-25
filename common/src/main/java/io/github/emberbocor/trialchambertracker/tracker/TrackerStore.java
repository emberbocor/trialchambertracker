package io.github.emberbocor.trialchambertracker.tracker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.emberbocor.trialchambertracker.TrialChamberTracker;
import io.github.emberbocor.trialchambertracker.config.JsonConfigFile;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;

final class TrackerStore {
    private static final String DIMENSIONS = "dimensions";
    private static final String COOLDOWNS = "cooldowns";
    private static final String OPENED_VAULTS = "openedVaults";
    private static final String POS = "pos";
    private static final String ENDS_AT = "endsAt";

    @Nullable
    private final Path file;
    private final Map<String, Long2LongMap> cooldowns = new HashMap<>();
    private final Map<String, Map<UUID, LongSet>> openedVaults = new HashMap<>();

    private TrackerStore(@Nullable Path file) {
        this.file = file;
    }

    static TrackerStore inMemory() {
        return new TrackerStore(null);
    }

    static TrackerStore load(Path file) {
        TrackerStore store = new TrackerStore(file);
        if (Files.exists(file)) {
            JsonConfigFile.read(file).ifPresent(json -> {
                try {
                    store.read(json);
                } catch (RuntimeException e) {
                    TrialChamberTracker.LOGGER.warn("Ignoring invalid data in {}: {}", file, e.getMessage());
                    store.cooldowns.clear();
                    store.openedVaults.clear();
                }
            });
        }
        return store;
    }

    OptionalLong cooldownEnd(String dimension, BlockPos pos) {
        Long2LongMap ends = cooldowns.get(dimension);
        return ends != null && ends.containsKey(pos.asLong()) ? OptionalLong.of(ends.get(pos.asLong())) : OptionalLong.empty();
    }

    void setCooldownEnd(String dimension, BlockPos pos, long endsAt) {
        Long2LongMap ends = cooldowns.computeIfAbsent(dimension, key -> new Long2LongOpenHashMap());
        if (!ends.containsKey(pos.asLong()) || ends.get(pos.asLong()) != endsAt) {
            ends.put(pos.asLong(), endsAt);
            save();
        }
    }

    void removeCooldown(String dimension, BlockPos pos) {
        Long2LongMap ends = cooldowns.get(dimension);
        if (ends != null && ends.containsKey(pos.asLong())) {
            ends.remove(pos.asLong());
            save();
        }
    }

    boolean isVaultOpened(String dimension, UUID player, BlockPos pos) {
        LongSet vaults = openedVaults.getOrDefault(dimension, Map.of()).get(player);
        return vaults != null && vaults.contains(pos.asLong());
    }

    void setVaultOpened(String dimension, UUID player, BlockPos pos, boolean opened) {
        if (isVaultOpened(dimension, player, pos) == opened) {
            return;
        }
        LongSet vaults = openedVaults.computeIfAbsent(dimension, key -> new HashMap<>())
                .computeIfAbsent(player, key -> new LongOpenHashSet());
        if (opened) {
            vaults.add(pos.asLong());
        } else {
            vaults.remove(pos.asLong());
        }
        save();
    }

    private void read(JsonObject json) {
        JsonObject dimensions = json.getAsJsonObject(DIMENSIONS);
        if (dimensions == null) {
            return;
        }
        for (Map.Entry<String, JsonElement> dimension : dimensions.entrySet()) {
            JsonObject data = dimension.getValue().getAsJsonObject();
            JsonArray spawners = data.getAsJsonArray(COOLDOWNS);
            if (spawners != null) {
                Long2LongMap ends = cooldowns.computeIfAbsent(dimension.getKey(), key -> new Long2LongOpenHashMap());
                for (JsonElement element : spawners) {
                    JsonObject spawner = element.getAsJsonObject();
                    ends.put(readPos(spawner.getAsJsonArray(POS)), spawner.get(ENDS_AT).getAsLong());
                }
            }
            JsonObject vaults = data.getAsJsonObject(OPENED_VAULTS);
            if (vaults != null) {
                Map<UUID, LongSet> players = openedVaults.computeIfAbsent(dimension.getKey(), key -> new HashMap<>());
                for (Map.Entry<String, JsonElement> player : vaults.entrySet()) {
                    LongSet positions = players.computeIfAbsent(UUID.fromString(player.getKey()), key -> new LongOpenHashSet());
                    for (JsonElement pos : player.getValue().getAsJsonArray()) {
                        positions.add(readPos(pos.getAsJsonArray()));
                    }
                }
            }
        }
    }

    private void save() {
        if (file == null) {
            return;
        }
        JsonObject dimensions = new JsonObject();
        cooldowns.forEach((dimension, ends) -> {
            if (ends.isEmpty()) {
                return;
            }
            JsonArray spawners = new JsonArray();
            for (Long2LongMap.Entry entry : ends.long2LongEntrySet()) {
                JsonObject spawner = new JsonObject();
                spawner.add(POS, writePos(entry.getLongKey()));
                spawner.addProperty(ENDS_AT, entry.getLongValue());
                spawners.add(spawner);
            }
            dimensionData(dimensions, dimension).add(COOLDOWNS, spawners);
        });
        openedVaults.forEach((dimension, players) -> {
            JsonObject vaults = new JsonObject();
            players.forEach((player, positions) -> {
                if (!positions.isEmpty()) {
                    JsonArray list = new JsonArray();
                    positions.forEach(pos -> list.add(writePos(pos)));
                    vaults.add(player.toString(), list);
                }
            });
            if (!vaults.isEmpty()) {
                dimensionData(dimensions, dimension).add(OPENED_VAULTS, vaults);
            }
        });
        JsonObject json = new JsonObject();
        json.add(DIMENSIONS, dimensions);
        JsonConfigFile.write(file, json);
    }

    private static JsonObject dimensionData(JsonObject dimensions, String dimension) {
        JsonObject data = dimensions.getAsJsonObject(dimension);
        if (data == null) {
            data = new JsonObject();
            dimensions.add(dimension, data);
        }
        return data;
    }

    private static long readPos(JsonArray pos) {
        return BlockPos.asLong(pos.get(0).getAsInt(), pos.get(1).getAsInt(), pos.get(2).getAsInt());
    }

    private static JsonArray writePos(long packed) {
        JsonArray pos = new JsonArray();
        pos.add(BlockPos.getX(packed));
        pos.add(BlockPos.getY(packed));
        pos.add(BlockPos.getZ(packed));
        return pos;
    }
}
