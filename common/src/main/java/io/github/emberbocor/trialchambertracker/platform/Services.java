package io.github.emberbocor.trialchambertracker.platform;

import java.util.ServiceLoader;

public final class Services {
    public static final PlatformHelper PLATFORM = load(PlatformHelper.class);

    private Services() {
    }

    private static <T> T load(Class<T> type) {
        return ServiceLoader.load(type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No implementation found for " + type.getName()));
    }
}
