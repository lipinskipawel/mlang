package com.github.lipinskipawel.mlang.evaluator.objects;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.BOOLEAN_OBJ;

final class MonkeyBoolean extends MonkeyObject {
    private boolean value;

    @Override
    public ObjectType type() {
        return BOOLEAN_OBJ;
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }
}
