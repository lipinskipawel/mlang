package com.github.lipinskipawel.mlang.ast;

import com.github.lipinskipawel.mlang.ast.statement.Statement;

import java.util.ArrayList;
import java.util.List;

public final class Program extends Node {
    private final List<Statement> statements;

    public Program() {
        this.statements = new ArrayList<>();
    }

    private Program(List<Statement> statements) {
        this.statements = statements;
    }

    public static Program givenProgram(List<Statement> builtProgram) {
        return new Program(builtProgram);
    }

    @Override
    public String tokenLiteral() {
        if (!statements.isEmpty()) {
            return statements.get(0).tokenLiteral();
        }
        return "";
    }

    @Override
    public String string() {
        return statements.stream()
                .map(Node::string)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    public void add(Statement statement) {
        this.statements.add(statement);
    }

    public List<Statement> programStatements() {
        return List.copyOf(statements);
    }
}
