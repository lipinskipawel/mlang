package com.github.lipinskipawel.mlang.evaluator.objects;

import com.github.lipinskipawel.mlang.code.Instructions;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.COMPILED_FUNCTION_OBJ;

public final class CompilerFunction extends MonkeyObject {
    private final Instructions instructions;
    private final int numberOfLocals;
    private final int numberOfParameters;

    private CompilerFunction(Instructions instructions, int numberOfLocals, int numberOfParameters) {
        this.instructions = instructions;
        this.numberOfLocals = numberOfLocals;
        this.numberOfParameters = numberOfParameters;
    }

    public static CompilerFunction compilerFunction(Instructions instructions) {
        return new CompilerFunction(instructions, 0, 0);
    }

    public static CompilerFunction compilerFunction(Instructions instructions, int numberOfLocals, int numberOfParameters) {
        return new CompilerFunction(instructions, numberOfLocals, numberOfParameters);
    }

    public Instructions instructions() {
        return instructions;
    }

    public int numberOfLocals() {
        return numberOfLocals;
    }

    public int numberOfParameters() {
        return numberOfParameters;
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
