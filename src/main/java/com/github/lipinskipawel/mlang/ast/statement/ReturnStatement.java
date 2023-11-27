package com.github.lipinskipawel.mlang.ast.statement;

import com.github.lipinskipawel.mlang.ast.expression.Expression;
import com.github.lipinskipawel.mlang.token.Token;

public final class ReturnStatement implements Statement {
    private Token token; // the Token.RETURN token
    private Expression expression;

    public ReturnStatement(Token token) {
        this.token = token;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public void statementNode() {

    }
}
