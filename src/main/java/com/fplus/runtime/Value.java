package com.fplus.runtime;

public interface Value {
    Object getRaw();

    Value copy();

    // ARC Lifecycle
    void retain(Heap heap);

    void release(Heap heap);
}
