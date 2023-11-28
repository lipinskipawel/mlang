package com.github.lipinskipawel.mlang.ast.statement;

import com.github.lipinskipawel.mlang.ast.expression.Expression;
import com.github.lipinskipawel.mlang.token.Token;

public final class ReturnStatement extends Statement {
    private Token token; // the Token.RETURN token
    private Expression returnValue;

    public ReturnStatement(Token token) {
        this.token = token;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        final var stringBuilder = new StringBuilder();

        stringBuilder.append(tokenLiteral())
                .append(" ");

        if (returnValue != null) {
            stringBuilder.append(returnValue.string());
        }

        return stringBuilder
                .append(";")
                .toString();
    }

    @Override
    public void statementNode() {

    }
}
