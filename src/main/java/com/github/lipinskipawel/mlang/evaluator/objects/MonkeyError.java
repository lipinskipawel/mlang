package com.github.lipinskipawel.mlang.evaluator.objects;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.ERROR_OBJ;

public final class MonkeyError extends MonkeyObject {
    private final String message;

    public MonkeyError(String message) {
        this.message = message;
    }

    @Override
    public ObjectType type() {
        return ERROR_OBJ;
    }

    @Override
    public String inspect() {
        return "ERROR: " + message;
    }

    public String message() {
        return message;
    }
}
