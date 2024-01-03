package com.github.lipinskipawel.mlang.evaluator.objects;

import java.util.List;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.BUILTIN_OBJ;

public abstract class MonkeyBuiltin extends MonkeyObject {

    public abstract MonkeyObject builtin(List<MonkeyObject> objects);

    @Override
    public ObjectType type() {
        return BUILTIN_OBJ;
    }

    @Override
    public String inspect() {
        return "builtin function";
    }
}
