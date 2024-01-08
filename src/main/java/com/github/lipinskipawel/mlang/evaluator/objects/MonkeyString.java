package com.github.lipinskipawel.mlang.evaluator.objects;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.STRING_OBJ;

public final class MonkeyString extends MonkeyObject implements Hashable {
    private final String value;

    public MonkeyString(String value) {
        this.value = value;
    }

    public HashKey hashKey() {
        return new HashKey(type(), value.hashCode());
    }

    @Override
    public ObjectType type() {
        return STRING_OBJ;
    }

    @Override
    public String inspect() {
        return value;
    }

    public String value() {
        return value;
    }
}
