package com.github.lipinskipawel.mlang.evaluator.builtin;

import com.github.lipinskipawel.mlang.evaluator.objects.BuiltinFunctionDefinition;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyArray;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBuiltin;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.lipinskipawel.mlang.evaluator.Evaluator.NULL;
import static com.github.lipinskipawel.mlang.evaluator.Evaluator.newError;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.ARRAY_OBJ;
import static java.util.Optional.ofNullable;

public final class Builtin {

    private static final Map<String, MonkeyBuiltin> builtins = Map.of(
            "len", abstractMonkeyBuiltin(objects -> {
                if (objects.size() != 1) {
                    return newError("wrong number of arguments. got=%d, want=1", objects.size());
                }
                return switch (objects.get(0).type()) {
                    case STRING_OBJ -> new MonkeyInteger(((MonkeyString) objects.get(0)).value().length());
                    case ARRAY_OBJ -> {
                        final var array = (MonkeyArray) objects.get(0);
                        yield new MonkeyInteger(array.elements().size());
                    }
                    default -> newError("argument to [len] not supported, got %s", objects.get(0).type());
                };
            }),
            "first", abstractMonkeyBuiltin(objects -> {
                if (objects.size() != 1) {
                    return newError("wrong number of arguments. got=%d, want=1", objects.size());
                }
                if (objects.get(0).type() != ARRAY_OBJ) {
                    return newError("argument to 'first' must be ARRAY, got %s", objects.get(0).type());
                }
                final var array = (MonkeyArray) objects.get(0);
                if (!array.elements().isEmpty()) {
                    return array.elements().get(0);
                }
                return NULL;
            }),
            "last", abstractMonkeyBuiltin(objects -> {
                if (objects.size() != 1) {
                    return newError("wrong number of arguments. got=%d, want=1", objects.size());
                }
                if (objects.get(0).type() != ARRAY_OBJ) {
                    return newError("argument to 'last' must be ARRAY, got %s", objects.get(0).type());
                }
                final var array = (MonkeyArray) objects.get(0);
                final var length = array.elements().size();
                if (length > 0) {
                    return array.elements().get(length - 1);
                }
                return NULL;
            }),
            "rest", abstractMonkeyBuiltin(objects -> {
                if (objects.size() != 1) {
                    return newError("wrong number of arguments. got=%d, want=1", objects.size());
                }
                if (objects.get(0).type() != ARRAY_OBJ) {
                    return newError("argument to 'rest' must be ARRAY, got %s", objects.get(0).type());
                }
                final var array = (MonkeyArray) objects.get(0);
                final var length = array.elements().size();
                if (length > 0) {
                    final var copy = new ArrayList<>(array.elements());
                    copy.remove(0);
                    return new MonkeyArray(copy);
                }
                return NULL;
            }),
            "push", abstractMonkeyBuiltin(objects -> {
                if (objects.size() != 2) {
                    return newError("wrong number of arguments. got=%d, want=2", objects.size());
                }
                if (objects.get(0).type() != ARRAY_OBJ) {
                    return newError("argument to 'push' must be ARRAY, got %s", objects.get(0).type());
                }
                final var array = (MonkeyArray) objects.get(0);
                final var copy = new ArrayList<>(array.elements());
                copy.add(objects.get(1));
                return new MonkeyArray(copy);
            })
    );

    public static Optional<MonkeyBuiltin> findBuiltIn(String builtInFunction) {
        return ofNullable(builtins.get(builtInFunction));
    }

    private static MonkeyBuiltin abstractMonkeyBuiltin(BuiltinFunctionDefinition func) {
        return new MonkeyBuiltin() {
            @Override
            public MonkeyObject builtin(List<MonkeyObject> objects) {
                return func.builtin(objects);
            }
        };
    }
}
