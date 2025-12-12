package com.fplus.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MapValue implements Value {
    public final Map<Value, Value> entries;

    public MapValue(Map<Value, Value> entries) {
        this.entries = entries;
    }

    @Override
    public Object getRaw() {
        return entries;
    }

    @Override
    public Value copy() {
        // Deep copy Map
        Map<Value, Value> newEntries = new HashMap<>();
        for (Map.Entry<Value, Value> e : entries.entrySet()) {
            newEntries.put(e.getKey().copy(), e.getValue().copy());
        }
        return new MapValue(newEntries);
    }

    @Override
    public void retain(Heap heap) {
        for (Map.Entry<Value, Value> e : entries.entrySet()) {
            e.getKey().retain(heap);
            e.getValue().retain(heap);
        }
    }

    @Override
    public void release(Heap heap) {
        for (Map.Entry<Value, Value> e : entries.entrySet()) {
            e.getKey().release(heap);
            e.getValue().release(heap);
        }
    }

    @Override
    public String toString() {
        return "#{" + entries.entrySet().stream()
                .map(e -> e.getKey().toString() + " => " + e.getValue().toString())
                .collect(Collectors.joining(", ")) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MapValue mapValue = (MapValue) o;
        return Objects.equals(entries, mapValue.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }
}
