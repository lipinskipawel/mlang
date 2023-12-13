package com.github.lipinskipawel.mlang.ast.statement;

import com.github.lipinskipawel.mlang.ast.expression.Expression;
import com.github.lipinskipawel.mlang.ast.expression.Identifier;
import com.github.lipinskipawel.mlang.token.Token;

public final class LetStatement extends Statement {
    private Token token; // the Token.LET token
    public Identifier name;
    private Expression value;

    public LetStatement(Token token) {
        this.token = token;
    }

    public LetStatement(Token token, Identifier identifier, Expression expression) {
        this.token = token;
        this.name = identifier;
        this.value = expression;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        final var stringBuilder = new StringBuilder();

        stringBuilder.append(tokenLiteral())
                .append(" ")
                .append(name.string())
                .append(" = ");

        if (value != null) {
            stringBuilder.append(value.string());
        }

        return stringBuilder
                .append(";")
                .toString();
    }

    @Override
    public void statementNode() {

    }

    public Identifier name() {
        return name;
    }

    public void value(Expression expression) {
        this.value = expression;
    }

    public Expression value() {
        return value;
    }
}
