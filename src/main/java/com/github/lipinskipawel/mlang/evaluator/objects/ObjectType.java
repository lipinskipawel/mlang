package com.github.lipinskipawel.mlang.evaluator.objects;

public enum ObjectType {
    INTEGER_OBJ("INTEGER"),
    BOOLEAN_OBJ("BOOLEAN"),
    NULL_OBJ("NULL"),
    RETURN_VALUE_OBJ("RETURN_VALUE"),
    ERROR_OBJ("ERROR"),
    FUNCTION_OBJ("FUNCTION");

    private final String name;

    ObjectType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
