package com.github.lipinskipawel.mlang.evaluator;

import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;

import java.util.HashMap;
import java.util.Map;

public final class Environment {
    private final Map<String, MonkeyObject> bindings;
    private Environment outer;

    public Environment() {
        this.bindings = new HashMap<>();
        this.outer = null;
    }

    public static Environment newEnclosedEnvironment(Environment outer) {
        final var environment = new Environment();
        environment.outer = outer;
        return environment;
    }

    MonkeyObject get(String name) {
        final var inner = bindings.get(name);
        if (inner == null && outer != null) {
            return outer.get(name);
        }
        return inner;
    }

    MonkeyObject set(String name, MonkeyObject monkeyObject) {
        bindings.put(name, monkeyObject);
        return monkeyObject;
    }
}
