package com.seristic.morphlib.client.render;

import com.seristic.morphlib.logging.ModLogger;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Utility for discovering and mapping model parts across different entity model
 * types.
 * Handles the variety of bone names and structures across Minecraft's entity
 * models.
 */
public class ModelPartMapper {

    /**
     * Standard bone mappings for common model parts
     */
    public enum BodyPart {
        HEAD,
        BODY,
        LEFT_ARM,
        RIGHT_ARM,
        LEFT_LEG,
        RIGHT_LEG,
        HAT,
        JACKET,
        LEFT_SLEEVE,
        RIGHT_SLEEVE,
        LEFT_PANTS,
        RIGHT_PANTS
    }

    /**
     * Find a specific body part in any entity model.
     * 
     * @param model The entity model to search
     * @param part  The body part to find
     * @return Optional ModelPart if found
     */
    public static Optional<ModelPart> findPart(EntityModel<?> model, BodyPart part) {
        // Try HumanoidModel first (most common)
        if (model instanceof HumanoidModel<?> humanoidModel) {
            return findHumanoidPart(humanoidModel, part);
        }

        // Try reflection-based search for other models
        return findPartByReflection(model, part);
    }

    /**
     * Find part in HumanoidModel (Players, Zombies, Skeletons, etc.)
     */
    private static Optional<ModelPart> findHumanoidPart(HumanoidModel<?> model, BodyPart part) {
        return switch (part) {
            case HEAD -> Optional.of(model.head);
            case BODY -> Optional.of(model.body);
            case LEFT_ARM -> Optional.of(model.leftArm);
            case RIGHT_ARM -> Optional.of(model.rightArm);
            case LEFT_LEG -> Optional.of(model.leftLeg);
            case RIGHT_LEG -> Optional.of(model.rightLeg);
            case HAT -> Optional.of(model.hat);
            default -> Optional.empty();
        };
    }

    /**
     * Find part using reflection (for non-standard models)
     */
    private static Optional<ModelPart> findPartByReflection(EntityModel<?> model, BodyPart part) {
        String[] possibleNames = getPossibleFieldNames(part);

        for (String fieldName : possibleNames) {
            try {
                Field field = findFieldRecursive(model.getClass(), fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (value instanceof ModelPart modelPart) {
                        ModLogger.debug("ModelPartMapper",
                                "Found " + part + " as '" + fieldName + "' in " + model.getClass().getSimpleName());
                        return Optional.of(modelPart);
                    }
                }
            } catch (Exception e) {
                // Continue searching
            }
        }

        ModLogger.debug("ModelPartMapper",
                "Could not find " + part + " in " + model.getClass().getSimpleName());
        return Optional.empty();
    }

    /**
     * Get possible field names for a body part
     */
    private static String[] getPossibleFieldNames(BodyPart part) {
        return switch (part) {
            case HEAD -> new String[] { "head", "headModel", "headPart" };
            case BODY -> new String[] { "body", "bodyModel", "bodyPart", "torso" };
            case LEFT_ARM -> new String[] { "leftArm", "leftArmModel", "armLeft" };
            case RIGHT_ARM -> new String[] { "rightArm", "rightArmModel", "armRight" };
            case LEFT_LEG -> new String[] { "leftLeg", "leftLegModel", "legLeft" };
            case RIGHT_LEG -> new String[] { "rightLeg", "rightLegModel", "legRight" };
            case HAT -> new String[] { "hat", "hatModel" };
            case JACKET -> new String[] { "jacket", "jacketModel" };
            case LEFT_SLEEVE -> new String[] { "leftSleeve", "leftSleeveModel" };
            case RIGHT_SLEEVE -> new String[] { "rightSleeve", "rightSleeveModel" };
            case LEFT_PANTS -> new String[] { "leftPants", "leftPantsModel" };
            case RIGHT_PANTS -> new String[] { "rightPants", "rightPantsModel" };
        };
    }

    /**
     * Find field recursively through class hierarchy
     */
    private static Field findFieldRecursive(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findFieldRecursive(clazz.getSuperclass(), fieldName);
            }
            return null;
        }
    }

    /**
     * Get all ModelParts from a model (for debugging/inspection)
     */
    public static Map<String, ModelPart> getAllParts(EntityModel<?> model) {
        Map<String, ModelPart> parts = new HashMap<>();

        Class<?> clazz = model.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (ModelPart.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(model);
                        if (value instanceof ModelPart modelPart) {
                            parts.put(field.getName(), modelPart);
                        }
                    } catch (Exception e) {
                        // Skip this field
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        return parts;
    }

    /**
     * Check if a model has a specific part
     */
    public static boolean hasPart(EntityModel<?> model, BodyPart part) {
        return findPart(model, part).isPresent();
    }

    /**
     * Check if model supports chest geometry (PlayerModel does, most others don't)
     */
    public static boolean supportsChestGeometry(EntityModel<?> model) {
        // PlayerModel has body part that can be extended
        if (model instanceof PlayerModel) {
            return true;
        }

        // HumanoidModel has body part
        if (model instanceof HumanoidModel) {
            return true;
        }

        return false;
    }

    /**
     * Get the root/body part for attaching custom geometry
     */
    public static Optional<ModelPart> getRootPart(EntityModel<?> model) {
        // Try body first
        Optional<ModelPart> body = findPart(model, BodyPart.BODY);
        if (body.isPresent()) {
            return body;
        }

        // Try head as fallback
        return findPart(model, BodyPart.HEAD);
    }
}
