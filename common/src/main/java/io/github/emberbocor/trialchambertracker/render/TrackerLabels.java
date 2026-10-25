package io.github.emberbocor.trialchambertracker.render;

import java.util.Locale;
import java.util.OptionalLong;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.emberbocor.trialchambertracker.TrialChamberTracker;
import io.github.emberbocor.trialchambertracker.config.ClientConfig;
import io.github.emberbocor.trialchambertracker.tracker.TrackerSession;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public final class TrackerLabels {
    private static final String SPAWNER_KEY = TrialChamberTracker.MODID + ".spawner.";
    private static final String VAULT_KEY = TrialChamberTracker.MODID + ".vault.";
    private static final int COOLDOWN_COLOR = 0xFFFFAA00;
    private static final int OMINOUS_COOLDOWN_COLOR = 0xFF55FFFF;
    private static final int NOT_OPENED_COLOR = 0xFF55FF55;
    private static final int OMINOUS_NOT_OPENED_COLOR = 0xFF55FFFF;
    private static final int OPENED_COLOR = 0xFFAAAAAA;
    private static final float LABEL_HEIGHT = 1.5F;
    private static final float TEXT_SCALE = 0.025F;

    private TrackerLabels() {
    }

    public static void renderSpawner(TrialSpawnerBlockEntity spawner, PoseStack poseStack, MultiBufferSource bufferSource) {
        Level level = spawner.getLevel();
        if (level == null || !TrackerSession.isTracking(level) || !ClientConfig.showSpawnerTimers()
                || spawner.getState() != TrialSpawnerState.COOLDOWN || !isVisible(spawner.getBlockPos())) {
            return;
        }
        boolean ominous = spawner.getBlockState().getOptionalValue(BlockStateProperties.OMINOUS).orElse(false);
        String key = SPAWNER_KEY + (ominous ? "ominous_cooldown" : "cooldown");
        OptionalLong left = TrackerSession.spawnerCooldownLeft(level, spawner.getBlockPos());
        Component text = left.isPresent()
                ? Component.translatable(key, formatTicks(left.getAsLong()))
                : Component.translatable(key + ".unknown");
        draw(poseStack, bufferSource, text, ominous ? OMINOUS_COOLDOWN_COLOR : COOLDOWN_COLOR);
    }

    public static void renderVault(VaultBlockEntity vault, PoseStack poseStack, MultiBufferSource bufferSource) {
        Level level = vault.getLevel();
        if (level == null || !TrackerSession.isTracking(level) || !ClientConfig.showVaultMarkers() || !isVisible(vault.getBlockPos())) {
            return;
        }
        boolean ominous = vault.getBlockState().getOptionalValue(BlockStateProperties.OMINOUS).orElse(false);
        boolean opened = TrackerSession.isVaultOpened(level, vault.getBlockPos());
        String key = VAULT_KEY + (ominous ? "ominous_" : "") + (opened ? "opened" : "not_opened");
        int color = opened ? OPENED_COLOR : ominous ? OMINOUS_NOT_OPENED_COLOR : NOT_OPENED_COLOR;
        draw(poseStack, bufferSource, Component.translatable(key), color);
    }

    public static AABB renderBounds(BlockPos pos) {
        return new AABB(pos.getX() - 1.0, pos.getY() - 1.0, pos.getZ() - 1.0, pos.getX() + 2.0, pos.getY() + 2.0, pos.getZ() + 2.0);
    }

    private static boolean isVisible(BlockPos pos) {
        int range = ClientConfig.displayRange();
        return Minecraft.renderNames()
                && Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                        <= (double) range * range;
    }

    private static void draw(PoseStack poseStack, MultiBufferSource bufferSource, Component text, int color) {
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        Font font = minecraft.font;
        poseStack.pushPose();
        poseStack.translate(0.5F, LABEL_HEIGHT, 0.5F);
        poseStack.mulPose(dispatcher.cameraOrientation());
        poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        int background = (int) (minecraft.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        float x = -font.width(text) / 2.0F;
        font.drawInBatch(text, x, 0.0F, color, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, background,
                LightTexture.FULL_BRIGHT);
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
