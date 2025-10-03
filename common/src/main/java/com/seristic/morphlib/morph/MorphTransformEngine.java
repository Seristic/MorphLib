package com.seristic.morphlib.morph;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.model.geom.ModelPart;

/**
 * Applies morph transformations to ModelPart instances.
 * This is where the magic happens - transforming vanilla cubes based on morph
 * parameters.
 */
public class MorphTransformEngine {

    /**
     * Apply morph transformations to a player model.
     * 
     * @param morphState The morph state containing transformation parameters
     * @param body       The body ModelPart
     * @param head       The head ModelPart
     * @param leftArm    The left arm ModelPart
     * @param rightArm   The right arm ModelPart
     * @param leftLeg    The left leg ModelPart
     * @param rightLeg   The right leg ModelPart
     * @param ageInTicks Current age in ticks for animation
     */
    public static void applyPlayerTransforms(MorphState morphState,
            ModelPart body, ModelPart head,
            ModelPart leftArm, ModelPart rightArm,
            ModelPart leftLeg, ModelPart rightLeg,
            float ageInTicks) {

        // Apply head transformations
        if (head != null) {
            head.xScale = morphState.getHeadSize();
            head.yScale = morphState.getHeadSize();
            head.zScale = morphState.getHeadSize();
        }

        // Apply body transformations
        if (body != null) {
            body.xScale = morphState.getBodyWidth();
            body.yScale = morphState.getHeight();
            body.zScale = morphState.getBodyWidth();

            // Add subtle idle animation
            float bounce = (float) Math.sin(ageInTicks * 0.1f) * morphState.getIdleBounce();
            body.y += bounce;
        }

        // Apply arm transformations
        float armScale = morphState.getArmLength();
        if (leftArm != null) {
            leftArm.yScale = armScale;
            leftArm.x = -5.0f * morphState.getShoulderWidth(); // Adjust shoulder position
        }
        if (rightArm != null) {
            rightArm.yScale = armScale;
            rightArm.x = 5.0f * morphState.getShoulderWidth(); // Adjust shoulder position
        }

        // Apply leg transformations
        float legScale = morphState.getLegLength();
        if (leftLeg != null) {
            leftLeg.yScale = legScale;
            leftLeg.x = -1.9f * morphState.getHipWidth(); // Adjust hip position
        }
        if (rightLeg != null) {
            rightLeg.yScale = legScale;
            rightLeg.x = 1.9f * morphState.getHipWidth(); // Adjust hip position
        }
    }

    /**
     * Apply specialized transformations for chest/torso area.
     * This creates additional ModelPart cubes for more detailed body morphing.
     * 
     * @param morphState  The morph state
     * @param body        The main body part
     * @param customParts Map of custom body parts (chest, hips, etc.)
     * @param ageInTicks  Current age for animation
     */
    public static void applyDetailedBodyTransforms(MorphState morphState,
            ModelPart body,
            Map<String, ModelPart> customParts,
            float ageInTicks) {

        // Apply chest transformations if custom chest parts exist
        ModelPart chestLeft = customParts.get("chest_left");
        ModelPart chestRight = customParts.get("chest_right");

        if (chestLeft != null && chestRight != null) {
            float chestScale = morphState.getChestScale();
            float chestSpacing = morphState.getChestSpacing();

            // Scale chest parts
            chestLeft.xScale = chestScale;
            chestLeft.yScale = chestScale;
            chestLeft.zScale = chestScale;

            chestRight.xScale = chestScale;
            chestRight.yScale = chestScale;
            chestRight.zScale = chestScale;

            // Position chest parts with spacing
            chestLeft.x = -1.5f - chestSpacing;
            chestRight.x = 1.5f + chestSpacing;

            // Add subtle bounce animation
            float bounce = (float) Math.sin(ageInTicks * 0.1f) * morphState.getIdleBounce() * 0.5f;
            chestLeft.y += bounce;
            chestRight.y += bounce;

            // Make chest parts visible
            chestLeft.visible = chestScale > 0.1f;
            chestRight.visible = chestScale > 0.1f;
        }

        // Apply hip transformations
        ModelPart hips = customParts.get("hips");
        if (hips != null) {
            hips.xScale = morphState.getHipWidth();
            hips.yScale = 1.0f;
            hips.zScale = morphState.getHipWidth();
            hips.visible = morphState.getHipWidth() > 1.05f; // Only show if significantly wider
        }

        // Apply neck transformations
        ModelPart neck = customParts.get("neck");
        if (neck != null) {
            neck.yScale = morphState.getNeckLength();
            neck.visible = morphState.getNeckLength() > 1.1f; // Only show if elongated
        }
    }

    /**
     * Apply transformations to armor layers to match body morph.
     * 
     * @param morphState The morph state
     * @param armorParts Map of armor ModelParts
     */
    public static void applyArmorTransforms(MorphState morphState, Map<String, ModelPart> armorParts) {
        // Scale armor to match body proportions
        for (Map.Entry<String, ModelPart> entry : armorParts.entrySet()) {
            String partName = entry.getKey();
            ModelPart armorPart = entry.getValue();

            if (armorPart == null)
                continue;

            switch (partName) {
                case "helmet":
                    armorPart.xScale = morphState.getHeadSize();
                    armorPart.yScale = morphState.getHeadSize();
                    armorPart.zScale = morphState.getHeadSize();
                    break;

                case "chestplate":
                    armorPart.xScale = morphState.getBodyWidth();
                    armorPart.yScale = morphState.getHeight();
                    armorPart.zScale = morphState.getBodyWidth();
                    break;

                case "leggings":
                    armorPart.xScale = morphState.getHipWidth();
                    armorPart.yScale = morphState.getLegLength();
                    armorPart.zScale = morphState.getHipWidth();
                    break;

                case "boots":
                    armorPart.yScale = morphState.getLegLength();
                    break;
            }
        }
    }

    /**
     * Reset all transformations to default values.
     * Call this before applying new transforms to ensure clean state.
     */
    public static void resetTransforms(ModelPart... parts) {
        for (ModelPart part : parts) {
            if (part != null) {
                part.xScale = 1.0f;
                part.yScale = 1.0f;
                part.zScale = 1.0f;
                part.x = part.x; // Keep original position
                part.y = part.y; // Keep original position
                part.z = part.z; // Keep original position
                part.visible = true;
            }
        }
    }

    /**
     * Create anchor transforms for attaching accessories or clothing.
     * 
     * @param morphState The current morph state
     * @return Map of anchor points with their transform data
     */
    public static Map<String, AnchorTransform> getAnchorTransforms(MorphState morphState) {
        Map<String, AnchorTransform> anchors = new HashMap<>();

        // Chest anchor points
        anchors.put("chest_left", new AnchorTransform(
                -1.5f - morphState.getChestSpacing(),
                10.5f,
                -2.0f,
                morphState.getChestScale()));

        anchors.put("chest_right", new AnchorTransform(
                1.5f + morphState.getChestSpacing(),
                10.5f,
                -2.0f,
                morphState.getChestScale()));

        // Shoulder anchor points
        anchors.put("shoulder_left", new AnchorTransform(
                -5.0f * morphState.getShoulderWidth(),
                12.0f,
                0.0f,
                1.0f));

        anchors.put("shoulder_right", new AnchorTransform(
                5.0f * morphState.getShoulderWidth(),
                12.0f,
                0.0f,
                1.0f));

        // Hip anchor points
        anchors.put("hip_left", new AnchorTransform(
                -1.9f * morphState.getHipWidth(),
                0.0f,
                0.0f,
                morphState.getHipWidth()));

        anchors.put("hip_right", new AnchorTransform(
                1.9f * morphState.getHipWidth(),
                0.0f,
                0.0f,
                morphState.getHipWidth()));

        return anchors;
    }

    /**
     * Data class for anchor transform information.
     */
    public static class AnchorTransform {
        public final float x, y, z, scale;

        public AnchorTransform(float x, float y, float z, float scale) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.scale = scale;
        }
    }
}