package io.github.emberbocor.trialchambertracker.platform;

import java.nio.file.Path;

import net.fabricmc.loader.api.FabricLoader;

public final class FabricPlatformHelper implements PlatformHelper {
    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
