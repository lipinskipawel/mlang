package com.github.lipinskipawel.mlang.evaluator.builtin;

import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBuiltin;
import com.github.lipinskipawel.mlang.object.Builtins;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public final class Builtin {
    private static final Builtins builtins = new Builtins();

    private static final Map<String, MonkeyBuiltin> builtinsMap = Map.of(
            "len", builtins.findBuiltIn("len").orElseThrow(),
            "first", builtins.findBuiltIn("first").orElseThrow(),
            "last", builtins.findBuiltIn("last").orElseThrow(),
            "rest", builtins.findBuiltIn("rest").orElseThrow(),
            "push", builtins.findBuiltIn("push").orElseThrow(),
            "puts", builtins.findBuiltIn("puts").orElseThrow()
    );

    public static Optional<MonkeyBuiltin> findBuiltIn(String builtInFunction) {
        return ofNullable(builtinsMap.get(builtInFunction));
    }
}
