package com.github.lipinskipawel.mlang.compiler;

import com.github.lipinskipawel.mlang.code.Instructions;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;

public record Bytecode(Instructions instructions, MonkeyObject[] constants) {
}
