package io.github.emberbocor.trialchambertracker;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(TrialChamberTracker.MODID)
public class TrialChamberTracker {
    public static final String MODID = "trialchambertracker";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TrialChamberTracker(IEventBus modEventBus, ModContainer modContainer) {
    }
}
