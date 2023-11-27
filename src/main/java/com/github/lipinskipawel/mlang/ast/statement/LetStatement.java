package com.github.lipinskipawel.mlang.ast.statement;

import com.github.lipinskipawel.mlang.ast.expression.Expression;
import com.github.lipinskipawel.mlang.ast.expression.Identifier;
import com.github.lipinskipawel.mlang.token.Token;

public final class LetStatement implements Statement {
    private Token token; // the Token.LET token
    public Identifier name;
    private Expression value;

    public LetStatement(Token token) {
        this.token = token;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public void statementNode() {

    }

    public Identifier name() {
        return name;
    }

    public Expression value() {
        return value;
    }
}
