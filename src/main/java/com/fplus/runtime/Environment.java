package com.fplus.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class Environment {
    private final Environment enclosing;
    private final Map<String, Value> values = new HashMap<>(); // Current value
    private final Map<String, Boolean> mu = new HashMap<>(); // Is Mutable?
    private final Map<String, String> types = new HashMap<>(); // Inferred Type Name ("int", "string", "Point", etc.)

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(String name, Value value, boolean isMutable) {
        values.put(name, value);
        mu.put(name, isMutable);
        types.put(name, inferType(value)); // Lock the type
    }

    public Value get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        if (enclosing != null)
            return enclosing.get(name);
        throw new RuntimeException("Undefined variable '" + name + "'.");
    }

    public void assign(String name, Value value) {
        if (values.containsKey(name)) {
            if (!mu.get(name)) {
                throw new RuntimeException("Cannot assign to immutable variable '" + name + "'.");
            }

            // Type Safety Check
            String expectedType = types.get(name);
            String actualType = inferType(value);
            if (!expectedType.equals(actualType)) {
                throw new RuntimeException(
                        "Type mismatch for '" + name + "'. Expected " + expectedType + " but got " + actualType + ".");
            }

            values.put(name, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeException("Undefined variable '" + name + "'.");
    }

    public Collection<Value> getLocalValues() {
        return values.values();
    }

    public Map<String, Value> getExports() {
        return new HashMap<>(values);
    }

    public Environment resolve(String name) {
        if (values.containsKey(name)) {
            return this;
        }
        if (enclosing != null) {
            return enclosing.resolve(name);
        }
        return null;
    }

    // Type Inference Helper
    public static String inferType(Value value) {
        if (value instanceof PrimitiveValue) {
            Object raw = value.getRaw();
            if (raw instanceof Integer)
                return "int";
            if (raw instanceof String)
                return "string";
            if (raw instanceof Boolean)
                return "bool";
        }
        if (value instanceof StructValue) {
            return ((StructValue) value).typeName;
        }
        if (value instanceof ClassReference) {
            // For references, we don't store the type in the reference object itself in
            // this prototype (oops).
            // ClassReference only has address.
            // We need to resolve it on Heap to know the type?
            // Or we should store type in ClassReference.
            // Let's modify ClassReference to carry type info for safety checks.
            return ((ClassReference) value).typeName;
        }
        return "unknown";
    }
}
