package com.seristic.morphlib.neoforge.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.seristic.morphlib.MorphData;
import com.seristic.morphlib.MorphManager;
import com.seristic.morphlib.MorphStack;
import com.seristic.morphlib.logging.ModLogger;
import com.seristic.morphlib.neoforge.MorphLibNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

/**
 * Commands for testing and using the morph system.
 * Provides /morph and /unmorph commands for players.
 */
public class MorphCommand {

        /**
         * Register morph commands with the command dispatcher.
         */
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
                dispatcher.register(
                                Commands.literal("morph")
                                                .requires(source -> source.hasPermission(2)) // Op level 2
                                                .then(Commands.argument("entity", ResourceLocationArgument.id())
                                                                .suggests((context, builder) -> {
                                                                        // Suggest all entity types
                                                                        BuiltInRegistries.ENTITY_TYPE.keySet()
                                                                                        .forEach(key -> builder.suggest(
                                                                                                        key.toString()));
                                                                        return builder.buildFuture();
                                                                })
                                                                .executes(MorphCommand::executeMorph)));

                dispatcher.register(
                                Commands.literal("unmorph")
                                                .requires(source -> source.hasPermission(2)) // Op level 2
                                                .executes(MorphCommand::executeUnmorph));

                // Morph stacking commands for testing genetics inheritance
                dispatcher.register(
                                Commands.literal("morphstack")
                                                .requires(source -> source.hasPermission(2)) // Op level 2
                                                .then(Commands.literal("add")
                                                                .then(Commands.argument("layer",
                                                                                StringArgumentType.string())
                                                                                .then(Commands.argument("priority",
                                                                                                IntegerArgumentType
                                                                                                                .integer(0, 1000))
                                                                                                .then(Commands.argument(
                                                                                                                "entity",
                                                                                                                ResourceLocationArgument
                                                                                                                                .id())
                                                                                                                .executes(MorphCommand::executeAddMorphLayer)))))
                                                .then(Commands.literal("remove")
                                                                .then(Commands.argument("layer",
                                                                                StringArgumentType.string())
                                                                                .executes(MorphCommand::executeRemoveMorphLayer)))
                                                .then(Commands.literal("clear")
                                                                .executes(MorphCommand::executeClearMorphStack))
                                                .then(Commands.literal("info")
                                                                .executes(MorphCommand::executeMorphStackInfo)));

                ModLogger.info("MorphCommand", "Morph commands registered");
        }

        /**
         * Execute the /morph command to apply a morph to the player.
         */
        private static int executeMorph(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
                ServerPlayer player = context.getSource().getPlayerOrException();
                ResourceLocation entityId = ResourceLocationArgument.getId(context, "entity");

                // Get the entity type from registry
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).orElse(null);

                if (entityType == null) {
                        context.getSource().sendFailure(Component.literal("Unknown entity type: " + entityId));
                        ModLogger.warn("MorphCommand", "Player " + player.getName().getString() +
                                        " tried to morph into unknown entity: " + entityId);
                        return 0;
                }

                // Create morph data
                MorphData morphData = new MorphData();
                morphData.setEntityType(entityType);

                // Apply morph
                MorphManager.applyMorph(player, morphData);

                // Sync to the player themselves (important for single-player and self-view)
                MorphLibNetworking.sendToPlayer(player, player, morphData);
                
                // Sync to all OTHER players tracking this player
                MorphLibNetworking.sendToAllTracking(player, morphData);

                // Send success message
                context.getSource().sendSuccess(
                                () -> Component.literal("Morphed into " + entityId),
                                true);

                ModLogger.info("MorphCommand", "Player " + player.getName().getString() +
                                " morphed into " + entityId);

                return 1;
        }

        /**
         * Execute the /unmorph command to remove a morph from the player.
         */
        private static int executeUnmorph(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
                ServerPlayer player = context.getSource().getPlayerOrException();

                if (!MorphManager.hasMorph(player)) {
                        context.getSource().sendFailure(Component.literal("You are not morphed"));
                        return 0;
                }

                // Remove morph
                MorphManager.removeMorph(player);

                // Sync removal to the player themselves
                MorphLibNetworking.sendRemovalToPlayer(player, player);
                
                // Sync removal to all OTHER players tracking this player
                MorphLibNetworking.sendRemovalToAllTracking(player);

                // Send success message
                context.getSource().sendSuccess(
                                () -> Component.literal("Morph removed"),
                                true);

                ModLogger.info("MorphCommand", "Player " + player.getName().getString() + " unmorphed");

                return 1;
        }

        /**
         * Execute the /morphstack add command to add a morph layer.
         */
        private static int executeAddMorphLayer(CommandContext<CommandSourceStack> context)
                        throws CommandSyntaxException {
                ServerPlayer player = context.getSource().getPlayerOrException();
                String layerId = StringArgumentType.getString(context, "layer");
                int priority = IntegerArgumentType.getInteger(context, "priority");
                ResourceLocation entityId = ResourceLocationArgument.getId(context, "entity");

                // Get the entity type from registry
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).orElse(null);

                if (entityType == null) {
                        context.getSource().sendFailure(Component.literal("Unknown entity type: " + entityId));
                        return 0;
                }

                // Create morph data
                MorphData morphData = new MorphData();
                morphData.setEntityType(entityType);

                // Add morph layer
                MorphManager.addMorphLayer(player, layerId, priority, morphData);

                // Send success message
                context.getSource().sendSuccess(
                                () -> Component.literal("Added morph layer '" + layerId + "' with priority " + priority
                                                + " (" + entityId + ")"),
                                true);

                ModLogger.info("MorphCommand", "Player " + player.getName().getString() +
                                " added morph layer: " + layerId + " (priority: " + priority + ", entity: " + entityId
                                + ")");

                return 1;
        }

        /**
         * Execute the /morphstack remove command to remove a morph layer.
         */
        private static int executeRemoveMorphLayer(CommandContext<CommandSourceStack> context)
                        throws CommandSyntaxException {
                ServerPlayer player = context.getSource().getPlayerOrException();
                String layerId = StringArgumentType.getString(context, "layer");

                // Check if layer exists
                MorphStack stack = MorphManager.getMorphStack(player);
                if (stack == null
                                || stack.getLayers().stream().noneMatch(layer -> layer.getLayerId().equals(layerId))) {
                        context.getSource().sendFailure(Component.literal("Layer '" + layerId + "' not found"));
                        return 0;
                }

                // Remove morph layer
                MorphManager.removeMorphLayer(player, layerId);

                // Send success message
                context.getSource().sendSuccess(
                                () -> Component.literal("Removed morph layer '" + layerId + "'"),
                                true);

                ModLogger.info("MorphCommand",
                                "Player " + player.getName().getString() + " removed morph layer: " + layerId);

                return 1;
        }

        /**
         * Execute the /morphstack clear command to clear all morph layers.
         */
        private static int executeClearMorphStack(CommandContext<CommandSourceStack> context)
                        throws CommandSyntaxException {
                ServerPlayer player = context.getSource().getPlayerOrException();

                // Clear all morphs
                MorphManager.clearAllMorphs(player);

                // Send success message
                context.getSource().sendSuccess(
                                () -> Component.literal("Cleared all morph layers"),
                                true);

                ModLogger.info("MorphCommand", "Player " + player.getName().getString() + " cleared all morph layers");

                return 1;
        }

        /**
         * Execute the /morphstack info command to show current morph stack.
         */
        private static int executeMorphStackInfo(CommandContext<CommandSourceStack> context)
                        throws CommandSyntaxException {
                ServerPlayer player = context.getSource().getPlayerOrException();

                MorphStack stack = MorphManager.getMorphStack(player);
                if (stack == null || stack.getLayers().isEmpty()) {
                        context.getSource().sendSuccess(
                                        () -> Component.literal("No morph layers active"),
                                        false);
                        return 1;
                }

                // Show stack info
                context.getSource().sendSuccess(
                                () -> Component.literal("Active morph layers:"),
                                false);

                stack.getLayers().forEach(layer -> {
                        String entityName = layer.getMorphData().getEntityType() != null
                                        ? BuiltInRegistries.ENTITY_TYPE.getKey(layer.getMorphData().getEntityType())
                                                        .toString()
                                        : "unknown";
                        context.getSource().sendSuccess(
                                        () -> Component.literal("  " + layer.getLayerId() + " (priority: "
                                                        + layer.getPriority() + ", entity: " + entityName + ")"),
                                        false);
                });

                // Show effective morph
                MorphData effective = MorphManager.getEffectiveMorph(player);
                if (effective != null && effective.getEntityType() != null) {
                        String effectiveEntity = BuiltInRegistries.ENTITY_TYPE.getKey(effective.getEntityType())
                                        .toString();
                        context.getSource().sendSuccess(
                                        () -> Component.literal("Effective morph: " + effectiveEntity),
                                        false);
                }

                return 1;
        }
}
