package com.seristic.morphlib.neoforge;

import com.seristic.morphlib.MorphData;
import com.seristic.morphlib.MorphManager;
import net.minecraft.world.entity.Entity;

/**
 * NeoForge-specific implementation of MorphAccessor.
 * Uses data attachments to store morph data on entities.
 */
public class NeoForgeMorphAccessor implements MorphManager.MorphAccessor {

    @Override
    public MorphData getMorph(Entity entity) {
        return entity.getData(MorphAttachments.MORPH_DATA);
    }

    @Override
    public void setMorph(Entity entity, MorphData data) {
        entity.setData(MorphAttachments.MORPH_DATA, data);
    }

    @Override
    public void removeMorph(Entity entity) {
        entity.removeData(MorphAttachments.MORPH_DATA);
    }
}
