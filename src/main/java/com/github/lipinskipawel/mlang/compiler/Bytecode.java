package com.github.lipinskipawel.mlang.compiler;

import com.github.lipinskipawel.mlang.code.Instructions;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;

import java.util.List;

public record Bytecode(Instructions instructions, List<MonkeyObject> constants) {
}
