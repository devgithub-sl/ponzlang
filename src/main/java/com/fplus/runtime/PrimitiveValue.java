package com.fplus.runtime;

/**
 * Wrapper for primitive values in FPlus/Ponz (integers, strings, booleans).
 * <p>
 * Primitive values are immutable and don't require heap allocation or reference
 * counting. They are copied by value and managed by the JVM's garbage
 * collector.
 * <p>
 * Supported primitive types:
 * <ul>
 * <li>{@link Integer} - Numeric values
 * <li>{@link String} - Text values
 * <li>{@link Boolean} - True/false values
 * </ul>
 * 
 * @see Value
 */
public class PrimitiveValue implements Value {
    private final Object value;

    public PrimitiveValue(Object value) {
        this.value = value;
    }

    @Override
    public Object getRaw() {
        return value;
    }

    @Override
    public Value copy() {
        return this;
    }

    @Override
    public void retain(Heap heap) {
        // Primitives don't need memory management
    }

    @Override
    public void release(Heap heap) {
        // Primitives don't need memory management
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
