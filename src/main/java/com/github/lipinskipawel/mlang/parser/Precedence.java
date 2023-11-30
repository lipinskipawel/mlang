package com.github.lipinskipawel.mlang.parser;

enum Precedence {
    LOWEST(1),
    EQUALS(2), // ==
    LESSGREATER(3), // > or <
    SUM(4), // +
    PRODUCT(5), // *
    PREFIX(6), // -X or !X
    CALL(7); // myFunction(X)

    private final int order;

    Precedence(int order) {
        this.order = order;
    }

    boolean hasLowerPrecedence(Precedence precedence) {
        return order < precedence.order;
    }
}
