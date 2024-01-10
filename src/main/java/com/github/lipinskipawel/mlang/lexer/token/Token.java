package com.github.lipinskipawel.mlang.lexer.token;

public record Token(TokenType type, String literal) {

    public Token(String literal) {
        this(TokenType.fromLiteral(literal), literal);
    }
}
