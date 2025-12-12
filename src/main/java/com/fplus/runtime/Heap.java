package com.fplus.runtime;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe heap for managing class instances with Automatic Reference
 * Counting (ARC).
 * <p>
 * The heap stores {@link StructValue} instances allocated for classes
 * (reference types)
 * and manages their lifetimes through reference counting. When the reference
 * count
 * reaches zero, the instance is automatically freed.
 * <p>
 * Key features:
 * <ul>
 * <li><b>Thread-safety</b>: Uses {@link ConcurrentHashMap} and
 * {@link AtomicInteger} for safe concurrent access
 * <li><b>Automatic memory management</b>: ARC tracks object lifetimes without
 * manual deallocation
 * <li><b>Recursive release</b>: Freeing an object recursively releases its
 * contained values
 * <li><b>Unique addresses</b>: Each allocated object gets a UUID-based address
 * </ul>
 * <p>
 * Usage: Classes use reference semantics (allocated on heap), while structs use
 * value semantics
 * (stack-allocated). {@link ClassReference} objects hold heap addresses and use
 * this heap
 * to manage their lifetimes.
 * 
 * @see ClassReference
 * @see Value
 */
public class Heap {
    // Thread-Safe Memory Storage
    private final Map<String, StructValue> memory = new ConcurrentHashMap<>();

    // Thread-Safe Reference Counting
    private final Map<String, AtomicInteger> refCounts = new ConcurrentHashMap<>();

    public String allocate(StructValue instance) {
        String address = UUID.randomUUID().toString().substring(0, 8);
        memory.put(address, instance);
        refCounts.put(address, new AtomicInteger(0));
        // Debug output might interleave, but that's fine for prototype
        // System.out.println("[Heap] Allocating " + address + " (" + instance.typeName
        // + ") on " + Thread.currentThread().getName());
        return address;
    }

    public StructValue dereference(String address) {
        if (!memory.containsKey(address)) {
            throw new RuntimeException("Segmentation Fault: accessing deleted or invalid memory at " + address);
        }
        return memory.get(address);
    }

    public void retain(String address) {
        if (!memory.containsKey(address))
            return;

        AtomicInteger count = refCounts.get(address);
        if (count != null) {
            count.incrementAndGet();
            // System.out.println("[Heap] Retain " + address + " -> " + count.get());
        }
    }

    public void release(String address) {
        if (!memory.containsKey(address))
            return;

        AtomicInteger atomicCount = refCounts.get(address);
        if (atomicCount == null)
            return; // Already freed race condition?

        int count = atomicCount.decrementAndGet();
        // System.out.println("[Heap] Release " + address + " -> " + count);

        if (count == 0) {
            free(address);
        } else if (count < 0) {
            throw new RuntimeException("Ref count underflow for " + address);
        }
    }

    private synchronized void free(String address) {
        // Double check in case of race?
        // If atomic ref count hit 0, only one thread should see that transition if
        // purely decremental?
        // But 'retain' could happen concurrently?
        // ARC Principle: You can only retain if you hold a valid reference (count > 0).
        // If count is 0, no one holds it, so no one can retain it.
        // So free is safe?

        if (!memory.containsKey(address))
            return; // Already freed

        // System.out.println("[Heap] Freeing " + address);
        StructValue val = memory.get(address);
        memory.remove(address);
        refCounts.remove(address);

        // Recursive release!
        val.release(this);
    }
}
