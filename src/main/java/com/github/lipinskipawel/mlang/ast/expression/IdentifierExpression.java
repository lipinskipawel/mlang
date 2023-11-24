package com.github.lipinskipawel.mlang.ast.expression;

import com.github.lipinskipawel.mlang.token.Token;

/**
 * let x = 5;
 * <p></p>
 * IdentifierExpression is the 'x'.
 */
public final class IdentifierExpression implements Expression {
    private Token token; // the token.IDENT token
    private String value;

    public IdentifierExpression(Token token, String value) {
        this.token = token;
        this.value = value;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public void expressionNode() {

    }

    public String value() {
        return value;
    }
}
