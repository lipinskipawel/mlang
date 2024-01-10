package com.github.lipinskipawel.mlang.parser.ast.expression;

import com.github.lipinskipawel.mlang.lexer.token.Token;

public final class Identifier extends Expression {
    private Token token; // the token.IDENT token
    private String value;

    public Identifier(Token token, String value) {
        this.token = token;
        this.value = value;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        return value;
    }

    @Override
    public void expressionNode() {

    }

    public String value() {
        return value;
    }
}
