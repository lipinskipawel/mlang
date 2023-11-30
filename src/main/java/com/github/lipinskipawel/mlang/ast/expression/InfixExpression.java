package com.github.lipinskipawel.mlang.ast.expression;

import com.github.lipinskipawel.mlang.token.Token;

public final class InfixExpression extends Expression {
    private final Token token;
    private final Expression left;
    private final String operator;
    private Expression right;

    public InfixExpression(Token token, Expression left, String operator) {
        this.token = token;
        this.left = left;
        this.operator = operator;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        return "(" +
                left.string() + " " + operator + " " + right.string() +
                ")";
    }

    @Override
    public void expressionNode() {

    }

    public Expression left() {
        return left;
    }

    public String operator() {
        return operator;
    }

    public Expression right() {
        return right;
    }

    public void right(Expression right) {
        this.right = right;
    }
}
