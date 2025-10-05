package com.seristic.morphlib.neoforge.client;

import com.seristic.morphlib.MorphData;
import com.seristic.morphlib.MorphManager;
import com.seristic.morphlib.logging.ModLogger;
import com.seristic.morphlib.morph.MorphCache;
import com.seristic.morphlib.morph.MorphState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles client-side rendering of morphed players.
 * Updated for Minecraft 1.21.4 PlayerRenderState system.
 */
public final class MorphRenderHandler {

    private static boolean fieldDiscoveryLogged = false;

    public static void register() {
        NeoForge.EVENT_BUS.addListener(MorphRenderHandler::onRenderPlayerPre);
    }

    /**
     * Handle pre-render logic - work with PlayerRenderState instead of Player
     * entity.
     */
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        // In MC 1.21.4, we work with PlayerRenderState, not Player entity directly
        PlayerRenderState renderState = null;
        Player player = null;

        try {
            // DEBUG: Let's see what fields are actually available (only log once)
            if (!fieldDiscoveryLogged) {
                Class<?> eventClass = event.getClass();

                ModLogger.debug("MorphRenderHandler", "=== Available fields in " + eventClass.getSimpleName() + " ===");
                Field[] allFields = eventClass.getDeclaredFields();
                for (Field field : allFields) {
                    ModLogger.debug("MorphRenderHandler",
                            "Field: " + field.getName() + " (Type: " + field.getType().getSimpleName() + ")");
                }

                // Also check superclass fields
                Class<?> currentClass = eventClass.getSuperclass();
                while (currentClass != null && currentClass != Object.class) {
                    Field[] superFields = currentClass.getDeclaredFields();
                    if (superFields.length > 0) {
                        ModLogger.debug("MorphRenderHandler",
                                "=== Superclass " + currentClass.getSimpleName() + " fields ===");
                        for (Field field : superFields) {
                            ModLogger.debug("MorphRenderHandler", "Superfield: " + field.getName() + " (Type: "
                                    + field.getType().getSimpleName() + ")");
                        }
                    }
                    currentClass = currentClass.getSuperclass();
                }
                fieldDiscoveryLogged = true;
            }

            // Try multiple approaches to find the renderState field
            Field renderStateField = null;
            String[] possibleFieldNames = { "renderState", "playerRenderState", "state", "player" };

            for (String fieldName : possibleFieldNames) {
                // Try direct field access
                try {
                    renderStateField = event.getClass().getDeclaredField(fieldName);
                    if (!fieldDiscoveryLogged) {
                        ModLogger.debug("MorphRenderHandler", "✅ Found field '" + fieldName + "' in event class");
                    }
                    break;
                } catch (NoSuchFieldException e) {
                    // Continue to next field name
                }

                // Try superclass search
                Class<?> searchClass = event.getClass();
                while (searchClass != null && searchClass != Object.class) {
                    try {
                        renderStateField = searchClass.getDeclaredField(fieldName);
                        if (!fieldDiscoveryLogged) {
                            ModLogger.debug("MorphRenderHandler",
                                    "✅ Found field '" + fieldName + "' in superclass: " + searchClass.getSimpleName());
                        }
                        break;
                    } catch (NoSuchFieldException e) {
                        // Continue to next class
                    }
                    searchClass = searchClass.getSuperclass();
                }

                if (renderStateField != null)
                    break;
            }

            if (renderStateField == null) {
                if (!fieldDiscoveryLogged) {
                    ModLogger.debug("MorphRenderHandler", "❌ Could not find any render state field in class hierarchy");
                }
                return;
            }

            renderStateField.setAccessible(true);
            Object stateValue = renderStateField.get(event);

            if (stateValue instanceof PlayerRenderState) {
                renderState = (PlayerRenderState) stateValue;
                if (!fieldDiscoveryLogged) {
                    ModLogger.debug("MorphRenderHandler", "✅ Successfully accessed PlayerRenderState");
                }

                // Try to get the actual player from the client
                player = getPlayerFromRenderState(renderState);

            } else {
                if (!fieldDiscoveryLogged) {
                    ModLogger.debug("MorphRenderHandler", "❌ renderState is not PlayerRenderState: " +
                            (stateValue != null ? stateValue.getClass().getSimpleName() : "null"));
                }
                return;
            }
        } catch (Exception e) {
            if (!fieldDiscoveryLogged) {
                ModLogger.debug("MorphRenderHandler", "❌ Failed to access renderState: " + e.getMessage());
            }
            return;
        }

        // If we couldn't get the player directly, we'll work with just the render state
        if (player == null) {
            if (!fieldDiscoveryLogged) {
                ModLogger.debug("MorphRenderHandler", "⚠️ No player entity found, working with render state only");
            }
            // For now, we'll apply morph transforms directly to the render state
            applyMorphToRenderState(renderState);
            return;
        }

        // Get current morph data
        MorphData morphData = MorphManager.getEffectiveMorph(player);
        if (morphData == null || morphData.getEntityType() == EntityType.PLAYER) {
            return; // No morph or player morph, render normally
        }

        ModLogger.info("MorphRenderHandler",
                "🎯 MORPH DETECTED for " + morphData.getEntityType().toShortString() +
                        " to player: " + player.getName().getString());

        // Get morph state from the morph data directly
        MorphState morphState = morphData.getMorphState();
        if (morphState == null) {
            morphState = new MorphState(); // Fallback to default
        }

        // Update cache with the morph state for interpolation
        MorphCache.getInstance().updateMorphState(player.getUUID(), morphState);

        // Get interpolated state for smooth transitions
        MorphState interpolatedState = MorphCache.getInstance().getInterpolatedState(
                player.getUUID(), 0.0f);

        ModLogger.debug("MorphRenderHandler", "Using morph state: height=" + interpolatedState.getHeight() +
                ", bodyWidth=" + interpolatedState.getBodyWidth());

        // Apply morph transforms to the render state
        applyMorphToRenderState(renderState, interpolatedState, morphData);

        // DON'T cancel the event - this was causing invisible players
        // event.setCanceled(true); // <-- This was the problem!

        ModLogger.debug("MorphRenderHandler",
                "✅ Applied morph transforms to render state");
    }

    /**
     * Try to get the Player entity from the render state.
     * This is a fallback method since the new rendering system doesn't always
     * provide direct access.
     */
    private static Player getPlayerFromRenderState(PlayerRenderState renderState) {
        try {
            // Try to get the client player if this is a first-person render
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                // For now, assume it's the client player
                // In a multiplayer context, we'd need a more sophisticated approach
                return mc.player;
            }
        } catch (Exception e) {
            ModLogger.debug("MorphRenderHandler", "Could not get player from render state: " + e.getMessage());
        }
        return null;
    }

    /**
     * Apply morph transforms directly to the render state when we don't have a
     * player entity.
     */
    private static void applyMorphToRenderState(PlayerRenderState renderState) {
        ModLogger.debug("MorphRenderHandler", "Applying default morph transforms to render state");
        // For now, just log that we're working with the render state
        // TODO: Implement render state transformation logic
    }

    /**
     * Apply morph transforms to the render state based on morph data.
     */
    private static void applyMorphToRenderState(PlayerRenderState renderState, MorphState morphState,
            MorphData morphData) {
        ModLogger.debug("MorphRenderHandler", "Applying morph-specific transforms to render state");

        try {
            // Apply actual render state transformations based on morph data
            // This involves modifying the render state properties to match the morphed
            // entity

            // Apply scale transformations
            float heightScale = morphState.getHeight();
            float widthScale = morphState.getBodyWidth();

            // Use reflection to modify PlayerRenderState properties
            applyScaleToRenderState(renderState, widthScale, heightScale);

            // For cow morph, we need specific transformations
            if (morphData.getEntityType() == EntityType.COW) {
                applyCowTransforms(renderState, morphState);
            }

            ModLogger.debug("MorphRenderHandler",
                    "Applied transforms: height=" + heightScale +
                            ", bodyWidth=" + widthScale +
                            ", entityType=" + morphData.getEntityType().toShortString());

        } catch (Exception e) {
            ModLogger.debug("MorphRenderHandler", "Failed to apply render state transforms: " + e.getMessage());

            // Fallback: log what would be applied
            ModLogger.debug("MorphRenderHandler",
                    "Would apply transforms: height=" + morphState.getHeight() +
                            ", bodyWidth=" + morphState.getBodyWidth() +
                            ", entityType=" + morphData.getEntityType().toShortString());
        }
    }

    /**
     * Apply scale transformations to the render state using reflection.
     */
    private static void applyScaleToRenderState(PlayerRenderState renderState, float widthScale, float heightScale) {
        try {
            // Try to find and modify scale-related fields in PlayerRenderState
            Class<?> renderStateClass = renderState.getClass();

            // Enhanced field discovery for PlayerRenderState
            ModLogger.debug("MorphRenderHandler", "=== Detailed PlayerRenderState Analysis ===");
            ModLogger.debug("MorphRenderHandler", "PlayerRenderState class: " + renderStateClass.getName());

            Field[] allFields = renderStateClass.getDeclaredFields();
            ModLogger.debug("MorphRenderHandler", "Total fields found: " + allFields.length);

            for (Field field : allFields) {
                field.setAccessible(true);
                String fieldInfo = "Field: " + field.getName() +
                        " (Type: " + field.getType().getSimpleName() +
                        ", Modifiers: " + field.getModifiers() + ")";
                ModLogger.debug("MorphRenderHandler", fieldInfo);

                // Try to read current value for debug
                try {
                    Object value = field.get(renderState);
                    ModLogger.debug("MorphRenderHandler", "  Current value: " + value);
                } catch (Exception e) {
                    ModLogger.debug("MorphRenderHandler", "  Cannot read value: " + e.getMessage());
                }
            }

            // Also check superclasses for inherited fields
            Class<?> currentClass = renderStateClass.getSuperclass();
            while (currentClass != null && currentClass != Object.class) {
                Field[] superFields = currentClass.getDeclaredFields();
                if (superFields.length > 0) {
                    ModLogger.debug("MorphRenderHandler",
                            "=== Superclass " + currentClass.getSimpleName() + " fields ===");
                    for (Field field : superFields) {
                        field.setAccessible(true);
                        ModLogger.debug("MorphRenderHandler", "Inherited field: " + field.getName() + " (Type: "
                                + field.getType().getSimpleName() + ")");
                    }
                }
                currentClass = currentClass.getSuperclass();
            }

            // Look for ANY float fields that might control size/scale
            ModLogger.debug("MorphRenderHandler", "=== Attempting to modify visual transformation fields ===");
            boolean modifiedAnyField = false;

            // Collect all fields including inherited ones
            List<Field> allFieldsList = new ArrayList<>();
            Collections.addAll(allFieldsList, allFields);

            // Add superclass fields
            Class<?> superClass = renderStateClass.getSuperclass();
            while (superClass != null && superClass != Object.class) {
                Field[] superFields = superClass.getDeclaredFields();
                Collections.addAll(allFieldsList, superFields);
                superClass = superClass.getSuperclass();
            }

            for (Field field : allFieldsList) {
                field.setAccessible(true);

                if (field.getType() == float.class || field.getType() == Float.class) {
                    try {
                        float originalValue = field.getFloat(renderState);
                        String fieldName = field.getName();

                        // Target specific fields that control visual appearance
                        if ("scale".equals(fieldName)) {
                            float newValue = originalValue * widthScale;
                            field.setFloat(renderState, newValue);
                            ModLogger.debug("MorphRenderHandler",
                                    "🎯 Modified SCALE: " + originalValue + " → " + newValue);
                            modifiedAnyField = true;
                        } else if ("ageScale".equals(fieldName)) {
                            float newValue = originalValue * heightScale;
                            field.setFloat(renderState, newValue);
                            ModLogger.debug("MorphRenderHandler",
                                    "🎯 Modified AGE_SCALE: " + originalValue + " → " + newValue);
                            modifiedAnyField = true;
                        } else if ("boundingBoxWidth".equals(fieldName)) {
                            float newValue = originalValue * widthScale;
                            field.setFloat(renderState, newValue);
                            ModLogger.debug("MorphRenderHandler",
                                    "🎯 Modified BOUNDING_BOX_WIDTH: " + originalValue + " → " + newValue);
                            modifiedAnyField = true;
                        } else if ("boundingBoxHeight".equals(fieldName)) {
                            float newValue = originalValue * heightScale;
                            field.setFloat(renderState, newValue);
                            ModLogger.debug("MorphRenderHandler",
                                    "🎯 Modified BOUNDING_BOX_HEIGHT: " + originalValue + " → " + newValue);
                            modifiedAnyField = true;
                        } else if ("eyeHeight".equals(fieldName)) {
                            float newValue = originalValue * heightScale;
                            field.setFloat(renderState, newValue);
                            ModLogger.debug("MorphRenderHandler",
                                    "🎯 Modified EYE_HEIGHT: " + originalValue + " → " + newValue);
                            modifiedAnyField = true;
                        }
                        // Also try generic scale/size/width/height pattern matching as fallback
                        else if (fieldName.toLowerCase().contains("scale") ||
                                fieldName.toLowerCase().contains("size") ||
                                fieldName.toLowerCase().contains("width") ||
                                fieldName.toLowerCase().contains("height")) {

                            float newValue = originalValue;
                            if (fieldName.toLowerCase().contains("height")) {
                                newValue = originalValue * heightScale;
                            } else {
                                newValue = originalValue * widthScale;
                            }

                            field.setFloat(renderState, newValue);
                            ModLogger.debug("MorphRenderHandler",
                                    "✅ Pattern-matched " + fieldName + ": " + originalValue + " → " + newValue);
                            modifiedAnyField = true;
                        } else {
                            ModLogger.debug("MorphRenderHandler",
                                    "Skipped " + fieldName + " (value: " + originalValue + ")");
                        }

                    } catch (Exception e) {
                        ModLogger.debug("MorphRenderHandler",
                                "Failed to modify " + field.getName() + ": " + e.getMessage());
                    }
                }
            }

            if (!modifiedAnyField) {
                ModLogger.debug("MorphRenderHandler", "⚠️ No suitable fields found to modify");
            }

        } catch (Exception e) {
            ModLogger.debug("MorphRenderHandler", "Could not apply scale transforms: " + e.getMessage());
        }
    }

    /**
     * Apply cow-specific transformations to the render state.
     */
    private static void applyCowTransforms(PlayerRenderState renderState, MorphState morphState) {
        try {
            // Cow-specific transformations
            ModLogger.debug("MorphRenderHandler", "🐄 Applying cow-specific transformations");

            Class<?> renderStateClass = renderState.getClass();
            Field[] allFields = renderStateClass.getDeclaredFields();

            // Try to find and modify position/pose fields for cow stance
            for (Field field : allFields) {
                field.setAccessible(true);
                String fieldName = field.getName().toLowerCase();

                try {
                    // Look for positional fields that might control stance
                    if (field.getType() == float.class || field.getType() == Float.class) {
                        float currentValue = field.getFloat(renderState);

                        if (fieldName.contains("y") || fieldName.contains("height") || fieldName.contains("pos")) {
                            // Lower cow stance
                            float newValue = currentValue - 0.3f; // Lower by 0.3 blocks
                            field.setFloat(renderState, newValue);
                            ModLogger.debug("MorphRenderHandler",
                                    "🐄 Lowered cow " + field.getName() + ": " + currentValue + " → " + newValue);
                        } else if (fieldName.contains("scale") && !fieldName.contains("height")) {
                            // Make cow wider
                            float newValue = currentValue * 1.4f;
                            field.setFloat(renderState, newValue);
                            ModLogger.debug("MorphRenderHandler",
                                    "🐄 Widened cow " + field.getName() + ": " + currentValue + " → " + newValue);
                        }
                    }

                } catch (Exception e) {
                    // Continue if we can't modify this field
                }
            }

        } catch (Exception e) {
            ModLogger.debug("MorphRenderHandler", "Could not apply cow transforms: " + e.getMessage());
        }
    }

    /**
     * Generate a morph state based on entity type.
     */
    private static MorphState generateMorphStateFromEntityType(EntityType<?> entityType) {
        MorphState state = new MorphState();
        String entityName = entityType.toString();
        String entityShortName = entityType.toShortString();

        ModLogger.debug("MorphRenderHandler",
                "Generating morph state for entity: '" + entityName + "' (short: '" + entityShortName + "')");

        // Set proportions based on entity type - try both full name and short name
        String typeToMatch = entityShortName; // Use short name for comparison
        switch (typeToMatch) {
            case "cow":
                state.setBodyWidth(1.4f);
                state.setHeight(1.2f);
                state.setChestScale(1.5f);
                state.setHipWidth(1.3f);
                ModLogger.debug("MorphRenderHandler", "✅ Applied cow proportions: height=" + state.getHeight()
                        + ", bodyWidth=" + state.getBodyWidth());
                break;

            case "pig":
                state.setBodyWidth(1.2f);
                state.setHeight(0.8f);
                state.setChestScale(1.2f);
                state.setLegLength(0.7f);
                break;

            case "chicken":
                state.setBodyWidth(0.6f);
                state.setHeight(0.7f);
                state.setChestScale(0.8f);
                state.setLegLength(0.6f);
                state.setNeckLength(1.4f);
                break;

            case "villager":
                // Default villager proportions
                state.setHeight(1.0f);
                state.setBodyWidth(1.0f);
                break;

            default:
                // Default human proportions
                ModLogger.debug("MorphRenderHandler", "⚠️ Using default proportions for unmatched entity: '"
                        + typeToMatch + "' (full: '" + entityName + "')");
                break;
        }

        ModLogger.debug("MorphRenderHandler",
                "Final generated state: height=" + state.getHeight() + ", bodyWidth=" + state.getBodyWidth());
        return state;
    }
}