package com.seristic.morphlib.client.render;

import com.seristic.morphlib.MorphData;
import com.seristic.morphlib.logging.ModLogger;
import com.seristic.morphlib.morph.MorphState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;

import java.util.Optional;

/**
 * Handles dynamic injection of chest/breast geometry into entity models.
 * Creates custom ModelParts for entities that don't have chest geometry.
 */
public class ChestGeometryInjector {

    private static final String CHEST_LEFT_NAME = "morphlib_chest_left";
    private static final String CHEST_RIGHT_NAME = "morphlib_chest_right";

    /**
     * Inject chest geometry into a model's body part if it doesn't already have it.
     * 
     * @param bodyPart   The body ModelPart to attach chest to
     * @param morphData  The morph data containing gender/body type
     * @param morphState The morph state with chest scaling parameters
     * @return true if injection was successful or already exists
     */
    public static boolean injectChestGeometry(ModelPart bodyPart, MorphData morphData, MorphState morphState) {
        if (bodyPart == null) {
            ModLogger.warn("ChestGeometryInjector", "Cannot inject chest - body part is null");
            return false;
        }

        // Check if chest parts already exist
        if (hasChestGeometry(bodyPart)) {
            // Update existing chest geometry
            updateChestGeometry(bodyPart, morphData, morphState);
            return true;
        }

        // Create and attach chest geometry
        try {
            createChestParts(bodyPart, morphData, morphState);
            ModLogger.info("ChestGeometryInjector", "Successfully injected chest geometry");
            return true;
        } catch (Exception e) {
            ModLogger.error("ChestGeometryInjector", "Failed to inject chest geometry: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if body part already has chest geometry attached
     */
    public static boolean hasChestGeometry(ModelPart bodyPart) {
        // Check if our custom chest parts exist
        for (var child : bodyPart.children.entrySet()) {
            if (CHEST_LEFT_NAME.equals(child.getKey()) || CHEST_RIGHT_NAME.equals(child.getKey())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create chest ModelParts and attach them to the body
     */
    private static void createChestParts(ModelPart bodyPart, MorphData morphData, MorphState morphState) {
        float chestScale = morphState.getChestScale();
        float chestSpacing = morphState.getChestSpacing();

        // Only create visible chest for female gender with non-zero scale
        boolean isFemale = morphData.getGender() == MorphData.Gender.FEMALE;
        if (!isFemale || chestScale <= 0.01f) {
            // Male or flat chest - no visible geometry needed
            return;
        }

        // Calculate chest dimensions based on scale
        float baseSize = 2.0f; // Base cube size
        float width = baseSize * chestScale;
        float height = baseSize * chestScale;
        float depth = baseSize * chestScale;

        // Calculate position (offset from body center)
        float offsetY = -2.0f; // Below head/neck
        float offsetZ = -2.0f - (depth / 2); // Forward from body
        float lateralOffset = 2.0f + chestSpacing; // Left/right spacing

        // Create left chest part
        ModelPart chestLeft = new ModelPart(
                CubeListBuilder.create()
                        .texOffs(16, 20) // Texture UV offset
                        .addBox(-width / 2, -height / 2, -depth / 2, width, height, depth, new CubeDeformation(0.0f))
                        .compile().cubes(),
                bodyPart.getAllChildren());
        chestLeft.setPos(lateralOffset, offsetY, offsetZ);

        // Create right chest part
        ModelPart chestRight = new ModelPart(
                CubeListBuilder.create()
                        .texOffs(16, 20) // Texture UV offset (mirror)
                        .addBox(-width / 2, -height / 2, -depth / 2, width, height, depth, new CubeDeformation(0.0f))
                        .compile().cubes(),
                bodyPart.getAllChildren());
        chestRight.setPos(-lateralOffset, offsetY, offsetZ);

        // Add to body part's children
        bodyPart.children.put(CHEST_LEFT_NAME, chestLeft);
        bodyPart.children.put(CHEST_RIGHT_NAME, chestRight);

        ModLogger.debug("ChestGeometryInjector",
                "Created chest parts: scale=" + chestScale + ", spacing=" + chestSpacing +
                        ", dimensions=" + width + "x" + height + "x" + depth);
    }

    /**
     * Update existing chest geometry with new scale values
     */
    public static void updateChestGeometry(ModelPart bodyPart, MorphData morphData, MorphState morphState) {
        ModelPart chestLeft = bodyPart.children.get(CHEST_LEFT_NAME);
        ModelPart chestRight = bodyPart.children.get(CHEST_RIGHT_NAME);

        if (chestLeft == null || chestRight == null) {
            return;
        }

        float chestScale = morphState.getChestScale();
        float chestSpacing = morphState.getChestSpacing();

        // Update scale
        chestLeft.xScale = chestScale;
        chestLeft.yScale = chestScale;
        chestLeft.zScale = chestScale;

        chestRight.xScale = chestScale;
        chestRight.yScale = chestScale;
        chestRight.zScale = chestScale;

        // Update position based on spacing
        float lateralOffset = 2.0f + chestSpacing;
        chestLeft.x = lateralOffset;
        chestRight.x = -lateralOffset;

        // Hide chest for male or flat
        boolean isFemale = morphData.getGender() == MorphData.Gender.FEMALE;
        boolean visible = isFemale && chestScale > 0.01f;

        chestLeft.visible = visible;
        chestRight.visible = visible;

        ModLogger.debug("ChestGeometryInjector",
                "Updated chest geometry: scale=" + chestScale + ", spacing=" + chestSpacing + ", visible=" + visible);
    }

    /**
     * Remove chest geometry from a model
     */
    public static void removeChestGeometry(ModelPart bodyPart) {
        if (bodyPart == null) {
            return;
        }

        bodyPart.children.remove(CHEST_LEFT_NAME);
        bodyPart.children.remove(CHEST_RIGHT_NAME);

        ModLogger.debug("ChestGeometryInjector", "Removed chest geometry");
    }

    /**
     * Apply animation to chest parts (bounce, sway)
     */
    public static void animateChest(ModelPart bodyPart, MorphState morphState, float ageInTicks) {
        ModelPart chestLeft = bodyPart.children.get(CHEST_LEFT_NAME);
        ModelPart chestRight = bodyPart.children.get(CHEST_RIGHT_NAME);

        if (chestLeft == null || chestRight == null) {
            return;
        }

        float bounce = morphState.getIdleBounce();
        float sway = morphState.getWalkSway();

        if (bounce > 0.001f) {
            // Gentle idle bounce
            float bounceAmount = (float) Math.sin(ageInTicks * 0.05f) * 0.05f * bounce;
            chestLeft.y += bounceAmount;
            chestRight.y += bounceAmount;
        }

        if (sway > 0.001f) {
            // Subtle sway during movement
            float swayAmount = (float) Math.sin(ageInTicks * 0.1f) * 0.03f * sway;
            chestLeft.zRot = swayAmount;
            chestRight.zRot = -swayAmount;
        }
    }
}
