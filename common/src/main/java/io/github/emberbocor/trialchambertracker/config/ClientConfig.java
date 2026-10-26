package io.github.emberbocor.trialchambertracker.config;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.emberbocor.trialchambertracker.TrialChamberTracker;

public final class ClientConfig {
    public static final int MIN_RANGE = 1;
    public static final int MAX_RANGE = 64;

    private static final String FILE_NAME = TrialChamberTracker.MODID + "-client.json";
    private static final String SHOW_SPAWNER_TIMERS = "showSpawnerTimers";
    private static final String SHOW_VAULT_MARKERS = "showVaultMarkers";
    private static final String SHOW_THROUGH_BLOCKS = "showThroughBlocks";
    private static final String DISPLAY_RANGE = "displayRange";
    private static final boolean DEFAULT_SHOW_SPAWNER_TIMERS = true;
    private static final boolean DEFAULT_SHOW_VAULT_MARKERS = true;
    private static final boolean DEFAULT_SHOW_THROUGH_BLOCKS = true;
    private static final int DEFAULT_DISPLAY_RANGE = 32;

    private static boolean loaded;
    private static boolean showSpawnerTimers = DEFAULT_SHOW_SPAWNER_TIMERS;
    private static boolean showVaultMarkers = DEFAULT_SHOW_VAULT_MARKERS;
    private static boolean showThroughBlocks = DEFAULT_SHOW_THROUGH_BLOCKS;
    private static int displayRange = DEFAULT_DISPLAY_RANGE;

    private ClientConfig() {
    }

    public static boolean showSpawnerTimers() {
        ensureLoaded();
        return showSpawnerTimers;
    }

    public static boolean showVaultMarkers() {
        ensureLoaded();
        return showVaultMarkers;
    }

    public static boolean showThroughBlocks() {
        ensureLoaded();
        return showThroughBlocks;
    }

    public static int displayRange() {
        ensureLoaded();
        return displayRange;
    }

    public static void reload() {
        loaded = true;
        showSpawnerTimers = DEFAULT_SHOW_SPAWNER_TIMERS;
        showVaultMarkers = DEFAULT_SHOW_VAULT_MARKERS;
        showThroughBlocks = DEFAULT_SHOW_THROUGH_BLOCKS;
        displayRange = DEFAULT_DISPLAY_RANGE;
        Path file = JsonConfigFile.resolve(FILE_NAME);
        if (Files.notExists(file)) {
            save(file);
            return;
        }
        JsonConfigFile.read(file).ifPresent(json -> {
            showSpawnerTimers = readBoolean(json, SHOW_SPAWNER_TIMERS, DEFAULT_SHOW_SPAWNER_TIMERS, file);
            showVaultMarkers = readBoolean(json, SHOW_VAULT_MARKERS, DEFAULT_SHOW_VAULT_MARKERS, file);
            showThroughBlocks = readBoolean(json, SHOW_THROUGH_BLOCKS, DEFAULT_SHOW_THROUGH_BLOCKS, file);
            displayRange = readRange(json, file);
        });
    }

    private static void ensureLoaded() {
        if (!loaded) {
            reload();
        }
    }

    private static boolean readBoolean(JsonObject json, String key, boolean fallback, Path file) {
        JsonElement value = json.get(key);
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
            return value.getAsBoolean();
        }
        TrialChamberTracker.LOGGER.warn("Invalid {} in {} (expected true or false), using {}", key, file, fallback);
        return fallback;
    }

    private static int readRange(JsonObject json, Path file) {
        JsonElement value = json.get(DISPLAY_RANGE);
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
            double range = value.getAsDouble();
            if (range == Math.rint(range) && range >= MIN_RANGE && range <= MAX_RANGE) {
                return (int) range;
            }
        }
        TrialChamberTracker.LOGGER.warn("Invalid {} in {} (expected a whole number from {} to {}), using {}",
                DISPLAY_RANGE, file, MIN_RANGE, MAX_RANGE, DEFAULT_DISPLAY_RANGE);
        return DEFAULT_DISPLAY_RANGE;
    }

    private static void save(Path file) {
        JsonObject json = new JsonObject();
        json.addProperty(SHOW_SPAWNER_TIMERS, showSpawnerTimers);
        json.addProperty(SHOW_VAULT_MARKERS, showVaultMarkers);
        json.addProperty(SHOW_THROUGH_BLOCKS, showThroughBlocks);
        json.addProperty(DISPLAY_RANGE, displayRange);
        JsonConfigFile.write(file, json);
    }
}
