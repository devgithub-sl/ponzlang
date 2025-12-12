package com.fplus.runtime;

import java.util.List;

public interface NativeFunction extends Value {
    Value call(List<Value> arguments, Heap heap);

    @Override
    default Value copy() {
        return this;
    } // Native functions are likely stateless singletons usually

    @Override
    default void retain(Heap heap) {
    }

    @Override
    default void release(Heap heap) {
    }

    @Override
    default Object getRaw() {
        return "<native fn>";
    }
}
