package com.github.lipinskipawel.mlang.evaluator.objects;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.STRING_OBJ;

public final class MonkeyString extends MonkeyObject {
    private final String value;

    public MonkeyString(String value) {
        this.value = value;
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
