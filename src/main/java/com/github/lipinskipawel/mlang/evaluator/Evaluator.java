package com.github.lipinskipawel.mlang.evaluator;

import com.github.lipinskipawel.mlang.ast.Node;
import com.github.lipinskipawel.mlang.ast.Program;
import com.github.lipinskipawel.mlang.ast.expression.BooleanExpression;
import com.github.lipinskipawel.mlang.ast.expression.CallExpression;
import com.github.lipinskipawel.mlang.ast.expression.Expression;
import com.github.lipinskipawel.mlang.ast.expression.FunctionLiteral;
import com.github.lipinskipawel.mlang.ast.expression.Identifier;
import com.github.lipinskipawel.mlang.ast.expression.IfExpression;
import com.github.lipinskipawel.mlang.ast.expression.InfixExpression;
import com.github.lipinskipawel.mlang.ast.expression.IntegerLiteral;
import com.github.lipinskipawel.mlang.ast.expression.PrefixExpression;
import com.github.lipinskipawel.mlang.ast.statement.BlockStatement;
import com.github.lipinskipawel.mlang.ast.statement.ExpressionStatement;
import com.github.lipinskipawel.mlang.ast.statement.LetStatement;
import com.github.lipinskipawel.mlang.ast.statement.ReturnStatement;
import com.github.lipinskipawel.mlang.ast.statement.Statement;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBoolean;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyError;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyFunction;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyNull;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.evaluator.objects.ReturnValue;

import java.util.ArrayList;
import java.util.List;

import static com.github.lipinskipawel.mlang.evaluator.Environment.newEnclosedEnvironment;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.ERROR_OBJ;
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

    public MonkeyObject eval(Node node, Environment environment) {
        return switch (node) {
            // statements
            case Program program -> evalProgram(program.programStatements(), environment);
            case BlockStatement block -> evalBlockStatements(block, environment);
            case ExpressionStatement expressionStatement -> eval(expressionStatement.expression(), environment);
            case ReturnStatement returnStatement -> {
                final var returned = eval(returnStatement.returnValue(), environment);
                if (isError(returned)) {
                    yield returned;
                }
                yield new ReturnValue(returned);
            }
            case LetStatement letStatement -> {
                var let = eval(letStatement.value(), environment);
                if (isError(let)) {
                    yield let;
                }
                yield environment.set(letStatement.name().value(), let);
            }

            // expressions
            case IntegerLiteral integer -> new MonkeyInteger(integer.value());
            case BooleanExpression bool -> nativeBoolToMonkeyBoolean(bool.value());
            case PrefixExpression prefix -> {
                final var right = eval(prefix.right(), environment);
                if (isError(right)) {
                    yield right;
                }
                yield evalPrefixExpression(prefix.operator(), right);
            }
            case InfixExpression infix -> {
                final var left = eval(infix.left(), environment);
                if (isError(left)) {
                    yield left;
                }
                final var right = eval(infix.right(), environment);
                if (isError(right)) {
                    yield right;
                }
                yield evalInfixExpression(infix.operator(), left, right);
            }
            case IfExpression ifExpression -> evalIfExpression(ifExpression, environment);
            case Identifier identifier -> evalIdentifier(identifier, environment);
            case FunctionLiteral fn -> {
                var params = fn.parameters();
                var body = fn.body();
                yield new MonkeyFunction(params, body, environment);
            }
            case CallExpression callExpression -> {
                var function = eval(callExpression.function(), environment);
                if (isError(function)) {
                    yield function;
                }
                var args = evalExpressions(callExpression.arguments(), environment);
                if (args.size() == 1 && isError(args.get(0))) {
                    yield args.get(0);

                }
                yield applyFunction(function, args);
            }
            default -> null;
        };
    }

    private boolean isError(MonkeyObject object) {
        if (object != null) {
            return object.type() == ERROR_OBJ;
        }
        return false;
    }

    private MonkeyObject evalProgram(List<Statement> statements, Environment environment) {
        MonkeyObject result = null;

        for (var statement : statements) {
            result = eval(statement, environment);
            if (result instanceof ReturnValue returnValue) {
                return returnValue.value();
            }
            if (result instanceof MonkeyError) {
                return result;
            }
        }

        return result;
    }

    private MonkeyObject evalBlockStatements(BlockStatement block, Environment environment) {
        MonkeyObject result = null;

        for (var statement : block.statements()) {
            result = eval(statement, environment);
            if (result != null) {
                final var type = result.type();
                if (type == RETURN_VALUE_OBJ || type == ERROR_OBJ) {
                    return result;
                }
            }
        }

        return result;
    }

    private MonkeyObject evalIfExpression(IfExpression ifExpression, Environment environment) {
        final var conditional = eval(ifExpression.condition(), environment);

        if (isTruthy(conditional)) {
            return eval(ifExpression.consequence(), environment);
        } else if (ifExpression.alternative() != null) {
            return eval(ifExpression.alternative(), environment);
        } else {
            return NULL;
        }
    }

    private MonkeyObject evalIdentifier(Identifier identifier, Environment environment) {
        final var value = environment.get(identifier.value());
        if (value == null) {
            return newError("identifier not found: " + identifier.value());
        }
        return value;
    }

    private List<MonkeyObject> evalExpressions(List<Expression> arguments, Environment environment) {
        var result = new ArrayList<MonkeyObject>();

        for (var arg : arguments) {
            var evaluated = eval(arg, environment);
            if (isError(evaluated)) {
                return List.of(evaluated);
            }
            result.add(evaluated);
        }

        return result;
    }

    private MonkeyObject applyFunction(MonkeyObject fn, List<MonkeyObject> arguments) {
        if (!(fn instanceof MonkeyFunction function)) {
            return newError("not a function: %s".formatted(fn.type()));
        }

        final var extendedEnv = extendFunctionEnv(function, arguments);
        final var evaluated = eval(function.block(), extendedEnv);
        return unwrapReturnValue(evaluated);
    }

    private Environment extendFunctionEnv(MonkeyFunction function, List<MonkeyObject> arguments) {
        final var env = newEnclosedEnvironment(function.environment());
        for (var i = 0; i < function.parameters().size(); i++) {
            env.set(function.parameters().get(i).value(), arguments.get(i));
        }
        return env;
    }

    private MonkeyObject unwrapReturnValue(MonkeyObject object) {
        if (object instanceof ReturnValue value) {
            return value.value();
        }
        return object;
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
            default -> newError("unknown operator: %s%s", operator, right.type());
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
            return newError("unknown operator: -%s", right.type());
        }
        return new MonkeyInteger(-((MonkeyInteger) (right)).value());
    }

    private MonkeyObject evalInfixExpression(String operator, MonkeyObject left, MonkeyObject right) {
        if (left.type() == INTEGER_OBJ && right.type() == INTEGER_OBJ) {
            return evalIntegerInfixExpression(operator, (MonkeyInteger) left, (MonkeyInteger) right);
        }
        if (left.type() != right.type()) {
            return newError("type mismatch: %s %s %s", left.type(), operator, right.type());
        }
        return switch (operator) {
            case "==" -> nativeBoolToMonkeyBoolean(left == right);
            case "!=" -> nativeBoolToMonkeyBoolean(left != right);
            default -> newError("unknown operator: %s %s %s", left.type(), operator, right.type());
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
            default -> newError("unknown operator: %s %s %s");
        };
    }

    private MonkeyBoolean nativeBoolToMonkeyBoolean(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    private MonkeyError newError(String message, Object... object) {
        return new MonkeyError(message.formatted(object));
    }
}
