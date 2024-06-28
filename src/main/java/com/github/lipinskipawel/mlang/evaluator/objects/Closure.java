package com.github.lipinskipawel.mlang.evaluator.objects;

import java.util.Arrays;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.CLOSURE_OBJ;
import static java.util.Objects.requireNonNull;

public final class Closure extends MonkeyObject {
    public final CompilerFunction fn;
    private MonkeyObject[] freeVariables;

    public Closure(CompilerFunction fn) {
        this.fn = requireNonNull(fn);
    }

    @Override
    public ObjectType type() {
        return CLOSURE_OBJ;
    }

    @Override
    public String inspect() {
        return "Closure{" + "fn=" + fn + ", freeVariables=" + Arrays.toString(freeVariables) + '}';
    }
}
