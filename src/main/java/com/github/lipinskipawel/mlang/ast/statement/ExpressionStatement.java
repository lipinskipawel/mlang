package com.github.lipinskipawel.mlang.ast.statement;

import com.github.lipinskipawel.mlang.ast.expression.Expression;
import com.github.lipinskipawel.mlang.token.Token;

public final class ExpressionStatement extends Statement {
    private Token token; // the first token of the expression
    private Expression expression;

    public ExpressionStatement(Token token) {
        this.token = token;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        if (expression != null) {
            return expression.string();
        }
        return "";
    }

    @Override
    public void statementNode() {

    }

    public void expression(Expression expression) {
        this.expression = expression;
    }

    public Expression expression() {
        return expression;
    }
}
