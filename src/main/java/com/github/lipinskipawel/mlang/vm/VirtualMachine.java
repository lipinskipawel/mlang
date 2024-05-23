package com.github.lipinskipawel.mlang.vm;

import com.github.lipinskipawel.mlang.code.Instructions;
import com.github.lipinskipawel.mlang.compiler.Bytecode;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import static com.github.lipinskipawel.mlang.code.OpCode.opCode;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.util.Optional.empty;

public final class VirtualMachine {
    private static final int STACK_SIZE = 2048;

    private final List<MonkeyObject> constants;
    private final Instructions instructions;
    private final MonkeyObject[] stack; // we can define limit on the queue but can't in Stack
    private int stackPointer = 0;

    private VirtualMachine(List<MonkeyObject> constants, Instructions instructions, MonkeyObject[] stack) {
        this.constants = constants;
        this.instructions = instructions;
        this.stack = stack;
    }

    public static VirtualMachine virtualMachine(Bytecode bytecode) {
        return new VirtualMachine(bytecode.constants(), bytecode.instructions(), new MonkeyObject[STACK_SIZE]);
    }

    // fetch-decode-execute cycle
    public Optional<Object> run() {
        for (var instructionPointer = 0; instructionPointer < instructions.bytes().length; instructionPointer++) {
            // we are in the hot path, this static `opCode` function probably should be replaced by raw byte
            final var op = opCode(instructions.bytes()[instructionPointer]);
            switch (op) {
                case OP_CONSTANT -> {
                    final var constIndex = readShort(instructions.slice(instructionPointer + 1, instructions.bytes().length));
                    instructionPointer += 2;
                    final var error = push(constants.get(constIndex));
                    if (error.isPresent()) {
                        return error;
                    }
                }
                case OP_ADD -> {
                    final var right = pop();
                    final var left = pop();
                    final var leftValue = ((MonkeyInteger) left).value();
                    final var rightValue = ((MonkeyInteger) right).value();

                    final var result = leftValue + rightValue;
                    push(new MonkeyInteger(result));
                }
                case OP_POP -> pop();
            }
        }

        return empty();
    }

    public MonkeyObject lastPoppedStackElement() {
        return stack[stackPointer];
    }

    private Optional<Object> push(MonkeyObject object) {
        if (stackPointer >= STACK_SIZE) {
            return Optional.of("stack overflow");
        }
        stack[stackPointer] = object;
        stackPointer++;
        return empty();
    }

    private MonkeyObject pop() {
        final var object = stack[stackPointer - 1];
        stackPointer--;
        // explicitly not null'ing last element. Just for the book and tests. see usage of lastPoppedStackElement()
        return object;
    }

    private short readShort(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(BIG_ENDIAN).getShort();
    }
}
