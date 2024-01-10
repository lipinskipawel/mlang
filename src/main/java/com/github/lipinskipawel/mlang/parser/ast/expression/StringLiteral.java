package com.github.lipinskipawel.mlang.parser.ast.expression;

import com.github.lipinskipawel.mlang.lexer.token.Token;

public final class StringLiteral extends Expression {
    private final Token token;
    private final String value;

    public StringLiteral(Token token, String value) {
        this.token = token;
        this.value = value;
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

    public String value() {
        return value;
    }
}
