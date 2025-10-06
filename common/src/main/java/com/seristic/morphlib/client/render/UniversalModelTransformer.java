package com.seristic.morphlib.client.render;

import com.seristic.morphlib.logging.ModLogger;
import com.seristic.morphlib.morph.MorphState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

import java.util.Optional;

/**
 * Applies MorphState transformations to any entity model.
 * Works across different model types (PlayerModel, VillagerModel, ZombieModel,
 * etc.)
 */
public class UniversalModelTransformer {

    /**
     * Apply all MorphState transformations to an entity model.
     * 
     * @param model      The entity model to transform
     * @param morphState The morph state with transformation parameters
     */
    public static void applyTransformations(EntityModel<?> model, MorphState morphState) {
        if (model == null || morphState == null) {
            return;
        }

        // Apply scale transformations to model parts
        applyHeadScale(model, morphState);
        applyBodyScale(model, morphState);
        applyArmScale(model, morphState);
        applyLegScale(model, morphState);
        applyShoulderWidth(model, morphState);
        applyHipWidth(model, morphState);

        ModLogger.debug("UniversalModelTransformer",
                "Applied transformations to " + model.getClass().getSimpleName() +
                        ": height=" + morphState.getHeight() + ", bodyWidth=" + morphState.getBodyWidth());
    }

    /**
     * Apply head size scaling
     */
    private static void applyHeadScale(EntityModel<?> model, MorphState morphState) {
        Optional<ModelPart> headOpt = ModelPartMapper.findPart(model, ModelPartMapper.BodyPart.HEAD);
        if (headOpt.isEmpty()) {
            return;
        }

        ModelPart head = headOpt.get();
        float scale = morphState.getHeadSize();

        head.xScale = scale;
        head.yScale = scale;
        head.zScale = scale;
    }

    /**
     * Apply body scaling (height and width)
     */
    private static void applyBodyScale(EntityModel<?> model, MorphState morphState) {
        Optional<ModelPart> bodyOpt = ModelPartMapper.findPart(model, ModelPartMapper.BodyPart.BODY);
        if (bodyOpt.isEmpty()) {
            return;
        }

        ModelPart body = bodyOpt.get();
        float heightScale = morphState.getHeight();
        float widthScale = morphState.getBodyWidth();

        body.xScale = widthScale;
        body.yScale = heightScale;
        body.zScale = widthScale;
    }

    /**
     * Apply arm length scaling
     */
    private static void applyArmScale(EntityModel<?> model, MorphState morphState) {
        float armScale = morphState.getArmLength();

        // Left arm
        ModelPartMapper.findPart(model, ModelPartMapper.BodyPart.LEFT_ARM).ifPresent(leftArm -> {
            leftArm.yScale = armScale;
        });

        // Right arm
        ModelPartMapper.findPart(model, ModelPartMapper.BodyPart.RIGHT_ARM).ifPresent(rightArm -> {
            rightArm.yScale = armScale;
        });
    }

    /**
     * Apply leg length scaling
     */
    private static void applyLegScale(EntityModel<?> model, MorphState morphState) {
        float legScale = morphState.getLegLength();

        // Left leg
        ModelPartMapper.findPart(model, ModelPartMapper.BodyPart.LEFT_LEG).ifPresent(leftLeg -> {
            leftLeg.yScale = legScale;
        });

        // Right leg
        ModelPartMapper.findPart(model, ModelPartMapper.BodyPart.RIGHT_LEG).ifPresent(rightLeg -> {
            rightLeg.yScale = legScale;
        });
    }

    /**
     * Apply shoulder width by adjusting arm positions
     */
    private static void applyShoulderWidth(EntityModel<?> model, MorphState morphState) {
        float shoulderWidth = morphState.getShoulderWidth();

        // Adjust arm positions for shoulder width
        ModelPartMapper.findPart(model, ModelPartMapper.BodyPart.LEFT_ARM).ifPresent(leftArm -> {
            // Move arm outward based on shoulder width
            leftArm.x = leftArm.x * shoulderWidth;
        });

        ModelPartMapper.findPart(model, ModelPartMapper.BodyPart.RIGHT_ARM).ifPresent(rightArm -> {
            // Move arm outward based on shoulder width
            rightArm.x = rightArm.x * shoulderWidth;
        });
    }

    /**
     * Apply hip width by adjusting leg positions
     */
    private static void applyHipWidth(EntityModel<?> model, MorphState morphState) {
        float hipWidth = morphState.getHipWidth();

        // Adjust leg positions for hip width
        ModelPartMapper.findPart(model, ModelPartMapper.BodyPart.LEFT_LEG).ifPresent(leftLeg -> {
            // Move leg outward based on hip width
            leftLeg.x = leftLeg.x * hipWidth;
        });

        ModelPartMapper.findPart(model, ModelPartMapper.BodyPart.RIGHT_LEG).ifPresent(rightLeg -> {
            // Move leg outward based on hip width
            rightLeg.x = rightLeg.x * hipWidth;
        });
    }

    /**
     * Reset all transformations to default (scale=1.0, default positions)
     */
    public static void resetTransformations(EntityModel<?> model) {
        if (model == null) {
            return;
        }

        // Reset all parts to default scale
        for (var part : ModelPartMapper.BodyPart.values()) {
            ModelPartMapper.findPart(model, part).ifPresent(modelPart -> {
                modelPart.xScale = 1.0f;
                modelPart.yScale = 1.0f;
                modelPart.zScale = 1.0f;
            });
        }

        ModLogger.debug("UniversalModelTransformer",
                "Reset transformations for " + model.getClass().getSimpleName());
    }

    /**
     * Apply transformations with smooth interpolation
     */
    public static void applyTransformationsSmooth(EntityModel<?> model, MorphState from, MorphState to,
            float progress) {
        if (model == null || from == null || to == null) {
            return;
        }

        // Interpolate between states
        MorphState interpolated = MorphState.lerp(from, to, progress);
        applyTransformations(model, interpolated);
    }
}
