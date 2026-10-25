package io.github.emberbocor.trialchambertracker.tracker;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;

final class WorldId {
    private static final int MAX_NAME_LENGTH = 64;

    private WorldId() {
    }

    static Optional<String> current(Minecraft minecraft) {
        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server != null) {
            Path folder = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().getFileName();
            return folder == null ? Optional.empty() : Optional.of(of("singleplayer", folder.toString()));
        }
        ServerData serverData = minecraft.getCurrentServer();
        if (serverData != null && !serverData.ip.isBlank()) {
            return Optional.of(of("server", serverData.ip.trim().toLowerCase(Locale.ROOT)));
        }
        return Optional.empty();
    }

    private static String of(String kind, String name) {
        String safe = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
        if (safe.length() > MAX_NAME_LENGTH) {
            safe = safe.substring(0, MAX_NAME_LENGTH);
        }
        return kind + "-" + safe + "-" + String.format(Locale.ROOT, "%08x", name.hashCode());
    }
}
