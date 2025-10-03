package com.seristic.morphlib.client.input;

import com.seristic.morphlib.Morphlib;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Keybinding definitions for MorphLib client-side interactions.
 */
public class MorphKeyBindings {
    public static final String CATEGORY = "key.categories." + Morphlib.MOD_ID;

    public static final KeyMapping OPEN_MORPH_MENU = new KeyMapping(
            "key." + Morphlib.MOD_ID + ".open_morph_menu",
            GLFW.GLFW_KEY_M,
            CATEGORY);

    public static final KeyMapping QUICK_MORPH = new KeyMapping(
            "key." + Morphlib.MOD_ID + ".quick_morph",
            GLFW.GLFW_KEY_B,
            CATEGORY);

    public static final KeyMapping REVERT_MORPH = new KeyMapping(
            "key." + Morphlib.MOD_ID + ".revert_morph",
            GLFW.GLFW_KEY_N,
            CATEGORY);

    // No registration here - that's handled by platform-specific code
}