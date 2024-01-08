package com.github.lipinskipawel.mlang.evaluator.objects;

import java.util.Objects;

public final class HashKey {
    private final ObjectType type;
    private final int value;

    public HashKey(ObjectType type, int value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        HashKey hashKey = (HashKey) object;
        return value == hashKey.value && type == hashKey.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
