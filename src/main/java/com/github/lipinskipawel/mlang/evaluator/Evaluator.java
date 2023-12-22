package com.github.lipinskipawel.mlang.evaluator;

import com.github.lipinskipawel.mlang.ast.Node;
import com.github.lipinskipawel.mlang.ast.Program;
import com.github.lipinskipawel.mlang.ast.expression.BooleanExpression;
import com.github.lipinskipawel.mlang.ast.expression.IntegerLiteral;
import com.github.lipinskipawel.mlang.ast.expression.PrefixExpression;
import com.github.lipinskipawel.mlang.ast.statement.ExpressionStatement;
import com.github.lipinskipawel.mlang.ast.statement.Statement;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBoolean;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyNull;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;

import java.util.List;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.INTEGER_OBJ;

public final class Evaluator {
    private static final MonkeyNull NULL = new MonkeyNull();
    private static final MonkeyBoolean TRUE = new MonkeyBoolean(true);
    private static final MonkeyBoolean FALSE = new MonkeyBoolean(false);

    public static MonkeyObject eval(Node node) {
        return switch (node) {
            // statements
            case Program program -> evalStatements(program.programStatements());
            case ExpressionStatement expressionStatement -> eval(expressionStatement.expression());

            // expressions
            case IntegerLiteral integer -> new MonkeyInteger(integer.value());
            case BooleanExpression bool -> nativeBoolToMonkeyBoolean(bool.value());

            case PrefixExpression prefix -> {
                final var right = eval(prefix.right());
                yield evalPrefixExpression(prefix.operator(), right);
            }
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

    private static MonkeyObject evalPrefixExpression(String operator, MonkeyObject right) {
        return switch (operator) {
            case "!" -> evalBangOperatorExpression(right);
            case "-" -> evalMinusOperatorExpression(right);
            default -> NULL;
        };
    }

    private static MonkeyObject evalBangOperatorExpression(MonkeyObject right) {
        return switch (right) {
            case MonkeyBoolean bool -> {
                if (bool.value()) {
                    yield FALSE;
                }
                yield TRUE;
            }
            case MonkeyNull __ -> TRUE;
            default -> FALSE;
        };
    }

    private static MonkeyObject evalMinusOperatorExpression(MonkeyObject right) {
        if (right.type() != INTEGER_OBJ) {
            return NULL;
        }

        return new MonkeyInteger(-((MonkeyInteger) (right)).value());

    }

    private static MonkeyBoolean nativeBoolToMonkeyBoolean(boolean bool) {
        return bool ? TRUE : FALSE;
    }
}
