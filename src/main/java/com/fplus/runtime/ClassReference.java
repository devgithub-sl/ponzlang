package com.fplus.runtime;

/**
 * Reference to a class instance allocated on the {@link Heap}.
 * <p>
 * In FPlus/Ponz, classes use reference semantics (like Java objects), while
 * structs
 * use value semantics. When a class is instantiated with {@code new}, it's
 * allocated
 * on the heap and a ClassReference is returned containing the address.
 * <p>
 * The reference participates in ARC by calling {@link Heap#retain(String)} and
 * {@link Heap#release(String)} on the address when the reference is copied or
 * destroyed.
 * When the reference count reaches zero, the heap automatically frees the
 * object.
 * 
 * @see Heap
 * @see StructValue
 */
public class ClassReference implements Value {
    public final String address;
    public final String typeName; // For type safety

    public ClassReference(String address, String typeName) {
        this.address = address;
        this.typeName = typeName;
    }

    @Override
    public Object getRaw() {
        return address;
    }

    @Override
    public Value copy() {
        return new ClassReference(address, typeName);
    }

    @Override
    public void retain(Heap heap) {
        heap.retain(address);
    }

    @Override
    public void release(Heap heap) {
        heap.release(address);
    }

    @Override
    public String toString() {
        return "ref<" + address + ":" + typeName + ">";
    }
}
