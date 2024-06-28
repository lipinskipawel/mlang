package com.github.lipinskipawel.mlang.evaluator.objects;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonkeyError that = (MonkeyError) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(message);
    }
}
