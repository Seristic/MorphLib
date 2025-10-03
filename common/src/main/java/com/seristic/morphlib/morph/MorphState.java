package com.seristic.morphlib.morph;

import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Represents a complete morph state with all transformation parameters.
 * This is the core data structure that stores body proportions and
 * transformations.
 */
public class MorphState {
    // Core body proportions
    private float height = 1.0f;
    private float bodyWidth = 1.0f;
    private float armLength = 1.0f;
    private float legLength = 1.0f;

    // Detailed body parts
    private float chestScale = 1.0f;
    private float chestSpacing = 0.0f;
    private float hipWidth = 1.0f;
    private float shoulderWidth = 1.0f;
    private float neckLength = 1.0f;
    private float headSize = 1.0f;

    // Animation parameters
    private float idleBounce = 0.0f;
    private float walkSway = 1.0f;

    // Cached hash for change detection
    private transient int cachedHash = -1;

    public MorphState() {
    }

    public MorphState(MorphState other) {
        this.height = other.height;
        this.bodyWidth = other.bodyWidth;
        this.armLength = other.armLength;
        this.legLength = other.legLength;
        this.chestScale = other.chestScale;
        this.chestSpacing = other.chestSpacing;
        this.hipWidth = other.hipWidth;
        this.shoulderWidth = other.shoulderWidth;
        this.neckLength = other.neckLength;
        this.headSize = other.headSize;
        this.idleBounce = other.idleBounce;
        this.walkSway = other.walkSway;
        this.cachedHash = other.cachedHash;
    }

    /**
     * Interpolate between two morph states.
     * 
     * @param from     Starting state
     * @param to       Target state
     * @param progress Progress from 0.0 to 1.0
     * @return Interpolated state
     */
    public static MorphState lerp(MorphState from, MorphState to, float progress) {
        MorphState result = new MorphState();
        result.height = lerp(from.height, to.height, progress);
        result.bodyWidth = lerp(from.bodyWidth, to.bodyWidth, progress);
        result.armLength = lerp(from.armLength, to.armLength, progress);
        result.legLength = lerp(from.legLength, to.legLength, progress);
        result.chestScale = lerp(from.chestScale, to.chestScale, progress);
        result.chestSpacing = lerp(from.chestSpacing, to.chestSpacing, progress);
        result.hipWidth = lerp(from.hipWidth, to.hipWidth, progress);
        result.shoulderWidth = lerp(from.shoulderWidth, to.shoulderWidth, progress);
        result.neckLength = lerp(from.neckLength, to.neckLength, progress);
        result.headSize = lerp(from.headSize, to.headSize, progress);
        result.idleBounce = lerp(from.idleBounce, to.idleBounce, progress);
        result.walkSway = lerp(from.walkSway, to.walkSway, progress);
        return result;
    }

    private static float lerp(float from, float to, float progress) {
        return from + (to - from) * progress;
    }

    /**
     * Generate a hash for change detection.
     */
    public int getContentHash() {
        if (cachedHash == -1) {
            cachedHash = Objects.hash(height, bodyWidth, armLength, legLength,
                    chestScale, chestSpacing, hipWidth, shoulderWidth,
                    neckLength, headSize, idleBounce, walkSway);
        }
        return cachedHash;
    }

    /**
     * Check if this state has changed compared to another.
     */
    public boolean hasChanged(MorphState other) {
        if (other == null)
            return true;
        return getContentHash() != other.getContentHash();
    }

    /**
     * Invalidate the cached hash (call when modifying state).
     */
    private void invalidateHash() {
        cachedHash = -1;
    }

    // Getters and setters with hash invalidation
    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
        invalidateHash();
    }

    public float getBodyWidth() {
        return bodyWidth;
    }

    public void setBodyWidth(float bodyWidth) {
        this.bodyWidth = bodyWidth;
        invalidateHash();
    }

    public float getArmLength() {
        return armLength;
    }

    public void setArmLength(float armLength) {
        this.armLength = armLength;
        invalidateHash();
    }

    public float getLegLength() {
        return legLength;
    }

    public void setLegLength(float legLength) {
        this.legLength = legLength;
        invalidateHash();
    }

    public float getChestScale() {
        return chestScale;
    }

    public void setChestScale(float chestScale) {
        this.chestScale = chestScale;
        invalidateHash();
    }

    public float getChestSpacing() {
        return chestSpacing;
    }

    public void setChestSpacing(float chestSpacing) {
        this.chestSpacing = chestSpacing;
        invalidateHash();
    }

    public float getHipWidth() {
        return hipWidth;
    }

    public void setHipWidth(float hipWidth) {
        this.hipWidth = hipWidth;
        invalidateHash();
    }

    public float getShoulderWidth() {
        return shoulderWidth;
    }

    public void setShoulderWidth(float shoulderWidth) {
        this.shoulderWidth = shoulderWidth;
        invalidateHash();
    }

    public float getNeckLength() {
        return neckLength;
    }

    public void setNeckLength(float neckLength) {
        this.neckLength = neckLength;
        invalidateHash();
    }

    public float getHeadSize() {
        return headSize;
    }

    public void setHeadSize(float headSize) {
        this.headSize = headSize;
        invalidateHash();
    }

    public float getIdleBounce() {
        return idleBounce;
    }

    public void setIdleBounce(float idleBounce) {
        this.idleBounce = idleBounce;
        invalidateHash();
    }

    public float getWalkSway() {
        return walkSway;
    }

    public void setWalkSway(float walkSway) {
        this.walkSway = walkSway;
        invalidateHash();
    }

    /**
     * Get a transformation parameter by name (for generic access).
     */
    public float getParameter(String name) {
        return switch (name) {
            case "height" -> height;
            case "bodyWidth" -> bodyWidth;
            case "armLength" -> armLength;
            case "legLength" -> legLength;
            case "chestScale" -> chestScale;
            case "chestSpacing" -> chestSpacing;
            case "hipWidth" -> hipWidth;
            case "shoulderWidth" -> shoulderWidth;
            case "neckLength" -> neckLength;
            case "headSize" -> headSize;
            case "idleBounce" -> idleBounce;
            case "walkSway" -> walkSway;
            default -> 1.0f;
        };
    }

    /**
     * Set a transformation parameter by name (for generic access).
     */
    public void setParameter(String name, float value) {
        switch (name) {
            case "height" -> setHeight(value);
            case "bodyWidth" -> setBodyWidth(value);
            case "armLength" -> setArmLength(value);
            case "legLength" -> setLegLength(value);
            case "chestScale" -> setChestScale(value);
            case "chestSpacing" -> setChestSpacing(value);
            case "hipWidth" -> setHipWidth(value);
            case "shoulderWidth" -> setShoulderWidth(value);
            case "neckLength" -> setNeckLength(value);
            case "headSize" -> setHeadSize(value);
            case "idleBounce" -> setIdleBounce(value);
            case "walkSway" -> setWalkSway(value);
        }
    }

    /**
     * Serialize to NBT for persistence.
     */
    public CompoundTag writeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("height", height);
        tag.putFloat("bodyWidth", bodyWidth);
        tag.putFloat("armLength", armLength);
        tag.putFloat("legLength", legLength);
        tag.putFloat("chestScale", chestScale);
        tag.putFloat("chestSpacing", chestSpacing);
        tag.putFloat("hipWidth", hipWidth);
        tag.putFloat("shoulderWidth", shoulderWidth);
        tag.putFloat("neckLength", neckLength);
        tag.putFloat("headSize", headSize);
        tag.putFloat("idleBounce", idleBounce);
        tag.putFloat("walkSway", walkSway);
        return tag;
    }

    /**
     * Deserialize from NBT.
     */
    public static MorphState readNBT(CompoundTag tag) {
        MorphState state = new MorphState();
        state.height = tag.getFloat("height");
        state.bodyWidth = tag.getFloat("bodyWidth");
        state.armLength = tag.getFloat("armLength");
        state.legLength = tag.getFloat("legLength");
        state.chestScale = tag.getFloat("chestScale");
        state.chestSpacing = tag.getFloat("chestSpacing");
        state.hipWidth = tag.getFloat("hipWidth");
        state.shoulderWidth = tag.getFloat("shoulderWidth");
        state.neckLength = tag.getFloat("neckLength");
        state.headSize = tag.getFloat("headSize");
        state.idleBounce = tag.getFloat("idleBounce");
        state.walkSway = tag.getFloat("walkSway");
        return state;
    }

    /**
     * Serialize to network buffer.
     */
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeFloat(height);
        buf.writeFloat(bodyWidth);
        buf.writeFloat(armLength);
        buf.writeFloat(legLength);
        buf.writeFloat(chestScale);
        buf.writeFloat(chestSpacing);
        buf.writeFloat(hipWidth);
        buf.writeFloat(shoulderWidth);
        buf.writeFloat(neckLength);
        buf.writeFloat(headSize);
        buf.writeFloat(idleBounce);
        buf.writeFloat(walkSway);
    }

    /**
     * Deserialize from network buffer.
     */
    public static MorphState read(RegistryFriendlyByteBuf buf) {
        MorphState state = new MorphState();
        state.height = buf.readFloat();
        state.bodyWidth = buf.readFloat();
        state.armLength = buf.readFloat();
        state.legLength = buf.readFloat();
        state.chestScale = buf.readFloat();
        state.chestSpacing = buf.readFloat();
        state.hipWidth = buf.readFloat();
        state.shoulderWidth = buf.readFloat();
        state.neckLength = buf.readFloat();
        state.headSize = buf.readFloat();
        state.idleBounce = buf.readFloat();
        state.walkSway = buf.readFloat();
        return state;
    }
}