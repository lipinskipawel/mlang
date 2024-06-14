package com.github.lipinskipawel.mlang.compiler;

import com.github.lipinskipawel.mlang.code.Instructions;

import static com.github.lipinskipawel.mlang.code.Instructions.noInstructions;

public final class CompilationScope {
    private final Instructions instructions;
    private EmittedInstructions lastInstruction;
    private EmittedInstructions previousInstruction;

    public CompilationScope() {
        this.instructions = noInstructions();
    }

    private CompilationScope(Instructions instructions, EmittedInstructions last, EmittedInstructions previous) {
        this.instructions = instructions;
        this.lastInstruction = last;
        this.previousInstruction = previous;
    }

    public CompilationScope withInstructions(Instructions instructions) {
        return new CompilationScope(instructions, lastInstruction, previousInstruction);
    }

    public CompilationScope withPreviousInstruction(EmittedInstructions previousInstruction) {
        return new CompilationScope(instructions, lastInstruction, previousInstruction);
    }

    public CompilationScope withLastInstruction(EmittedInstructions lastInstruction) {
        return new CompilationScope(instructions, lastInstruction, previousInstruction);
    }

    public Instructions instructions() {
        return instructions;
    }

    public EmittedInstructions lastInstruction() {
        return lastInstruction;
    }

    public EmittedInstructions previousInstruction() {
        return previousInstruction;
    }
}
