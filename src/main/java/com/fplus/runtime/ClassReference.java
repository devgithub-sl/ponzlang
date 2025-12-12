package com.fplus.runtime;

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
