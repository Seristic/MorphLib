package com.seristic.morphlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Morphlib {
    public static final String MOD_ID = "morphlib";
    public static final Logger LOGGER = LoggerFactory.getLogger("MorphLib");

    public static void init() {
        LOGGER.info("MorphLib Common Module Initialized");
    }
}
