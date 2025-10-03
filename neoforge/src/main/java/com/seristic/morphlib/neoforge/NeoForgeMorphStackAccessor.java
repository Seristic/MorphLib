package com.seristic.morphlib.neoforge;

import com.seristic.morphlib.MorphManager;
import com.seristic.morphlib.MorphStack;
import net.minecraft.world.entity.Entity;

/**
 * NeoForge implementation of MorphStackAccessor using data attachments.
 */
public class NeoForgeMorphStackAccessor implements MorphManager.MorphStackAccessor {

    @Override
    public MorphStack getMorphStack(Entity entity) {
        return entity.getData(MorphAttachments.MORPH_STACK);
    }

    @Override
    public void setMorphStack(Entity entity, MorphStack stack) {
        entity.setData(MorphAttachments.MORPH_STACK, stack);
    }

    @Override
    public void removeMorphStack(Entity entity) {
        entity.removeData(MorphAttachments.MORPH_STACK);
    }
}