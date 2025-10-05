package com.seristic.morphlib;

import com.seristic.morphlib.morph.MorphState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.UUID;

public class MorphData {
    public enum Gender {
        MALE,
        FEMALE,
    }

    public enum BodyType {
        NORMAL,
        SLIM
    }

    private UUID morphId;

    private Gender gender = Gender.MALE;
    private BodyType bodyType = BodyType.NORMAL;
    private ResourceLocation customSkin = null;
    private EntityType<?> entityType = EntityType.PLAYER;
    private MorphState morphState = new MorphState();

    public MorphData() {
        this.morphId = UUID.randomUUID();
    }

    public MorphData(UUID morphId) {
        this.morphId = morphId;
    }

    public static MorphData create(EntityType<?> entityType, Gender gender, BodyType bodyType,
            ResourceLocation customSkin) {
        MorphData data = new MorphData();
        data.setEntityType(entityType);
        data.setGender(gender);
        data.setBodyType(bodyType);
        data.setCustomSkin(customSkin);
        return data;
    }

    public static MorphData create(EntityType<?> entityType, Gender gender, BodyType bodyType,
            ResourceLocation customSkin, MorphState morphState) {
        MorphData data = new MorphData();
        data.setEntityType(entityType);
        data.setGender(gender);
        data.setBodyType(bodyType);
        data.setCustomSkin(customSkin);
        data.setMorphState(morphState);
        return data;
    }

    public UUID getMorphId() {
        return morphId;
    }

    public void setMorphId(UUID morphId) {
        this.morphId = morphId;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public BodyType getBodyType() {
        return bodyType;
    }

    public void setBodyType(BodyType bodyType) {
        this.bodyType = bodyType;
    }

    public ResourceLocation getCustomSkin() {
        return customSkin;
    }

    public void setCustomSkin(ResourceLocation customSkin) {
        this.customSkin = customSkin;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    public MorphState getMorphState() {
        return morphState;
    }

    public void setMorphState(MorphState morphState) {
        this.morphState = morphState != null ? morphState : new MorphState();
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(morphId);

        buf.writeEnum(gender);
        buf.writeEnum(bodyType);
        buf.writeBoolean(customSkin != null);
        if (customSkin != null) {
            buf.writeResourceLocation(customSkin);
        }
        buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
        morphState.write(buf);
    }

    public static MorphData read(RegistryFriendlyByteBuf buf) {
        UUID morphId = buf.readUUID();

        MorphData data = new MorphData(morphId);
        data.gender = buf.readEnum(Gender.class);
        data.bodyType = buf.readEnum(BodyType.class);
        if (buf.readBoolean()) {
            data.customSkin = buf.readResourceLocation();
        }
        ResourceLocation entityTypeId = buf.readResourceLocation();
        data.entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityTypeId).orElse(EntityType.PLAYER);
        data.morphState = MorphState.read(buf);
        return data;
    }

    /**
     * Write the morph data to an NBT compound tag.
     */
    public CompoundTag writeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("morphId", morphId);
        tag.putString("gender", gender.name());
        tag.putString("bodyType", bodyType.name());
        if (customSkin != null) {
            tag.putString("customSkin", customSkin.toString());
        }
        tag.putString("entityType", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());

        // Write morph state
        tag.put("morphState", morphState.writeNBT());

        return tag;
    }

    /**
     * Read morph data from an NBT compound tag.
     */
    public static MorphData readNBT(CompoundTag tag) {
        UUID morphId = tag.contains("morphId") ? tag.getUUID("morphId") : UUID.randomUUID();
        MorphData data = new MorphData(morphId);

        if (tag.contains("gender")) {
            try {
                data.gender = Gender.valueOf(tag.getString("gender"));
            } catch (IllegalArgumentException e) {
                data.gender = Gender.MALE; // Default fallback
            }
        }

        if (tag.contains("bodyType")) {
            try {
                data.bodyType = BodyType.valueOf(tag.getString("bodyType"));
            } catch (IllegalArgumentException e) {
                data.bodyType = BodyType.NORMAL; // Default fallback
            }
        }

        if (tag.contains("customSkin")) {
            try {
                data.customSkin = ResourceLocation.parse(tag.getString("customSkin"));
            } catch (Exception e) {
                data.customSkin = null; // Invalid resource location
            }
        }

        if (tag.contains("entityType")) {
            ResourceLocation entityTypeId;
            try {
                entityTypeId = ResourceLocation.parse(tag.getString("entityType"));
                data.entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityTypeId).orElse(EntityType.PLAYER);
            } catch (Exception e) {
                data.entityType = EntityType.PLAYER; // Default fallback
            }
        }

        // Read morph state
        if (tag.contains("morphState")) {
            data.morphState = MorphState.readNBT(tag.getCompound("morphState"));
        }

        return data;
    }

    @Override
    public String toString() {
        return "MorphData{gender=" + gender + ", bodyType=" + bodyType + ", customSkin=" + customSkin + ", entityType="
                + entityType + ", morphState=" + morphState + "}";
    }
}
