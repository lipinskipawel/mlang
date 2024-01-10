package com.github.lipinskipawel.mlang.evaluator.builtin;

import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;

import java.util.List;

interface BuiltinFunctionDefinition {

    MonkeyObject builtin(List<MonkeyObject> objects);
}
