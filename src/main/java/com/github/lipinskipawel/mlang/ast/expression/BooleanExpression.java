package com.github.lipinskipawel.mlang.ast.expression;

import com.github.lipinskipawel.mlang.token.Token;

public final class BooleanExpression extends Expression {
    private final Token token;
    private final boolean value;

    public BooleanExpression(Token token, boolean bool) {
        this.token = token;
        this.value = bool;
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

    public boolean value() {
        return value;
    }
}
