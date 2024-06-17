package com.github.lipinskipawel.mlang.vm;

import com.github.lipinskipawel.mlang.code.Instructions;
import com.github.lipinskipawel.mlang.evaluator.objects.CompilerFunction;

final class Frame {

    private final CompilerFunction fn;
    private int instructionPointer; // instruction pointer in this frame

    private Frame(CompilerFunction fn, int instructionPointer) {
        this.fn = fn;
        this.instructionPointer = instructionPointer;
    }

    public static Frame frame(CompilerFunction fn) {
        return new Frame(fn, -1);
    }

    Instructions instructions() {
        return fn.instructions();
    }

    int instructionPointer() {
        return instructionPointer;
    }

    void incrementInstructionPointer() {
        instructionPointer++;
    }

    void incrementInstructionPointer(int by) {
        instructionPointer = instructionPointer + by;
    }

    void setInstructionPointer(int newInstructionPointer) {
        instructionPointer = newInstructionPointer;
    }
}
