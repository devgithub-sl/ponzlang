package com.fplus.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * Runtime representation of a struct instance in FPlus/Ponz.
 * <p>
 * Structs are user-defined composite types with named fields. They use value
 * semantics,
 * meaning assignment creates a deep copy rather than sharing references.
 * Structs can be
 * defined with {@code type MyStruct = struct { ... }} and instantiated with
 * {@code new}.
 * <p>
 * Key characteristics:
 * <ul>
 * <li><b>Value semantics</b>: Copying a struct creates an independent copy
 * <li><b>Named fields</b>: Access fields via dot notation {@code obj.field}
 * <li><b>Deep copying</b>: All contained values are recursively copied
 * <li><b>ARC participation</b>: Retains/releases all field values when owned
 * </ul>
 * <p>
 * Note: Structs can also be used internally for module exports and other
 * aggregate data.
 * 
 * @see ClassReference
 * @see Environment
 */
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
