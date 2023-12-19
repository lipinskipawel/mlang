package com.github.lipinskipawel.mlang.evaluator.objects;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.NULL_OBJ;

public final class MonkeyNull extends MonkeyObject {

    @Override
    public ObjectType type() {
        return NULL_OBJ;
    }

    @Override
    public String inspect() {
        return "null";
    }
}
