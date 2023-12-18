package com.github.lipinskipawel.mlang.evaluator.objects;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.INTEGER_OBJ;

final class MonkeyInteger extends MonkeyObject {
    private int value;

    @Override
    public ObjectType type() {
        return INTEGER_OBJ;
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }
}
