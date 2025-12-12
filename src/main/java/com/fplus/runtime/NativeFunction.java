package com.fplus.runtime;

import java.util.List;

/**
 * Interface for native (built-in) functions implemented in Java.
 * <p>
 * Native functions provide core functionality that can't be implemented in
 * FPlus/Ponz
 * itself, such as I/O, system interaction, and primitive operations. They are
 * registered during interpreter initialization and callable like user-defined
 * functions.
 * <p>
 * Built-in native functions include:
 * <ul>
 * <li>{@code time()} - Current Unix timestamp
 * <li>{@code len(list)} - List length
 * <li>{@code push(list, item)} - Append to list
 * <li>{@code get(list, index)} - Access list element
 * <li>{@code sleep(ms)} - Sleep for milliseconds
 * <li>{@code spawn(lambda)} - Spawn concurrent thread
 * </ul>
 * 
 * @see FunctionValue
 * @see Heap
 */
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
