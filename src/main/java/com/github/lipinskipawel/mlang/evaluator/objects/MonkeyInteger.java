package com.github.lipinskipawel.mlang.evaluator.objects;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.INTEGER_OBJ;

public final class MonkeyInteger extends MonkeyObject implements Hashable {
    private final int value;

    public MonkeyInteger(int value) {
        this.value = value;
    }

    public HashKey hashKey() {
        return new HashKey(type(), value);
    }

    @Override
    public ObjectType type() {
        return INTEGER_OBJ;
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }

    public int value() {
        return value;
    }
}
