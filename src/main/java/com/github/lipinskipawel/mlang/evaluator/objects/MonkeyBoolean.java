package com.github.lipinskipawel.mlang.evaluator.objects;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.BOOLEAN_OBJ;

public final class MonkeyBoolean extends MonkeyObject {
    private final boolean value;

    public MonkeyBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public ObjectType type() {
        return BOOLEAN_OBJ;
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }

    public boolean value() {
        return value;
    }
}
