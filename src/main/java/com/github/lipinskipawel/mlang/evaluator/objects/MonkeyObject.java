package com.github.lipinskipawel.mlang.evaluator.objects;

abstract class MonkeyObject {
    public abstract ObjectType type();

    public abstract String inspect();
}
