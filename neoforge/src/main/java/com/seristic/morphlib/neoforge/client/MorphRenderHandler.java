package com.seristic.morphlib.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.seristic.morphlib.Morphlib;
import com.seristic.morphlib.MorphData;
import com.seristic.morphlib.MorphManager;
import com.seristic.logging.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Handles client-side rendering of morphed players.
 * Replaces player model with morph entity model when a morph is active.
 */
public final class MorphRenderHandler {
    public static void register() {
        NeoForge.EVENT_BUS.addListener(MorphRenderHandler::onRenderPlayerPre);
    }

    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        // Try different field access patterns for the player
        Player player = null;

        // Attempt to get player through reflection or direct field access
        try {
            // Check if it's available through a field
            java.lang.reflect.Field playerField = event.getClass().getField("player");
            player = (Player) playerField.get(event);
        } catch (Exception e1) {
            try {
                // Try entity field
                java.lang.reflect.Field entityField = event.getClass().getField("entity");
                player = (Player) entityField.get(event);
            } catch (Exception e2) {
                try {
                    // Try getPlayer method through reflection
                    java.lang.reflect.Method getPlayerMethod = event.getClass().getMethod("getPlayer");
                    player = (Player) getPlayerMethod.invoke(event);
                } catch (Exception e3) {
                    ModLogger.error("MorphRenderHandler",
                            "Could not access player from RenderPlayerEvent.Pre: " + e3.getMessage());
                    return;
                }
            }
        }

        if (player == null) {
            return;
        }

        // Check if player has a morph
        if (!MorphManager.hasMorph(player)) {
            return; // No morph, render normally
        }

        MorphData morphData = MorphManager.getMorph(player);
        if (morphData == null) {
            return; // No morph data, render normally
        }

        try {
            EntityType<?> morphType = morphData.getEntityType();
            if (morphType == EntityType.PLAYER) {
                return; // Don't morph players into players
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return; // No level available
            }

            // Create morph entity for rendering
            LivingEntity morphEntity = (LivingEntity) morphType.create(mc.level, EntitySpawnReason.SPAWNER);
            if (morphEntity == null) {
                ModLogger.error("MorphRenderHandler", "Failed to create morph entity for type: " + morphType);
                return;
            }

            // Copy player position and rotation to morph entity
            morphEntity.setPos(player.getX(), player.getY(), player.getZ());
            morphEntity.setYRot(player.getYRot());
            morphEntity.setXRot(player.getXRot());
            morphEntity.yRotO = player.yRotO;
            morphEntity.xRotO = player.xRotO;

            // Copy pose and animation state
            morphEntity.setPose(player.getPose());
            morphEntity.setOnGround(player.onGround());

            // Cancel default player rendering
            event.setCanceled(true);

            // Render the morph entity instead
            EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource bufferSource = event.getMultiBufferSource();
            int packedLight = event.getPackedLight();

            // Render morph entity at player position
            dispatcher.render(morphEntity, 0.0, 0.0, 0.0, 0.0f,
                    poseStack, bufferSource, packedLight);

            ModLogger.debug("MorphRenderHandler",
                    "Successfully rendered morph for player: " + player.getName().getString());
        } catch (Exception e) {
            ModLogger.error("MorphRenderHandler",
                    "Error rendering morph for player " + player.getName().getString() + ": " + e.getMessage());
            // Don't cancel event if there's an error - let normal rendering proceed
        }
    }
}
