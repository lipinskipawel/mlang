package com.github.lipinskipawel.mlang.evaluator;

import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;

import java.util.HashMap;
import java.util.Map;

public final class Environment {
    private final Map<String, MonkeyObject> bindings;

    public Environment() {
        this.bindings = new HashMap<>();
    }

    MonkeyObject get(String name) {
        return bindings.get(name);
    }

    MonkeyObject set(String name, MonkeyObject monkeyObject) {
        bindings.put(name, monkeyObject);
        return monkeyObject;
    }
}
