package com.github.lipinskipawel.mlang.parser.ast.expression;

import com.github.lipinskipawel.mlang.lexer.token.Token;

public final class IntegerLiteral extends Expression {
    private Token token;
    private int value;

    public IntegerLiteral(Token token) {
        this.token = token;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        return token.literal();
    }

    @Override
    public void expressionNode() {

    }

    public void value(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
