package com.github.lipinskipawel.mlang.evaluator;

import com.github.lipinskipawel.mlang.ast.Node;
import com.github.lipinskipawel.mlang.ast.Program;
import com.github.lipinskipawel.mlang.ast.expression.BooleanExpression;
import com.github.lipinskipawel.mlang.ast.expression.IntegerLiteral;
import com.github.lipinskipawel.mlang.ast.statement.ExpressionStatement;
import com.github.lipinskipawel.mlang.ast.statement.Statement;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBoolean;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;

import java.util.List;

public final class Evaluator {
    private static final MonkeyBoolean TRUE = new MonkeyBoolean(true);
    private static final MonkeyBoolean FALSE = new MonkeyBoolean(false);

    public static MonkeyObject eval(Node node) {
        return switch (node) {
            case Program program -> evalStatements(program.programStatements());
            case ExpressionStatement expressionStatement -> eval(expressionStatement.expression());

            case IntegerLiteral integer -> new MonkeyInteger(integer.value());
            case BooleanExpression bool -> nativeBoolToMonkeyBoolean(bool.value());
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

    private static MonkeyBoolean nativeBoolToMonkeyBoolean(boolean bool) {
        return bool ? TRUE : FALSE;
    }
}
