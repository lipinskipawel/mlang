package com.github.lipinskipawel.mlang.evaluator.objects;

public enum ObjectType {
    INTEGER_OBJ("INTEGER"),
    BOOLEAN_OBJ("BOOLEAN"),
    NULL_OBJ("NULL"),
    RETURN_VALUE_OBJ("RETURN_VALUE"),
    ERROR_OBJ("ERROR"),
    FUNCTION_OBJ("FUNCTION"),

    STRING_OBJ("STRING"),
    BUILTIN_OBJ("BUILTIN_OBJ"),
    ARRAY_OBJ("ARRAY_OBJ"),
    HASH_OBJ("HASH_OBJ"),

    COMPILED_FUNCTION_OBJ("COMPILED_FUNCTION_OBJ"),
    CLOSURE_OBJ("CLOSURE_OBJ");

    private final String name;

    ObjectType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
