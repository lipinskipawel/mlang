package com.github.lipinskipawel.mlang.evaluator.builtin;

import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBuiltin;

import java.util.Map;
import java.util.Optional;

import static com.github.lipinskipawel.mlang.object.Builtins.builtins;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

public final class Builtin {
    private static final Map<String, MonkeyBuiltin> builtins = builtins()
            .stream()
            .map(it -> Map.entry(it.name(), it.builtin()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

    public static Optional<MonkeyBuiltin> findBuiltIn(String builtInFunction) {
        return ofNullable(builtins.get(builtInFunction));
    }
}
