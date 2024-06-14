package com.github.lipinskipawel.mlang.evaluator.objects;

import com.github.lipinskipawel.mlang.code.Instructions;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.COMPILED_FUNCTION_OBJ;

public final class CompilerFunction extends MonkeyObject {
    private final Instructions instructions;

    public CompilerFunction(Instructions instructions) {
        this.instructions = instructions;
    }

    public Instructions instructions() {
        return instructions;
    }

    @Override
    public ObjectType type() {
        return COMPILED_FUNCTION_OBJ;
    }

    @Override
    public String inspect() {
        return "CompiledFunction[%s]".formatted(instructions.toString());
    }
}
