package com.seristic.morphlib;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EntityType;

import java.util.*;

/**
 * Represents a stack of morph layers that can be combined.
 * This allows for complex inheritance and trait combination systems.
 * 
 * Layers are applied in order, with higher priority layers overriding lower
 * ones.
 * This enables genetics systems where base traits can be modified by mutations,
 * temporary effects, or other modifying factors.
 */
public class MorphStack {

    /**
     * Represents a single layer in the morph stack with its priority.
     */
    public static class MorphLayer {
        private final String layerId;
        private final int priority;
        private final MorphData morphData;
        private final Map<String, Object> metadata;

        public MorphLayer(String layerId, int priority, MorphData morphData) {
            this.layerId = layerId;
            this.priority = priority;
            this.morphData = morphData;
            this.metadata = new HashMap<>();
        }

        public MorphLayer(String layerId, int priority, MorphData morphData, Map<String, Object> metadata) {
            this.layerId = layerId;
            this.priority = priority;
            this.morphData = morphData;
            this.metadata = new HashMap<>(metadata);
        }

        public String getLayerId() {
            return layerId;
        }

        public int getPriority() {
            return priority;
        }

        public MorphData getMorphData() {
            return morphData;
        }

        public Map<String, Object> getMetadata() {
            return Collections.unmodifiableMap(metadata);
        }

        public void setMetadata(String key, Object value) {
            metadata.put(key, value);
        }

        public Object getMetadata(String key) {
            return metadata.get(key);
        }
    }

    private final List<MorphLayer> layers;

    public MorphStack() {
        this.layers = new ArrayList<>();
    }

    public MorphStack(List<MorphLayer> layers) {
        this.layers = new ArrayList<>(layers);
        // Sort by priority (higher priority first)
        this.layers.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
    }

    /**
     * Add a morph layer to the stack.
     * 
     * @param layerId   Unique identifier for this layer
     * @param priority  Higher numbers take precedence (0-1000 range recommended)
     * @param morphData The morph data for this layer
     */
    public void addLayer(String layerId, int priority, MorphData morphData) {
        // Remove any existing layer with the same ID
        removeLayer(layerId);

        layers.add(new MorphLayer(layerId, priority, morphData));
        // Re-sort by priority
        layers.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
    }

    /**
     * Add a morph layer with metadata.
     */
    public void addLayer(String layerId, int priority, MorphData morphData, Map<String, Object> metadata) {
        removeLayer(layerId);
        layers.add(new MorphLayer(layerId, priority, morphData, metadata));
        layers.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
    }

    /**
     * Remove a layer by ID.
     */
    public void removeLayer(String layerId) {
        layers.removeIf(layer -> layer.getLayerId().equals(layerId));
    }

    /**
     * Get a layer by ID.
     */
    public Optional<MorphLayer> getLayer(String layerId) {
        return layers.stream().filter(layer -> layer.getLayerId().equals(layerId)).findFirst();
    }

    /**
     * Get all layers, sorted by priority (highest first).
     */
    public List<MorphLayer> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    /**
     * Check if the stack has any layers.
     */
    public boolean isEmpty() {
        return layers.isEmpty();
    }

    /**
     * Clear all layers.
     */
    public void clear() {
        layers.clear();
    }

    /**
     * Combine all layers into a single MorphData, applying priority-based
     * inheritance.
     * Higher priority values override lower ones.
     */
    public MorphData combine() {
        if (layers.isEmpty()) {
            return new MorphData(); // Default morph data
        }

        // Start with the lowest priority layer and work up
        List<MorphLayer> reversedLayers = new ArrayList<>(layers);
        Collections.reverse(reversedLayers);

        MorphData result = new MorphData();

        for (MorphLayer layer : reversedLayers) {
            MorphData layerData = layer.getMorphData();

            // Apply each field if it's not null/default in the layer
            if (layerData.getGender() != null) {
                result.setGender(layerData.getGender());
            }

            if (layerData.getBodyType() != null) {
                result.setBodyType(layerData.getBodyType());
            }

            if (layerData.getCustomSkin() != null) {
                result.setCustomSkin(layerData.getCustomSkin());
            }

            if (layerData.getEntityType() != null && layerData.getEntityType() != EntityType.PLAYER) {
                result.setEntityType(layerData.getEntityType());
            }
        }

        return result;
    }

    /**
     * Get the highest priority entity type that's not PLAYER.
     */
    public EntityType<?> getEffectiveEntityType() {
        for (MorphLayer layer : layers) {
            EntityType<?> entityType = layer.getMorphData().getEntityType();
            if (entityType != null && entityType != EntityType.PLAYER) {
                return entityType;
            }
        }
        return EntityType.PLAYER;
    }

    /**
     * Check if any layer has a non-player entity type.
     */
    public boolean hasMorph() {
        return layers.stream().anyMatch(layer -> {
            EntityType<?> entityType = layer.getMorphData().getEntityType();
            return entityType != null && entityType != EntityType.PLAYER;
        });
    }

    /**
     * Write the morph stack to a network buffer.
     */
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(layers.size());
        for (MorphLayer layer : layers) {
            buf.writeUtf(layer.getLayerId());
            buf.writeInt(layer.getPriority());
            layer.getMorphData().write(buf);

            // Write metadata
            buf.writeInt(layer.getMetadata().size());
            for (Map.Entry<String, Object> entry : layer.getMetadata().entrySet()) {
                buf.writeUtf(entry.getKey());
                // For now, only support string metadata - can be extended later
                buf.writeUtf(entry.getValue().toString());
            }
        }
    }

    /**
     * Read a morph stack from a network buffer.
     */
    public static MorphStack read(RegistryFriendlyByteBuf buf) {
        int layerCount = buf.readInt();
        List<MorphLayer> readLayers = new ArrayList<>();

        for (int i = 0; i < layerCount; i++) {
            String layerId = buf.readUtf();
            int priority = buf.readInt();
            MorphData morphData = MorphData.read(buf);

            Map<String, Object> metadata = new HashMap<>();
            int metadataCount = buf.readInt();
            for (int j = 0; j < metadataCount; j++) {
                String key = buf.readUtf();
                String value = buf.readUtf();
                metadata.put(key, value);
            }

            readLayers.add(new MorphLayer(layerId, priority, morphData, metadata));
        }

        return new MorphStack(readLayers);
    }

    /**
     * Write the morph stack to an NBT compound tag.
     */
    public CompoundTag writeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag layersTag = new ListTag();

        for (MorphLayer layer : layers) {
            CompoundTag layerTag = new CompoundTag();
            layerTag.putString("layerId", layer.getLayerId());
            layerTag.putInt("priority", layer.getPriority());
            layerTag.put("morphData", layer.getMorphData().writeNBT());

            // Write metadata
            CompoundTag metadataTag = new CompoundTag();
            for (Map.Entry<String, Object> entry : layer.getMetadata().entrySet()) {
                // For now, only support string metadata - can be extended later
                metadataTag.putString(entry.getKey(), entry.getValue().toString());
            }
            layerTag.put("metadata", metadataTag);

            layersTag.add(layerTag);
        }

        tag.put("layers", layersTag);
        return tag;
    }

    /**
     * Read a morph stack from an NBT compound tag.
     */
    public static MorphStack readNBT(CompoundTag tag) {
        List<MorphLayer> readLayers = new ArrayList<>();

        if (tag.contains("layers", Tag.TAG_LIST)) {
            ListTag layersTag = tag.getList("layers", Tag.TAG_COMPOUND);

            for (int i = 0; i < layersTag.size(); i++) {
                CompoundTag layerTag = layersTag.getCompound(i);

                String layerId = layerTag.getString("layerId");
                int priority = layerTag.getInt("priority");

                MorphData morphData = null;
                if (layerTag.contains("morphData", Tag.TAG_COMPOUND)) {
                    morphData = MorphData.readNBT(layerTag.getCompound("morphData"));
                } else {
                    morphData = new MorphData(); // Default fallback
                }

                Map<String, Object> metadata = new HashMap<>();
                if (layerTag.contains("metadata", Tag.TAG_COMPOUND)) {
                    CompoundTag metadataTag = layerTag.getCompound("metadata");
                    for (String key : metadataTag.getAllKeys()) {
                        metadata.put(key, metadataTag.getString(key));
                    }
                }

                readLayers.add(new MorphLayer(layerId, priority, morphData, metadata));
            }
        }

        return new MorphStack(readLayers);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MorphStack{layers=[");
        for (int i = 0; i < layers.size(); i++) {
            if (i > 0)
                sb.append(", ");
            MorphLayer layer = layers.get(i);
            sb.append(layer.getLayerId())
                    .append("(p=").append(layer.getPriority()).append(")")
                    .append("=").append(layer.getMorphData());
        }
        sb.append("]}");
        return sb.toString();
    }
}