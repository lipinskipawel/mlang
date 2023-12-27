package com.github.lipinskipawel.mlang.evaluator.objects;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.RETURN_VALUE_OBJ;

public final class ReturnValue extends MonkeyObject {
    private final MonkeyObject value;

    public ReturnValue(MonkeyObject value) {
        this.value = value;
    }

    @Override
    public ObjectType type() {
        return RETURN_VALUE_OBJ;
    }

    @Override
    public String inspect() {
        return value.inspect();
    }

    public MonkeyObject value() {
        return value;
    }
}
