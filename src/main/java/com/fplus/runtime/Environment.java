package com.fplus.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

/**
 * Manages variable bindings and scopes in the FPlus/Ponz interpreter.
 * <p>
 * Environment implements lexical scoping with support for:
 * <ul>
 * <li>Variable definitions with immutability tracking
 * <li>Static type inference and enforcement
 * <li>Nested scopes via enclosing environment chain
 * <li>Variable shadowing in inner scopes
 * </ul>
 * <p>
 * Each variable binding stores:
 * <ul>
 * <li>The current value ({@code values} map)
 * <li>Mutability flag ({@code mu} map) - {@code let} vs {@code mutable}
 * <li>Inferred type ({@code types} map) - enforced on reassignment
 * </ul>
 * 
 * @see Value
 * @see com.fplus.Interpreter
 */
public class Environment {
    /** The parent/enclosing scope, or null for global scope */
    private final Environment enclosing;

    /** Map from variable name to current value */
    private final Map<String, Value> values = new HashMap<>();

    /**
     * Map from variable name to mutability flag (true = mutable, false = immutable)
     */
    private final Map<String, Boolean> mu = new HashMap<>();

    /**
     * Map from variable name to inferred type name ("int", "string", struct names,
     * etc.)
     */
    private final Map<String, String> types = new HashMap<>();

    /**
     * Creates a new global (top-level) environment with no enclosing scope.
     */
    public Environment() {
        enclosing = null;
    }

    /**
     * Creates a new environment nested within an enclosing scope.
     * 
     * @param enclosing The parent environment for variable resolution
     */
    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * Defines a new variable in this environment.
     * <p>
     * Captures the value, mutability, and infers the type. Type is locked at
     * definition
     * and enforced on all future assignments to this variable.
     * 
     * @param name      Variable name
     * @param value     Initial value
     * @param isMutable true for {@code mutable} variables, false for {@code let}
     */
    public void define(String name, Value value, boolean isMutable) {
        values.put(name, value);
        mu.put(name, isMutable);
        types.put(name, inferType(value)); // Lock the type
    }

    /**
     * Retrieves the value of a variable by name.
     * <p>
     * Searches this environment first, then walks up the enclosing chain.
     * 
     * @param name Variable name to look up
     * @return The variable's current value
     * @throws RuntimeException if variable is not defined in any scope
     */
    public Value get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        if (enclosing != null)
            return enclosing.get(name);
        throw new RuntimeException("Undefined variable '" + name + "'.");
    }

    /**
     * Assigns a new value to an existing variable.
     * <p>
     * Enforces:
     * <ul>
     * <li>Variable must already be defined
     * <li>Variable must be mutable ({@code mutable}, not {@code let})
     * <li>New value must match the variable's original inferred type
     * </ul>
     * Walks up the enclosing chain to find the variable's defining scope.
     * 
     * @param name  Variable name
     * @param value New value to assign
     * @throws RuntimeException if variable is undefined, immutable, or type
     *                          mismatches
     */
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

    /**
     * Infers the type name for a given value.
     * <p>
     * Used for type checking during variable assignment.
     * 
     * @param value The value to inspect
     * @return Type name string ("int", "string", "bool", struct name, or "unknown")
     */
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
