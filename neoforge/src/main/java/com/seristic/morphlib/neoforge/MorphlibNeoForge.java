package com.seristic.morphlib.neoforge;

import com.seristic.morphlib.Morphlib;
import com.seristic.morphlib.MorphManager;
import com.seristic.morphlib.neoforge.client.MorphRenderHandler;
import com.seristic.morphlib.neoforge.command.MorphCommand;
import com.seristic.morphlib.logging.ModLogger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Main NeoForge mod class for MorphLib.
 * Handles platform-specific initialization and networking setup.
 */
@Mod(Morphlib.MOD_ID)
public class MorphlibNeoForge {

    public MorphlibNeoForge(IEventBus modEventBus) {
        // Initialize common module FIRST (this initializes the logger)
        Morphlib.init();

        ModLogger.info("MorphlibNeoForge", "🚀 CONSTRUCTOR CALLED! MorphlibNeoForge is being instantiated!");

        // Register attachments
        MorphAttachments.ATTACHMENT_TYPES.register(modEventBus);

        // Initialize MorphManager with NeoForge accessors
        MorphManager.setAccessor(new NeoForgeMorphAccessor());
        MorphManager.setStackAccessor(new NeoForgeMorphStackAccessor()); // Register networking
        modEventBus.addListener(this::registerNetworking);

        // Register client-side rendering on client setup
        modEventBus.addListener(this::clientSetup);

        // Register commands
        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        ModLogger.info("MorphlibNeoForge", "✅ NeoForge mod initialization completed successfully!");
    }

    private void clientSetup(FMLClientSetupEvent event) {
        // Register morph rendering handler
        MorphRenderHandler.register();
        ModLogger.info("MorphlibNeoForge", "Client setup completed");
    }

    private void registerNetworking(RegisterPayloadHandlersEvent event) {
        ModLogger.info("MorphlibNeoForge", "🔧 registerNetworking method called!");

        PayloadRegistrar registrar = event.registrar("morphlib")
                .versioned("1.0.0")
                .optional();

        ModLogger.info("MorphlibNeoForge", "📦 PayloadRegistrar created, calling MorphLibNetworking.register...");
        MorphLibNetworking.register(registrar);
        ModLogger.info("MorphlibNeoForge", "✅ Networking registration completed successfully!");
    }

    private void registerCommands(RegisterCommandsEvent event) {
        MorphCommand.register(event.getDispatcher());
        ModLogger.info("MorphlibNeoForge", "Commands registered");
    }
}
