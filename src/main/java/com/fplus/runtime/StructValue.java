package com.fplus.runtime;

import java.util.HashMap;
import java.util.Map;

public class StructValue implements Value {
    public final String typeName;
    private final Map<String, Value> fields;

    public StructValue(String typeName) {
        this.typeName = typeName;
        this.fields = new HashMap<>();
    }

    private StructValue(String typeName, Map<String, Value> fields) {
        this.typeName = typeName;
        this.fields = new HashMap<>();
        for (Map.Entry<String, Value> entry : fields.entrySet()) {
            this.fields.put(entry.getKey(), entry.getValue().copy());
        }
    }

    public void set(String name, Value value) {
        fields.put(name, value);
    }

    public Value get(String name) {
        return fields.get(name);
    }

    @Override
    public Object getRaw() {
        return this;
    }

    @Override
    public Value copy() {
        // Deep copy of struct means we have new values.
        // If those values are references, we will eventually Retain them when this
        // struct is Retained.
        return new StructValue(typeName, fields);
    }

    @Override
    public void retain(Heap heap) {
        // When a struct is retained, all its fields must be retained
        // because the struct "owns" its fields (Value Semantics)
        for (Value val : fields.values()) {
            val.retain(heap);
        }
    }

    @Override
    public void release(Heap heap) {
        for (Value val : fields.values()) {
            val.release(heap);
        }
    }

    @Override
    public String toString() {
        return typeName + " " + fields.toString();
    }
}
