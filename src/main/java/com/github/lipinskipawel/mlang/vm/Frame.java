package com.github.lipinskipawel.mlang.vm;

import com.github.lipinskipawel.mlang.code.Instructions;
import com.github.lipinskipawel.mlang.evaluator.objects.Closure;

final class Frame {

    private int instructionPointer; // instruction pointer in this frame
    private int basePointer; // ip before we execute the function call
    public final Closure closure;

    private Frame(Closure closure, int instructionPointer, int basePointer) {
        this.closure = closure;
        this.instructionPointer = instructionPointer;
        this.basePointer = basePointer;
    }

    public static Frame frame(Closure fn, int basePointer) {
        return new Frame(fn, -1, basePointer);
    }

    Instructions instructions() {
        return closure.fn.instructions();
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

    int basePointer() {
        return basePointer;
    }
}
