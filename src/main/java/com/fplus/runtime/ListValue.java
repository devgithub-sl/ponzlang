package com.fplus.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListValue implements Value {
    private final List<Value> elements;

    public ListValue(List<Value> elements) {
        this.elements = new ArrayList<>(elements);
    }

    public void add(Value value) {
        elements.add(value);
    }

    public Value get(int index) {
        if (index < 0 || index >= elements.size()) {
            throw new RuntimeException("Index out of bounds: " + index);
        }
        return elements.get(index);
    }

    public int size() {
        return elements.size();
    }

    @Override
    public Object getRaw() {
        return elements;
    }

    @Override
    public Value copy() {
        // Shallow copy list, but deep copy logic depends on semantics.
        // FPlus struct/list semantics: List itself is a Reference or Value?
        // Let's say List is a REFERENCE type by default in modern langs or Value type?
        // In this prototype, treating List as a VALUE (Python list is Ref, C++ vector
        // is Value).
        // If Value: copy means valid independent list.
        List<Value> newElements = new ArrayList<>();
        for (Value v : elements) {
            newElements.add(v.copy());
        }
        return new ListValue(newElements);
    }

    @Override
    public void retain(Heap heap) {
        // List retains its elements
        for (Value v : elements) {
            v.retain(heap);
        }
    }

    @Override
    public void release(Heap heap) {
        // List releases its elements
        for (Value v : elements) {
            v.release(heap);
        }
    }

    @Override
    public String toString() {
        return "[" + elements.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }
}
