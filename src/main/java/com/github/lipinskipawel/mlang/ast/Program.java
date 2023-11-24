package com.github.lipinskipawel.mlang.ast;

import com.github.lipinskipawel.mlang.ast.statement.Statement;

import java.util.ArrayList;
import java.util.List;

public final class Program implements Node {
    private final List<Statement> statements;

    public Program() {
        this.statements = new ArrayList<>();
    }

    @Override
    public String tokenLiteral() {
        if (!statements.isEmpty()) {
            return statements.get(0).tokenLiteral();
        }
        return "";
    }

    public void add(Statement statement) {
        this.statements.add(statement);
    }

    public List<Statement> programStatements() {
        return List.copyOf(statements);
    }
}
