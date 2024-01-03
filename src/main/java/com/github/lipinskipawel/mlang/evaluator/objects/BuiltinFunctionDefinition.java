package com.github.lipinskipawel.mlang.evaluator.objects;

import java.util.List;

public interface BuiltinFunctionDefinition {

    MonkeyObject builtin(List<MonkeyObject> objects);
}
