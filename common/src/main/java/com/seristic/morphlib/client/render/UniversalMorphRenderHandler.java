package com.seristic.morphlib.client.render;

import com.seristic.morphlib.MorphData;
import com.seristic.morphlib.MorphManager;
import com.seristic.morphlib.logging.ModLogger;
import com.seristic.morphlib.morph.MorphState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Optional;

/**
 * Universal rendering handler for ALL LivingEntity types.
 * Replaces the old Player-only MorphRenderHandler.
 * 
 * This handler:
 * - Works on Players, Villagers, Zombies, and custom entities
 * - Applies MorphState transformations to any entity model
 * - Injects custom chest geometry dynamically
 * - Handles gender-specific morphing
 */
public class UniversalMorphRenderHandler {

    private static boolean registered = false;

    /**
     * Register the universal morph render handler
     */
    public static void register() {
        if (registered) {
            ModLogger.warn("UniversalMorphRenderHandler", "Already registered!");
            return;
        }

        NeoForge.EVENT_BUS.addListener(UniversalMorphRenderHandler::onRenderLivingPre);
        registered = true;

        ModLogger.info("UniversalMorphRenderHandler", "✅ Registered universal morph rendering system");
    }

    /**
     * Handle pre-render for ALL LivingEntity types
     */
    private static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        EntityModel<?> model = event.getRenderer().getModel();

        if (entity == null || model == null) {
            return;
        }

        // Get morph data for this entity
        MorphData morphData = MorphManager.getEffectiveMorph(entity);
        if (morphData == null) {
            return; // No morph applied
        }

        // Get morph state
        MorphState morphState = morphData.getMorphState();
        if (morphState == null) {
            morphState = new MorphState(); // Default state
        }

        ModLogger.debug("UniversalMorphRenderHandler",
                "🎨 Rendering morph for " + entity.getType().toShortString() +
                        " (model: " + model.getClass().getSimpleName() + ")");

        // Apply universal transformations to the model
        applyMorphToModel(model, morphData, morphState, entity);
    }

    /**
     * Apply morph transformations to any entity model
     */
    private static void applyMorphToModel(EntityModel<?> model, MorphData morphData,
            MorphState morphState, LivingEntity entity) {
        try {
            // Step 1: Apply basic scale transformations
            UniversalModelTransformer.applyTransformations(model, morphState);

            // Step 2: Inject/update chest geometry if needed
            if (ModelPartMapper.supportsChestGeometry(model)) {
                Optional<ModelPart> bodyOpt = ModelPartMapper.getRootPart(model);
                bodyOpt.ifPresent(body -> {
                    ChestGeometryInjector.injectChestGeometry(body, morphData, morphState);

                    // Apply chest animation if entity is moving
                    if (entity.walkAnimation.isMoving()) {
                        ChestGeometryInjector.animateChest(body, morphState, entity.tickCount);
                    }
                });
            }

            ModLogger.debug("UniversalMorphRenderHandler",
                    "✅ Applied morph: height=" + morphState.getHeight() +
                            ", chest=" + morphState.getChestScale() +
                            ", gender=" + morphData.getGender());

        } catch (Exception e) {
            ModLogger.error("UniversalMorphRenderHandler",
                    "Failed to apply morph to " + model.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if an entity has an active morph
     */
    public static boolean hasActiveMorph(LivingEntity entity) {
        return MorphManager.getEffectiveMorph(entity) != null;
    }

    /**
     * Get the morph state for an entity (for external querying)
     */
    public static Optional<MorphState> getMorphState(LivingEntity entity) {
        MorphData morphData = MorphManager.getEffectiveMorph(entity);
        if (morphData == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(morphData.getMorphState());
    }
}
