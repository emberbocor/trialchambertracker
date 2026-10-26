package io.github.emberbocor.trialchambertracker.render;

import java.util.Locale;
import java.util.OptionalLong;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.emberbocor.trialchambertracker.TrialChamberTracker;
import io.github.emberbocor.trialchambertracker.config.ClientConfig;
import io.github.emberbocor.trialchambertracker.tracker.TrackerSession;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public final class TrackerLabels {
    private static final String SPAWNER_KEY = TrialChamberTracker.MODID + ".spawner.";
    private static final String VAULT_KEY = TrialChamberTracker.MODID + ".vault.";
    private static final int COOLDOWN_COLOR = 0xFFFFFFFF;
    private static final int READY_COLOR = 0xFF55FF55;
    private static final int OMINOUS_READY_COLOR = 0xFF55FFFF;
    private static final int OPENED_COLOR = 0xFFFF5555;
    private static final int SEE_THROUGH_ALPHA = 0x20000000;
    private static final float LABEL_HEIGHT = 1.5F;
    private static final float TEXT_SCALE = 0.025F;

    private TrackerLabels() {
    }

    public static void render(Level level, CameraRenderState camera, SubmitNodeCollector nodeCollector) {
        if (Minecraft.getInstance().gui.hud.isHidden() || !TrackerSession.isTracking(level)) {
            return;
        }
        PoseStack poseStack = new PoseStack();
        if (ClientConfig.showSpawnerTimers()) {
            TrackerSession.forEachSpawner(level, pos -> renderSpawner(level, pos, camera, poseStack, nodeCollector));
        }
        if (ClientConfig.showVaultMarkers()) {
            TrackerSession.forEachVault(level, pos -> renderVault(level, pos, camera, poseStack, nodeCollector));
        }
    }

    private static void renderSpawner(Level level, BlockPos pos, CameraRenderState camera, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(TrialSpawnerBlock.STATE) || state.getValue(TrialSpawnerBlock.STATE) != TrialSpawnerState.COOLDOWN
                || !isInRange(pos, camera)) {
            return;
        }
        String key = SPAWNER_KEY + (state.getValue(TrialSpawnerBlock.OMINOUS) ? "ominous_cooldown" : "cooldown");
        OptionalLong left = TrackerSession.spawnerCooldownLeft(level, pos);
        Component text = left.isPresent()
                ? Component.translatable(key, formatTicks(left.getAsLong()))
                : Component.translatable(key + ".unknown");
        draw(poseStack, nodeCollector, camera, pos, text, COOLDOWN_COLOR);
    }

    private static void renderVault(Level level, BlockPos pos, CameraRenderState camera, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(VaultBlock.STATE) || !isInRange(pos, camera)) {
            return;
        }
        boolean ominous = state.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false);
        boolean opened = TrackerSession.isVaultOpened(level, pos);
        String key = VAULT_KEY + (ominous ? "ominous_" : "") + (opened ? "opened" : "ready");
        int color = opened ? OPENED_COLOR : ominous ? OMINOUS_READY_COLOR : READY_COLOR;
        draw(poseStack, nodeCollector, camera, pos, Component.translatable(key), color);
    }

    private static boolean isInRange(BlockPos pos, CameraRenderState camera) {
        int range = ClientConfig.displayRange();
        return camera.pos.distanceToSqr(Vec3.atCenterOf(pos)) <= (double) range * range;
    }

    private static void draw(PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera, BlockPos pos, Component text, int color) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Vec3 cameraPos = camera.pos;
        poseStack.pushPose();
        poseStack.translate(pos.getX() + 0.5 - cameraPos.x, pos.getY() + LABEL_HEIGHT - cameraPos.y, pos.getZ() + 0.5 - cameraPos.z);
        poseStack.rotate(camera.orientation);
        poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        int background = (int) (minecraft.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        float x = -font.width(text) / 2.0F;
        if (ClientConfig.showThroughBlocks()) {
            nodeCollector.submitText(poseStack, x, 0.0F, text.getVisualOrderText(), false, Font.DisplayMode.SEE_THROUGH,
                    LightCoordsUtil.FULL_BRIGHT, color & 0xFFFFFF | SEE_THROUGH_ALPHA, background, 0);
            nodeCollector.submitText(poseStack, x, 0.0F, text.getVisualOrderText(), false, Font.DisplayMode.NORMAL,
                    LightCoordsUtil.FULL_BRIGHT, color, 0, 0);
        } else {
            nodeCollector.submitText(poseStack, x, 0.0F, text.getVisualOrderText(), false, Font.DisplayMode.NORMAL,
                    LightCoordsUtil.FULL_BRIGHT, color, background, 0);
        }
        poseStack.popPose();
    }

    private static String formatTicks(long ticks) {
        long seconds = (ticks + 19L) / 20L;
        long hours = seconds / 3600L;
        long minutes = seconds / 60L % 60L;
        return hours > 0L
                ? String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds % 60L)
                : String.format(Locale.ROOT, "%d:%02d", minutes, seconds % 60L);
    }
}
