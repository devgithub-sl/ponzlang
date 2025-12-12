package com.fplus.runtime;

import java.util.Objects;

public class AtomValue implements Value {
    public final String name;

    public AtomValue(String name) {
        this.name = name;
    }

    @Override
    public Object getRaw() {
        return name;
    }

    @Override
    public Value copy() {
        // Atoms are immutable
        return this;
    }

    @Override
    public void retain(Heap heap) {
        // Atoms usually don't need extensive memory management if they are just
        // strings,
        // but consistent RC is good practice so it doesn't crash if we ever track them.
        // For now, no-op or default is fine as they are essentially primitives.
    }

    @Override
    public void release(Heap heap) {
        // No-op
    }

    @Override
    public String toString() {
        return "@" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AtomValue atomValue = (AtomValue) o;
        return Objects.equals(name, atomValue.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
