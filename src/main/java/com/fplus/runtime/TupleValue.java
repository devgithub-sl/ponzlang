package com.fplus.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Erlang-style tuple - a fixed-size, ordered collection of heterogeneous
 * values.
 * <p>
 * Tuples group related values together and are commonly used in pattern
 * matching
 * and multiple return values. Unlike lists, tuples are immutable and
 * fixed-size.
 * Tuples use value semantics - assignment creates a deep copy.
 * <p>
 * Syntax in FPlus/Ponz: {@code {element1, element2, ...}}
 * <p>
 * Examples:
 * <ul>
 * <li>{@code {1, 2, 3}} - Three-element tuple
 * <li>{@code {@ok, "success"}} - Result tuple with atom and string
 * <li>{@code {@error, 404, "Not Found"}} - HTTP error tuple
 * </ul>
 * 
 * @see AtomValue
 * @see MapValue
 */
public class TupleValue implements Value {
    public final List<Value> elements;

    public TupleValue(List<Value> elements) {
        this.elements = elements;
    }

    @Override
    public Object getRaw() {
        return elements;
    }

    @Override
    public Value copy() {
        // Tuples have value semantics, deep copy elements
        List<Value> newElements = new ArrayList<>();
        for (Value v : elements) {
            newElements.add(v.copy());
        }
        return new TupleValue(newElements);
    }

    @Override
    public void retain(Heap heap) {
        for (Value v : elements) {
            v.retain(heap);
        }
    }

    @Override
    public void release(Heap heap) {
        for (Value v : elements) {
            v.release(heap); // Assuming we own them
        }
    }

    @Override
    public String toString() {
        return "{" + elements.stream().map(Object::toString).collect(Collectors.joining(", ")) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        // Tuples strictly compare contents?
        if (o == null || getClass() != o.getClass())
            return false;
        TupleValue that = (TupleValue) o;
        // Assuming elements implement equals correctly (Primitives do, Atoms do)
        // Structs might not yet, but best effort.
        return Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }
}
