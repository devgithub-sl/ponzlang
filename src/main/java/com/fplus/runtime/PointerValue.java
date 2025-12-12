package com.fplus.runtime;

public class PointerValue implements Value {
    public final Environment environment;
    public final String name;

    public PointerValue(Environment environment, String name) {
        this.environment = environment;
        this.name = name;
    }

    @Override
    public Object getRaw() {
        return this;
    }

    @Override
    public Value copy() {
        // Pointers are values (the address is the value).
        // We copy the pointer, pointing to the same location.
        return new PointerValue(environment, name);
    }

    @Override
    public void retain(Heap heap) {
        // A pointer keeps the Environment alive?
        // Java GC handles Environment.
        // We don't really 'retain' the variable inside explicit ARC, unless the
        // variable value itself needs retain?
        // But pointer just points to the slot.
    }

    @Override
    public void release(Heap heap) {
        // Nothing to release.
    }

    @Override
    public String toString() {
        return "<ptr " + name + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PointerValue that = (PointerValue) o;
        return name.equals(that.name) && environment == that.environment;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + environment.hashCode();
    }
}
