package com.github.lipinskipawel.mlang.evaluator.objects;

public enum ObjectType {
    INTEGER_OBJ("INTEGER"),
    BOOLEAN_OBJ("BOOLEAN"),
    NULL_OBJ("NULL");

    private final String name;

    ObjectType(String name) {
        this.name = name;
    }
}
