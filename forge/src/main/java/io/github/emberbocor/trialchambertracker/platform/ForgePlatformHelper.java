package io.github.emberbocor.trialchambertracker.platform;

import java.nio.file.Path;

import net.minecraftforge.fml.loading.FMLPaths;

public final class ForgePlatformHelper implements PlatformHelper {
    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
