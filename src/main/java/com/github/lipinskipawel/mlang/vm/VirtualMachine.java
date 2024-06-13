package com.github.lipinskipawel.mlang.vm;

import com.github.lipinskipawel.mlang.code.Instructions;
import com.github.lipinskipawel.mlang.code.OpCode;
import com.github.lipinskipawel.mlang.compiler.Bytecode;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyArray;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBoolean;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyNull;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyString;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import static com.github.lipinskipawel.mlang.code.OpCode.OP_ADD;
import static com.github.lipinskipawel.mlang.code.OpCode.opCode;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.INTEGER_OBJ;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.STRING_OBJ;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Stream.iterate;

public final class VirtualMachine {
    public static final int GLOBAL_SIZE = 65536;
    static final MonkeyNull NULL = new MonkeyNull();
    private static final int STACK_SIZE = 2048;
    private static final MonkeyBoolean TRUE = new MonkeyBoolean(true);
    private static final MonkeyBoolean FALSE = new MonkeyBoolean(false);

    private final List<MonkeyObject> constants;
    private final Instructions instructions;
    private final MonkeyObject[] stack; // we can define limit on the queue but can't in Stack
    private int stackPointer = 0;
    private final MonkeyObject[] globals;

    private VirtualMachine(
            List<MonkeyObject> constants,
            Instructions instructions,
            MonkeyObject[] stack,
            MonkeyObject[] globals
    ) {
        this.constants = constants;
        this.instructions = instructions;
        this.stack = stack;
        this.globals = globals;
    }

    public static VirtualMachine virtualMachine(Bytecode bytecode) {
        return virtualMachine(bytecode, new MonkeyObject[GLOBAL_SIZE]);
    }

    public static VirtualMachine virtualMachine(Bytecode bytecode, MonkeyObject[] globals) {
        return new VirtualMachine(bytecode.constants(), bytecode.instructions(), new MonkeyObject[STACK_SIZE], globals);
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
                case OP_ADD, OP_SUB, OP_MUL, OP_DIV -> {
                    final var error = executeBinaryOperation(op);
                    if (error.isPresent()) {
                        return error;
                    }
                }
                case OP_POP -> pop();
                case OP_TRUE -> {
                    final var error = push(TRUE);
                    if (error.isPresent()) {
                        return error;
                    }
                }
                case OP_FALSE -> {
                    final var error = push(FALSE);
                    if (error.isPresent()) {
                        return error;
                    }
                }
                case OP_EQUAL, OP_NOT_EQUAL, OP_GREATER_THAN -> {
                    final var error = executeComparison(op);
                    if (error.isPresent()) {
                        return error;
                    }
                }
                case OP_BANG -> {
                    final var error = executeBangOperator();
                    if (error.isPresent()) {
                        return error;
                    }
                }
                case OP_MINUS -> {
                    final var error = executeMinusOperator();
                    if (error.isPresent()) {
                        return error;
                    }
                }
                case OP_JUMP -> {
                    final var pos = readShort(instructions.slice(instructionPointer + 1, instructions.bytes().length));
                    instructionPointer = pos - 1;
                }
                case OP_JUMP_NOT_TRUTHY -> {
                    final var pos = readShort(instructions.slice(instructionPointer + 1, instructions.bytes().length));
                    instructionPointer += 2;

                    final var condition = pop();
                    if (!isTruthy(condition)) {
                        instructionPointer = pos - 1;
                    }
                }
                case OP_NULL -> {
                    final var error = push(NULL);
                    if (error.isPresent()) {
                        return error;
                    }
                }
                case OP_SET_GLOBAL -> {
                    final var globalIndex = readShort(instructions.slice(instructionPointer + 1, instructions.bytes().length));
                    instructionPointer += 2;

                    globals[globalIndex] = pop();
                }
                case OP_GET_GLOBAL -> {
                    final var globalIndex = readShort(instructions.slice(instructionPointer + 1, instructions.bytes().length));
                    instructionPointer += 2;

                    final var error = push(globals[globalIndex]);
                    if (error.isPresent()) {
                        return error;
                    }
                }
                case OP_ARRAY -> {
                    final var arrayLength = readShort(instructions.slice(instructionPointer + 1, instructions.bytes().length));
                    instructionPointer += 2;

                    final var error = push(new MonkeyArray(iterate(1, i -> i <= arrayLength, i -> i + 1)
                            .map(it -> pop())
                            .toList()
                            .reversed()));
                    if (error.isPresent()) {
                        return error;
                    }
                }
            }
        }

        return empty();
    }

    private Optional<Object> executeBinaryOperation(OpCode op) {
        final var right = pop();
        final var left = pop();

        final var leftType = left.type();
        final var rightType = right.type();

        if (leftType == INTEGER_OBJ && rightType == INTEGER_OBJ) {
            return executeBinaryInteger(op, left, right);
        }

        if (leftType == STRING_OBJ && rightType == STRING_OBJ) {
            return executeBinaryString(op, left, right);
        }

        return of("unsupported types for binary operation: %s %s".formatted(leftType, rightType));
    }

    private Optional<Object> executeBinaryInteger(OpCode op, MonkeyObject left, MonkeyObject right) {
        final var leftValue = ((MonkeyInteger) left).value();
        final var rightValue = ((MonkeyInteger) right).value();

        final var result = switch (op) {
            case OP_ADD -> leftValue + rightValue;
            case OP_SUB -> leftValue - rightValue;
            case OP_MUL -> leftValue * rightValue;
            case OP_DIV -> leftValue / rightValue;
            default -> of("unknown integer operation [%s]".formatted(op));
        };
        if (result instanceof Optional<?> error) {
            return of(error.get());
        }
        return push(new MonkeyInteger((int) result));
    }

    private Optional<Object> executeBinaryString(OpCode op, MonkeyObject left, MonkeyObject right) {
        if (op != OP_ADD) {
            return of("unknown string operator [%s]".formatted(op));
        }

        final var leftValue = ((MonkeyString) left).value();
        final var rightValue = ((MonkeyString) right).value();

        return push(new MonkeyString(leftValue + rightValue));
    }

    private Optional<Object> executeComparison(OpCode op) {
        final var right = pop();
        final var left = pop();

        if (left.type() == INTEGER_OBJ && right.type() == INTEGER_OBJ) {
            return executeIntegerComparison(op, left, right);
        }

        final var result = switch (op) {
            case OP_EQUAL -> push(nativeBoolToBooleanObject(right == left));
            case OP_NOT_EQUAL -> push(nativeBoolToBooleanObject(right != left));
            default -> of("unknown integer operation [%s] (%s %s)".formatted(op, left.type(), right.type()));
        };
        if (result instanceof Optional<?> error && error.isPresent()) {
            return of(error.get());
        }
        return empty();
    }

    private Optional<Object> executeIntegerComparison(OpCode op, MonkeyObject left, MonkeyObject right) {
        final var leftValue = ((MonkeyInteger) left).value();
        final var rightValue = ((MonkeyInteger) right).value();

        final var result = switch (op) {
            case OP_EQUAL -> push(nativeBoolToBooleanObject(rightValue == leftValue));
            case OP_NOT_EQUAL -> push(nativeBoolToBooleanObject(rightValue != leftValue));
            case OP_GREATER_THAN -> push(nativeBoolToBooleanObject(leftValue > rightValue));
            default -> of("unknown operator [%s]".formatted(op));
        };

        if (result instanceof Optional<?> error && error.isPresent()) {
            return of(error.get());
        }

        return empty();
    }

    private MonkeyBoolean nativeBoolToBooleanObject(boolean input) {
        return input ? TRUE : FALSE;
    }

    private Optional<Object> executeBangOperator() {
        final var operand = pop();

        switch (operand) {
            case MonkeyBoolean monkeyBoolean -> {
                if (monkeyBoolean.value()) {
                    push(FALSE);
                } else {
                    push(TRUE);
                }
            }
            case MonkeyNull monkeyNull -> push(TRUE);
            default -> push(FALSE);
        }
        return empty();
    }

    private Optional<Object> executeMinusOperator() {
        final var operand = pop();

        if (operand.type() != INTEGER_OBJ) {
            return of("unsupported type for negation: %s".formatted(operand.type()));
        }

        final var value = ((MonkeyInteger) operand).value();
        return push(new MonkeyInteger(-value));
    }

    public MonkeyObject lastPoppedStackElement() {
        return stack[stackPointer];
    }

    private boolean isTruthy(MonkeyObject object) {
        return switch (object.type()) {
            case BOOLEAN_OBJ -> ((MonkeyBoolean) object).value();
            case NULL_OBJ -> false;
            default -> true;
        };
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
