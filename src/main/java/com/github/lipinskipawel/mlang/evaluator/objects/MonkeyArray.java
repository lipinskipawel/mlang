package com.github.lipinskipawel.mlang.evaluator.objects;

import java.util.List;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.ARRAY_OBJ;
import static java.util.stream.Collectors.joining;

public final class MonkeyArray extends MonkeyObject {
    private final List<MonkeyObject> elements;

    public MonkeyArray(List<MonkeyObject> elements) {
        this.elements = elements;
    }

    @Override
    public ObjectType type() {
        return ARRAY_OBJ;
    }

    @Override
    public String inspect() {
        return "["
                + elements.stream()
                .map(MonkeyObject::inspect)
                .collect(joining(", "))
                + "]";
    }

    public List<MonkeyObject> elements() {
        return elements;
    }
}
