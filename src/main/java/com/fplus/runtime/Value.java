package com.fplus.runtime;

/**
 * Base interface for all runtime values in FPlus/Ponz.
 * <p>
 * This interface abstracts over different value types (primitives, structs,
 * lists,
 * functions, pointers, Erlang types, etc.) providing a common protocol for:
 * <ul>
 * <li>Accessing the underlying raw value
 * <li>Creating copies for value semantics
 * <li>Reference counting via Automatic Reference Counting (ARC)
 * </ul>
 * <p>
 * Implementations include:
 * {@link PrimitiveValue}, {@link StructValue}, {@link ListValue},
 * {@link FunctionValue},
 * {@link PointerValue}, {@link AtomValue}, {@link TupleValue}, {@link MapValue}
 * 
 * @see Heap
 * @see Environment
 */
public interface Value {
    /**
     * Returns the raw underlying Java object representing this value.
     * 
     * @return The raw value (e.g., Integer, String, etc.)
     */
    Object getRaw();

    /**
     * Creates a deep copy of this value.
     * <p>
     * Used for implementing value semantics (e.g., struct assignment).
     * 
     * @return A new value with copied data
     */
    Value copy();

    /**
     * Increments the reference count for this value on the heap.
     * <p>
     * Part of the Automatic Reference Counting (ARC) memory management system.
     * 
     * @param heap The heap managing this value's lifecycle
     */
    void retain(Heap heap);

    /**
     * Decrements the reference count for this value on the heap.
     * <p>
     * When the count reaches zero, the value may be deallocated.
     * Part of the Automatic Reference Counting (ARC) memory management system.
     * 
     * @param heap The heap managing this value's lifecycle
     */
    void release(Heap heap);
}
