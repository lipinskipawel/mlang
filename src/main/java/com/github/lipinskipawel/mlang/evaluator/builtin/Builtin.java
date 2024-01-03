package com.github.lipinskipawel.mlang.evaluator.builtin;

import com.github.lipinskipawel.mlang.evaluator.objects.BuiltinFunctionDefinition;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBuiltin;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyString;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.lipinskipawel.mlang.evaluator.Evaluator.newError;
import static java.util.Optional.ofNullable;

public final class Builtin {

    private static final Map<String, MonkeyBuiltin> builtins = Map.of(
            "len", abstractMonkeyBuiltin(objects -> {
                if (objects.size() != 1) {
                    return newError("wrong number of arguments. got=%d, want=1", objects.size());
                }
                return switch (objects.get(0).type()) {
                    case STRING_OBJ -> new MonkeyInteger(((MonkeyString) objects.get(0)).value().length());
                    default -> newError("argument to [len] not supported, got %s", objects.get(0).type());
                };
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
