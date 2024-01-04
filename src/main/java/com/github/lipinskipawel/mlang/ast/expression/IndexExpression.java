package com.github.lipinskipawel.mlang.ast.expression;

import com.github.lipinskipawel.mlang.token.Token;

public final class IndexExpression extends Expression {
    private final Token token; // The [ token
    private Expression left;
    private Expression index;

    public IndexExpression(Token token, Expression left) {
        this.token = token;
        this.left = left;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        return "("
                + left.string()
                + "["
                + index.string()
                + "])";
    }

    @Override
    public void expressionNode() {

    }

    public Expression left() {
        return left;
    }

    public Expression index() {
        return index;
    }

    public void index(Expression index) {
        this.index = index;
    }
}
