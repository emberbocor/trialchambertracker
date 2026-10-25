package io.github.emberbocor.trialchambertracker.platform;

import java.nio.file.Path;

import net.neoforged.fml.loading.FMLPaths;

public final class NeoForgePlatformHelper implements PlatformHelper {
    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
