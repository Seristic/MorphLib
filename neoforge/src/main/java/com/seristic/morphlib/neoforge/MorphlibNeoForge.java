package com.seristic.morphlib.neoforge;

import com.seristic.morphlib.Morphlib;
import com.seristic.morphlib.neoforge.client.MorphRenderHandler;
import com.seristic.logging.ModLogger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Main NeoForge mod class for MorphLib.
 * Handles platform-specific initialization and networking setup.
 */
@Mod(Morphlib.MOD_ID)
public class MorphlibNeoForge {

    public MorphlibNeoForge(IEventBus modEventBus) {
        // Initialize common module
        Morphlib.init();

        // Register networking
        modEventBus.addListener(this::registerNetworking);

        // Register client-side rendering on client setup
        modEventBus.addListener(this::clientSetup);

        ModLogger.info("MorphlibNeoForge", "NeoForge mod initialized");
    }

    private void clientSetup(FMLClientSetupEvent event) {
        // Register morph rendering handler
        MorphRenderHandler.register();
        ModLogger.info("MorphlibNeoForge", "Client setup completed");
    }

    private void registerNetworking(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("morphlib")
                .versioned("1.0.0")
                .optional();

        MorphLibNetworking.register(registrar);
        ModLogger.info("MorphlibNeoForge", "Networking registered");
    }
}
