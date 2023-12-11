package com.github.lipinskipawel.mlang.ast.expression;

import com.github.lipinskipawel.mlang.ast.statement.BlockStatement;
import com.github.lipinskipawel.mlang.token.Token;

public final class IfExpression extends Expression {
    private Token token; // The 'if' token
    private Expression condition;
    private BlockStatement consequence;
    private BlockStatement alternative;

    public IfExpression(Token token) {
        this.token = token;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        final var stringBuilder = new StringBuilder();

        stringBuilder
                .append("if")
                .append(condition.string())
                .append(" ")
                .append(consequence.string());

        if (alternative != null) {
            stringBuilder
                    .append("else ")
                    .append(alternative.string());
        }

        return stringBuilder.toString();
    }

    @Override
    public void expressionNode() {

    }

    public void condition(Expression condition) {
        this.condition = condition;
    }

    public Expression condition() {
        return condition;
    }

    public void consequence(BlockStatement blockStatement) {
        this.consequence = blockStatement;
    }

    public BlockStatement consequence() {
        return consequence;
    }

    public void alternative(BlockStatement blockStatement) {
        this.alternative = blockStatement;
    }

    public BlockStatement alternative() {
        return alternative;
    }
}
