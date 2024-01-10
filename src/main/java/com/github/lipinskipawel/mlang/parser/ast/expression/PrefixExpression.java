package com.github.lipinskipawel.mlang.parser.ast.expression;

import com.github.lipinskipawel.mlang.lexer.token.Token;

public final class PrefixExpression extends Expression {
    private final Token token; // The prefix token, e.g. !
    private String operator;
    private Expression right;

    public PrefixExpression(Token token, String operator) {
        this.token = token;
        this.operator = operator;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        return "(" +
                operator +
                right.string() +
                ")";
    }

    @Override
    public void expressionNode() {

    }

    public String operator() {
        return operator;
    }

    public void right(Expression expression) {
        this.right = expression;
    }

    public Expression right() {
        return right;
    }
}
