package com.github.lipinskipawel.mlang.token;

import static com.github.lipinskipawel.mlang.token.TokenType.fromLiteral;

public record Token(TokenType type, String literal) {

    public Token(String literal) {
        this(fromLiteral(literal), literal);
    }
}
