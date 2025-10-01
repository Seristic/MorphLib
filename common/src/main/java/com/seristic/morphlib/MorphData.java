package com.seristic.morphlib;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class MorphData {
    public enum Gender {
        MALE,
        FEMALE,
    }

    public enum BodyType {
        NORMAL,
        SLIM
    }

    private Gender gender = Gender.MALE;
    private BodyType bodyType = BodyType.NORMAL;
    private ResourceLocation customSkin = null;
    private EntityType<?> entityType = EntityType.PLAYER;

    public MorphData() {
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

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(gender);
        buf.writeEnum(bodyType);
        buf.writeBoolean(customSkin != null);
        if (customSkin != null) {
            buf.writeResourceLocation(customSkin);
        }
        buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
    }

    public static MorphData read(RegistryFriendlyByteBuf buf) {
        MorphData data = new MorphData();
        data.gender = buf.readEnum(Gender.class);
        data.bodyType = buf.readEnum(BodyType.class);
        if (buf.readBoolean()) {
            data.customSkin = buf.readResourceLocation();
        }
        ResourceLocation entityTypeId = buf.readResourceLocation();
        data.entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityTypeId).orElse(EntityType.PLAYER);
        return data;
    }

    @Override
    public String toString() {
        return "MorphData{gender=" + gender + ", bodyType=" + bodyType + ", customSkin=" + customSkin + ", entityType="
                + entityType + "}";
    }
}
