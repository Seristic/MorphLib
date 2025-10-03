package com.seristic.morphlib.neoforge;

import com.seristic.morphlib.MorphData;
import com.seristic.morphlib.MorphStack;
import com.seristic.morphlib.Morphlib;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Registers NeoForge data attachments for MorphLib.
 * Attachments store MorphData and MorphStack on entities.
 */
public class MorphAttachments {

        public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
                        .create(NeoForgeRegistries.ATTACHMENT_TYPES, Morphlib.MOD_ID);

        /**
         * The morph attachment type.
         * Stores MorphData on entities (especially Players and LivingEntities).
         * Defaults to null (no morph applied).
         */
        public static final DeferredHolder<AttachmentType<?>, AttachmentType<MorphData>> MORPH_DATA = ATTACHMENT_TYPES
                        .register("morph_data", () -> AttachmentType.<MorphData>builder(() -> null).build());

        /**
         * The morph stack attachment type.
         * Stores MorphStack on entities for complex layered morphs.
         * Defaults to null (no morph stack applied).
         */
        public static final DeferredHolder<AttachmentType<?>, AttachmentType<MorphStack>> MORPH_STACK = ATTACHMENT_TYPES
                        .register("morph_stack", () -> AttachmentType.<MorphStack>builder(() -> null).build());
}
