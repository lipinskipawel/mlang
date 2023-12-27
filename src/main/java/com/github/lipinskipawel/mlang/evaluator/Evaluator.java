package com.github.lipinskipawel.mlang.evaluator;

import com.github.lipinskipawel.mlang.ast.Node;
import com.github.lipinskipawel.mlang.ast.Program;
import com.github.lipinskipawel.mlang.ast.expression.BooleanExpression;
import com.github.lipinskipawel.mlang.ast.expression.IfExpression;
import com.github.lipinskipawel.mlang.ast.expression.InfixExpression;
import com.github.lipinskipawel.mlang.ast.expression.IntegerLiteral;
import com.github.lipinskipawel.mlang.ast.expression.PrefixExpression;
import com.github.lipinskipawel.mlang.ast.statement.BlockStatement;
import com.github.lipinskipawel.mlang.ast.statement.ExpressionStatement;
import com.github.lipinskipawel.mlang.ast.statement.ReturnStatement;
import com.github.lipinskipawel.mlang.ast.statement.Statement;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBoolean;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyNull;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.evaluator.objects.ReturnValue;

import java.util.List;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.INTEGER_OBJ;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.RETURN_VALUE_OBJ;

public final class Evaluator {
    private static final MonkeyNull NULL = new MonkeyNull();
    private static final MonkeyBoolean TRUE = new MonkeyBoolean(true);
    private static final MonkeyBoolean FALSE = new MonkeyBoolean(false);

    private Evaluator() {

    }

    public static Evaluator evaluator() {
        return new Evaluator();
    }

    public MonkeyObject eval(Node node) {
        return switch (node) {
            // statements
            case Program program -> evalProgram(program.programStatements());
            case BlockStatement block -> evalBlockStatements(block);
            case IfExpression ifExpression -> evalIfExpression(ifExpression);
            case ExpressionStatement expressionStatement -> eval(expressionStatement.expression());
            case ReturnStatement returnStatement -> {
                final var returned = eval(returnStatement.returnValue());
                yield new ReturnValue(returned);
            }

            // expressions
            case IntegerLiteral integer -> new MonkeyInteger(integer.value());
            case BooleanExpression bool -> nativeBoolToMonkeyBoolean(bool.value());

            case PrefixExpression prefix -> {
                final var right = eval(prefix.right());
                yield evalPrefixExpression(prefix.operator(), right);
            }
            case InfixExpression infix -> {
                final var left = eval(infix.left());
                final var right = eval(infix.right());
                yield evalInfixExpression(infix.operator(), left, right);
            }
            default -> null;
        };
    }

    private MonkeyObject evalProgram(List<Statement> statements) {
        MonkeyObject result = null;

        for (var statement : statements) {
            result = eval(statement);
            if (result instanceof ReturnValue returnValue) {
                return returnValue.value();
            }
        }

        return result;
    }

    private MonkeyObject evalBlockStatements(BlockStatement block) {
        MonkeyObject result = null;

        for (var statement : block.statements()) {
            result = eval(statement);
            if (result != null && result.type() == RETURN_VALUE_OBJ) {
                return result;
            }
        }

        return result;
    }

    private MonkeyObject evalIfExpression(IfExpression ifExpression) {
        final var conditional = eval(ifExpression.condition());

        if (isTruthy(conditional)) {
            return eval(ifExpression.consequence());
        } else if (ifExpression.alternative() != null) {
            return eval(ifExpression.alternative());
        } else {
            return NULL;
        }
    }

    private boolean isTruthy(MonkeyObject object) {
        return switch (object) {
            case MonkeyBoolean bool -> bool.value();
            case MonkeyNull __ -> false;
            default -> true;
        };
    }

    private MonkeyObject evalPrefixExpression(String operator, MonkeyObject right) {
        return switch (operator) {
            case "!" -> evalBangOperatorExpression(right);
            case "-" -> evalMinusOperatorExpression(right);
            default -> NULL;
        };
    }

    private MonkeyObject evalBangOperatorExpression(MonkeyObject right) {
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

    private MonkeyObject evalMinusOperatorExpression(MonkeyObject right) {
        if (right.type() != INTEGER_OBJ) {
            return NULL;
        }
        return new MonkeyInteger(-((MonkeyInteger) (right)).value());
    }

    private MonkeyObject evalInfixExpression(String operator, MonkeyObject left, MonkeyObject right) {
        if (left.type() == INTEGER_OBJ && right.type() == INTEGER_OBJ) {
            return evalIntegerInfixExpression(operator, (MonkeyInteger) left, (MonkeyInteger) right);
        }
        return switch (operator) {
            case "==" -> nativeBoolToMonkeyBoolean(left == right);
            case "!=" -> nativeBoolToMonkeyBoolean(left != right);
            default -> NULL;
        };
    }

    private MonkeyObject evalIntegerInfixExpression(String operator, MonkeyInteger left, MonkeyInteger right) {
        return switch (operator) {
            case "+" -> new MonkeyInteger(left.value() + right.value());
            case "-" -> new MonkeyInteger(left.value() - right.value());
            case "*" -> new MonkeyInteger(left.value() * right.value());
            case "/" -> new MonkeyInteger(left.value() / right.value());

            case "<" -> nativeBoolToMonkeyBoolean(left.value() < right.value());
            case ">" -> nativeBoolToMonkeyBoolean(left.value() > right.value());
            case "==" -> nativeBoolToMonkeyBoolean(left.value() == right.value());
            case "!=" -> nativeBoolToMonkeyBoolean(left.value() != right.value());
            default -> NULL;
        };
    }

    private MonkeyBoolean nativeBoolToMonkeyBoolean(boolean bool) {
        return bool ? TRUE : FALSE;
    }
}
