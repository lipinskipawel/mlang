package com.github.lipinskipawel.mlang.evaluator;

import com.github.lipinskipawel.mlang.evaluator.objects.Hashable;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyArray;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBoolean;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBuiltin;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyError;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyFunction;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyHash;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyNull;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyString;
import com.github.lipinskipawel.mlang.evaluator.objects.ReturnValue;
import com.github.lipinskipawel.mlang.parser.ast.Node;
import com.github.lipinskipawel.mlang.parser.ast.Program;
import com.github.lipinskipawel.mlang.parser.ast.expression.ArrayLiteral;
import com.github.lipinskipawel.mlang.parser.ast.expression.BooleanExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.CallExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.Expression;
import com.github.lipinskipawel.mlang.parser.ast.expression.FunctionLiteral;
import com.github.lipinskipawel.mlang.parser.ast.expression.HashLiteral;
import com.github.lipinskipawel.mlang.parser.ast.expression.Identifier;
import com.github.lipinskipawel.mlang.parser.ast.expression.IfExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.IndexExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.InfixExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.IntegerLiteral;
import com.github.lipinskipawel.mlang.parser.ast.expression.PrefixExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.StringLiteral;
import com.github.lipinskipawel.mlang.parser.ast.statement.BlockStatement;
import com.github.lipinskipawel.mlang.parser.ast.statement.ExpressionStatement;
import com.github.lipinskipawel.mlang.parser.ast.statement.LetStatement;
import com.github.lipinskipawel.mlang.parser.ast.statement.ReturnStatement;
import com.github.lipinskipawel.mlang.parser.ast.statement.Statement;

import java.util.ArrayList;
import java.util.List;

import static com.github.lipinskipawel.mlang.evaluator.Environment.newEnclosedEnvironment;
import static com.github.lipinskipawel.mlang.evaluator.builtin.Builtin.findBuiltIn;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.ARRAY_OBJ;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.ERROR_OBJ;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.HASH_OBJ;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.INTEGER_OBJ;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.RETURN_VALUE_OBJ;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.STRING_OBJ;

public final class Evaluator {
    public static final MonkeyNull NULL = new MonkeyNull();

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
                final var let = eval(letStatement.value(), environment);
                if (isError(let)) {
                    yield let;
                }
                yield environment.set(letStatement.name().value(), let);
            }

            // expressions
            case IntegerLiteral integer -> new MonkeyInteger(integer.value());
            case StringLiteral string -> new MonkeyString(string.value());
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
                final var params = fn.parameters();
                final var body = fn.body();
                yield new MonkeyFunction(params, body, environment);
            }
            case CallExpression callExpression -> {
                final var function = eval(callExpression.function(), environment);
                if (isError(function)) {
                    yield function;
                }
                final var args = evalExpressions(callExpression.arguments(), environment);
                if (args.size() == 1 && isError(args.get(0))) {
                    yield args.get(0);

                }
                yield applyFunction(function, args);
            }
            case ArrayLiteral arrayLiteral -> {
                final var elements = evalExpressions(arrayLiteral.elements(), environment);
                if (elements.size() == 1 && isError(elements.get(0))) {
                    yield elements.get(0);
                }
                yield new MonkeyArray(elements);
            }
            case IndexExpression indexExpression -> {
                final var identifier = eval(indexExpression.left(), environment);
                if (isError(identifier)) {
                    yield identifier;
                }
                final var index = eval(indexExpression.index(), environment);
                if (isError(index)) {
                    yield index;
                }
                yield evalIndexExpression(identifier, index);
            }
            case HashLiteral hashLiteral -> evalHashLiterals(hashLiteral, environment);
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
        if (value != null) {
            return value;
        }
        final var builtIn = findBuiltIn(identifier.value());
        if (builtIn.isPresent()) {
            return builtIn.get();
        }
        return newError("identifier not found: " + identifier.value());
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
        return switch (fn.type()) {
            case FUNCTION_OBJ -> {
                final var function = (MonkeyFunction) fn;
                final var extendedEnv = extendFunctionEnv(function, arguments);
                final var evaluated = eval(function.block(), extendedEnv);
                yield unwrapReturnValue(evaluated);
            }
            case BUILTIN_OBJ -> {
                final var result = ((MonkeyBuiltin) fn).builtin(arguments);
                if (result != null) {
                    yield result;
                }
                yield NULL;
            }
            default -> newError("not a function: %s".formatted(fn.type()));
        };
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

    private MonkeyObject evalIndexExpression(MonkeyObject identifier, MonkeyObject index) {
        if (identifier.type() == ARRAY_OBJ && index.type() == INTEGER_OBJ) {
            return evalArrayIndexExpression((MonkeyArray) identifier, (MonkeyInteger) index);
        }
        if (identifier.type() == HASH_OBJ) {
            return evalHashExpression((MonkeyHash) identifier, (MonkeyObject) index);
        }
        return newError("index operator not supported: %s", identifier.type());
    }

    private MonkeyObject evalArrayIndexExpression(MonkeyArray left, MonkeyInteger index) {
        final var max = left.elements().size() - 1;
        final var idx = index.value();

        if (idx < 0 || idx > max) {
            return NULL;
        }

        return left.elements().get(idx);
    }

    private MonkeyObject evalHashExpression(MonkeyHash left, MonkeyObject key) {
        if (!(key instanceof Hashable)) {
            return newError("unusable as hash key: %s", key.type());
        }

        final var value = left.getHashPair(((Hashable) key).hashKey());
        if (value == null) {
            return NULL;
        }

        return value.value();
    }

    private MonkeyObject evalHashLiterals(HashLiteral hashLiteral, Environment environment) {
        final var monkeyHash1 = new MonkeyHash();

        for (var entry : hashLiteral.pairs().entrySet()) {
            final var key = eval(entry.getKey(), environment);
            if (isError(key)) {
                return key;
            }

            if (!(key instanceof Hashable)) {
                return newError("unusable as hash key: %s", key.type());
            }

            final var value = eval(entry.getValue(), environment);
            if (isError(value)) {
                return value;
            }
            monkeyHash1.put(key, value);
        }

        return monkeyHash1;
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
        if (left.type() == STRING_OBJ && right.type() == STRING_OBJ) {
            return evalStringInfixExpression(operator, (MonkeyString) left, (MonkeyString) right);
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

    private MonkeyObject evalStringInfixExpression(String operator, MonkeyString left, MonkeyString right) {
        if (!operator.equals("+")) {
            return newError("unknown operator: " + left.type() + " - " + right.type());
        }
        return new MonkeyString(left.value() + right.value());
    }

    private MonkeyBoolean nativeBoolToMonkeyBoolean(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    public static MonkeyError newError(String message, Object... object) {
        return new MonkeyError(message.formatted(object));
    }
}
