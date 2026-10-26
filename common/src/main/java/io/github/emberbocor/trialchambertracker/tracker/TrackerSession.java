package io.github.emberbocor.trialchambertracker.tracker;

import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import io.github.emberbocor.trialchambertracker.TrialChamberTracker;
import io.github.emberbocor.trialchambertracker.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.phys.Vec3;

public final class TrackerSession {
    private static final double NEAR_DISTANCE = 32.0;
    private static final SpawnerTracker SPAWNERS = new SpawnerTracker();
    private static final VaultTracker VAULTS = new VaultTracker();

    @Nullable
    private static Level level;
    @Nullable
    private static String worldId;
    private static TrackerStore store = TrackerStore.inMemory();

    private TrackerSession() {
    }

    public static boolean isTracking(Level current) {
        if (!current.isClientSide() || current.getGameTime() <= 0L) {
            return false;
        }
        if (current != level) {
            switchLevel(current);
        }
        return true;
    }

    public static void observeSpawner(Level level, BlockPos pos, TrialSpawnerState state) {
        if (isTracking(level)) {
            LocalPlayer player = Minecraft.getInstance().player;
            boolean playerNear = player != null && player.distanceToSqr(Vec3.atCenterOf(pos)) <= NEAR_DISTANCE * NEAR_DISTANCE;
            SPAWNERS.observe(store, dimension(level), pos, state, playerNear, level.getGameTime());
        }
    }

    public static void forEachSpawner(Level level, Consumer<BlockPos> action) {
        SPAWNERS.forEachVisible(level.getGameTime(), pos -> action.accept(BlockPos.of(pos)));
    }

    public static void forEachVault(Level level, Consumer<BlockPos> action) {
        VAULTS.forEachVisible(level.getGameTime(), pos -> action.accept(BlockPos.of(pos)));
    }

    public static OptionalLong spawnerCooldownLeft(Level level, BlockPos pos) {
        return isTracking(level) ? SPAWNERS.cooldownLeft(store, dimension(level), pos, level.getGameTime()) : OptionalLong.empty();
    }

    public static void observeVault(Level level, BlockPos pos, VaultState state, Set<UUID> connectedPlayers) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && isTracking(level)) {
            VAULTS.observe(store, dimension(level), pos, state, connectedPlayers, player, level.getGameTime());
        }
    }

    public static boolean isVaultOpened(Level level, BlockPos pos) {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && isTracking(level) && store.isVaultOpened(dimension(level), player.getUUID(), pos);
    }

    private static void switchLevel(Level current) {
        level = current;
        SPAWNERS.clear();
        VAULTS.clear();
        ClientConfig.reload();
        Minecraft minecraft = Minecraft.getInstance();
        Optional<String> id = WorldId.current(minecraft);
        if (id.isEmpty() || !id.get().equals(worldId)) {
            worldId = id.orElse(null);
            store = id.map(name -> TrackerStore.load(storageDirectory(minecraft).resolve(name + ".json")))
                    .orElseGet(TrackerStore::inMemory);
            TrialChamberTracker.LOGGER.debug("Tracking data for {}", id.orElse("an unknown world (not saved)"));
        }
    }

    private static Path storageDirectory(Minecraft minecraft) {
        return minecraft.gameDirectory.toPath().resolve(TrialChamberTracker.MODID);
    }

    private static String dimension(Level level) {
        return level.dimension().location().toString();
    }
}
