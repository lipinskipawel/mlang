package com.github.lipinskipawel.mlang.ast.statement;

import com.github.lipinskipawel.mlang.ast.expression.Expression;
import com.github.lipinskipawel.mlang.token.Token;

public final class ExpressionStatement extends Statement {
    private Token token; // the first token of the expression
    private Expression expression;

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
}
