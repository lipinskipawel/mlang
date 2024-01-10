package com.github.lipinskipawel.mlang.parser.ast.statement;

import com.github.lipinskipawel.mlang.lexer.token.Token;

import java.util.List;

public final class BlockStatement extends Statement {
    private Token token; // The { token
    private List<Statement> statements;

    public BlockStatement(Token token) {
        this.token = token;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        final var stringBuilder = new StringBuilder();

        statements.forEach(it -> stringBuilder.append(it.string()));

        return stringBuilder.toString();
    }

    @Override
    public void statementNode() {

    }

    public void statements(List<Statement> statements) {
        this.statements = statements;
    }

    public List<Statement> statements() {
        return statements;
    }
}
