package io.github.emberbocor.trialchambertracker.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.emberbocor.trialchambertracker.TrialChamberTracker;
import io.github.emberbocor.trialchambertracker.platform.Services;

public final class JsonConfigFile {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private JsonConfigFile() {
    }

    static Path resolve(String fileName) {
        return Services.PLATFORM.getConfigDirectory().resolve(fileName);
    }

    public static Optional<JsonObject> read(Path file) {
        try (Reader reader = Files.newBufferedReader(file)) {
            return Optional.of(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (IOException | RuntimeException e) {
            TrialChamberTracker.LOGGER.warn("Could not read {}, using defaults: {}", file, e.getMessage());
            return Optional.empty();
        }
    }

    public static void write(Path file, JsonObject json) {
        try {
            Files.createDirectories(file.getParent());
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp)) {
                GSON.toJson(json, writer);
            }
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            TrialChamberTracker.LOGGER.warn("Could not write {}: {}", file, e.getMessage());
        }
    }
}
