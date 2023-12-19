package com.github.lipinskipawel.mlang.evaluator;

import com.github.lipinskipawel.mlang.ast.Node;
import com.github.lipinskipawel.mlang.ast.Program;
import com.github.lipinskipawel.mlang.ast.expression.IntegerLiteral;
import com.github.lipinskipawel.mlang.ast.statement.ExpressionStatement;
import com.github.lipinskipawel.mlang.ast.statement.Statement;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;

import java.util.List;

public final class Evaluator {

    public static MonkeyObject eval(Node node) {
        return switch (node) {
            case Program program -> evalStatements(program.programStatements());
            case ExpressionStatement expressionStatement -> eval(expressionStatement.expression());

            case IntegerLiteral integer -> new MonkeyInteger(integer.value());
            default -> null;
        };
    }

    private static MonkeyObject evalStatements(List<Statement> statements) {
        MonkeyObject result = null;

        for (var statement : statements) {
            result = eval(statement);
        }

        return result;
    }
}
