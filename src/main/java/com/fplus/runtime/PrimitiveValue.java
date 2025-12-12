package com.fplus.runtime;

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
