package com.github.lipinskipawel.mlang.vm;

import com.github.lipinskipawel.mlang.compiler.Bytecode;
import com.github.lipinskipawel.mlang.evaluator.objects.Closure;
import com.github.lipinskipawel.mlang.evaluator.objects.CompilerFunction;
import com.github.lipinskipawel.mlang.evaluator.objects.Hashable;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyArray;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBoolean;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBuiltin;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyHash;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyNull;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyString;

import java.util.List;

import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_ADD;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_ARRAY;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_BANG;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_CALL;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_CLOSURE;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_CONSTANT;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_CURRENT_CLOSURE;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_DIV;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_EQUAL;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_FALSE;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_GET_BUILTIN;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_GET_FREE;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_GET_GLOBAL;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_GET_LOCAL;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_GREATER_THAN;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_HASH;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_INDEX;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_JUMP;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_JUMP_NOT_TRUTHY;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_MINUS;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_MUL;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_NOT_EQUAL;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_NULL;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_POP;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_RETURN;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_RETURN_VALUE;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_SET_GLOBAL;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_SET_LOCAL;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_SUB;
import static com.github.lipinskipawel.mlang.code.InstructionOpCodes.OP_TRUE;
import static com.github.lipinskipawel.mlang.evaluator.objects.CompilerFunction.compilerFunction;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.ARRAY_OBJ;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.HASH_OBJ;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.INTEGER_OBJ;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.STRING_OBJ;
import static com.github.lipinskipawel.mlang.object.Builtins.builtins;
import static com.github.lipinskipawel.mlang.vm.Frame.frame;
import static java.util.Arrays.asList;
import static java.util.stream.Stream.iterate;

public final class VirtualMachine {
    public static final int GLOBAL_SIZE = 65536;
    static final MonkeyNull NULL = new MonkeyNull();
    private static final int STACK_SIZE = 2048;
    private static final int MAX_FRAMES = 1024;
    private static final MonkeyBoolean TRUE = new MonkeyBoolean(true);
    private static final MonkeyBoolean FALSE = new MonkeyBoolean(false);

    private final List<MonkeyObject> constants;
    private final MonkeyObject[] stack; // we can define limit on the queue but can't in Stack
    private int stackPointer = 0;
    private final MonkeyObject[] globals;
    private final Frame[] frames;
    private int frameIndex;

    private VirtualMachine(
            Bytecode bytecode,
            MonkeyObject[] stack,
            MonkeyObject[] globals
    ) {
        this.constants = bytecode.constants();
        this.stack = stack;
        this.globals = globals;

        final var mainFn = compilerFunction(bytecode.instructions());
        final var mainClosure = new Closure(mainFn);
        final var mainFrame = frame(mainClosure, 0);

        this.frames = new Frame[MAX_FRAMES];
        this.frames[0] = mainFrame;
        this.frameIndex = 1;
    }

    public static VirtualMachine virtualMachine(Bytecode bytecode) {
        return virtualMachine(bytecode, new MonkeyObject[GLOBAL_SIZE]);
    }

    public static VirtualMachine virtualMachine(Bytecode bytecode, MonkeyObject[] globals) {
        return new VirtualMachine(bytecode, new MonkeyObject[STACK_SIZE], globals);
    }

    private Frame currentFrame() {
        return frames[frameIndex - 1];
    }

    private void pushFrame(Frame frame) {
        frames[frameIndex] = frame;
        frameIndex++;
    }

    private Frame popFrame() {
        frameIndex--;
        return frames[frameIndex];
    }

    // fetch-decode-execute cycle
    public void run() {
        while (currentFrame().instructionPointer() < currentFrame().instructions().length() - 1) {
            // we are in the hot path
            currentFrame().incrementInstructionPointer();

            final var instructionPointer = currentFrame().instructionPointer();
            final var instructions = currentFrame().instructions();
            // this static `opCode` function probably should be replaced by raw byte
            final var op = instructions.instructionAt(instructionPointer);

            switch (op) {
                case OP_CONSTANT -> {
                    final var constIndex = instructions.readShort(instructionPointer + 1);
                    currentFrame().incrementInstructionPointer(2);
                    push(constants.get(constIndex));
                }
                case OP_ADD, OP_SUB, OP_MUL, OP_DIV -> executeBinaryOperation(op);
                case OP_POP -> pop();
                case OP_TRUE -> push(TRUE);
                case OP_FALSE -> push(FALSE);
                case OP_EQUAL, OP_NOT_EQUAL, OP_GREATER_THAN -> executeComparison(op);
                case OP_BANG -> executeBangOperator();
                case OP_MINUS -> executeMinusOperator();
                case OP_JUMP -> {
                    final var pos = instructions.readShort(instructionPointer + 1);
                    currentFrame().setInstructionPointer(pos - 1);
                }
                case OP_JUMP_NOT_TRUTHY -> {
                    final var pos = instructions.readShort(instructionPointer + 1);
                    currentFrame().incrementInstructionPointer(2);

                    final var condition = pop();
                    if (!isTruthy(condition)) {
                        currentFrame().setInstructionPointer(pos - 1);
                    }
                }
                case OP_NULL -> push(NULL);
                case OP_SET_GLOBAL -> {
                    final var globalIndex = instructions.readShort(instructionPointer + 1);
                    currentFrame().incrementInstructionPointer(2);

                    globals[globalIndex] = pop();
                }
                case OP_GET_GLOBAL -> {
                    final var globalIndex = instructions.readShort(instructionPointer + 1);
                    currentFrame().incrementInstructionPointer(2);

                    push(globals[globalIndex]);
                }
                case OP_SET_LOCAL -> {
                    final var localIndex = instructions.instructionAt(instructionPointer + 1);
                    currentFrame().incrementInstructionPointer();

                    final var frame = currentFrame();

                    stack[frame.basePointer() + localIndex] = pop();
                }
                case OP_GET_LOCAL -> {
                    final var localIndex = instructions.instructionAt(instructionPointer + 1);
                    currentFrame().incrementInstructionPointer();

                    final var frame = currentFrame();

                    push(stack[frame.basePointer() + localIndex]);
                }
                case OP_GET_BUILTIN -> {
                    final var builtinIndex = instructions.instructionAt(instructionPointer + 1);
                    currentFrame().incrementInstructionPointer();

                    final var definition = builtins().get(builtinIndex);

                    push(definition.builtin());
                }
                case OP_ARRAY -> {
                    final var arrayLength = instructions.readShort(instructionPointer + 1);
                    currentFrame().incrementInstructionPointer(2);

                    push(new MonkeyArray(iterate(1, i -> i <= arrayLength, i -> i + 1)
                            .map(it -> pop())
                            .toList()
                            .reversed()));
                }
                case OP_HASH -> {
                    final var hashLength = instructions.readShort(instructionPointer + 1);
                    currentFrame().incrementInstructionPointer(2);

                    final var entries = iterate(1, i -> i <= hashLength, i -> i + 1)
                            .map(it -> pop())
                            .toList()
                            .reversed();
                    final var hash = new MonkeyHash();
                    for (var i = 0; i < hashLength; i = i + 2) {
                        hash.put(entries.get(i), entries.get(i + 1));
                    }

                    push(hash);
                }
                case OP_INDEX -> {
                    final var index = pop();
                    final var left = pop();

                    executeIndexExpression(left, index);
                }
                case OP_CALL -> {
                    final var numArgs = instructions.instructionAt(instructionPointer + 1);
                    currentFrame().incrementInstructionPointer();

                    executeCall(numArgs);
                }
                case OP_RETURN_VALUE -> {
                    final var returnValue = pop();

                    final var frame = popFrame();
                    stackPointer = frame.basePointer() - 1;

                    push(returnValue);
                }
                case OP_RETURN -> {
                    final var frame = popFrame();
                    stackPointer = frame.basePointer() - 1;

                    push(NULL);
                }
                case OP_CLOSURE -> {
                    final var constIndex = instructions.readShort(instructionPointer + 1);
                    final var numFree = instructions.instructionAt(instructionPointer + 3);
                    currentFrame().incrementInstructionPointer(3);

                    pushClosure(constIndex, numFree);
                }
                case OP_GET_FREE -> {
                    final var freeIndex = instructions.instructionAt(instructionPointer + 1);
                    currentFrame().incrementInstructionPointer();

                    final var currentClosure = currentFrame().closure;
                    push(currentClosure.freeVariables[freeIndex]);
                }
                case OP_CURRENT_CLOSURE -> {
                    final var currentClosure = currentFrame().closure;

                    push(currentClosure);
                }
            }
        }
    }

    private void executeCall(int numArgs) {
        final var callee = stack[stackPointer - 1 - numArgs];
        switch (callee.type()) {
            case CLOSURE_OBJ -> callClosure((Closure) callee, numArgs);
            case BUILTIN_OBJ -> callBuiltin((MonkeyBuiltin) callee, numArgs);
            default -> throw new RuntimeException("calling non-function and non-built-in");
        }
    }

    private void callClosure(Closure closure, int numArgs) {
        if (numArgs != closure.fn.numberOfParameters()) {
            throw new RuntimeException("wrong number of arguments want=%d, got=%d".formatted(closure.fn.numberOfParameters(), numArgs));
        }
        final var newFrame = frame(closure, stackPointer - numArgs);
        pushFrame(newFrame);

        stackPointer = newFrame.basePointer() + closure.fn.numberOfLocals();
    }

    private void callBuiltin(MonkeyBuiltin fn, int numArgs) {
        final var args = slice(stack, stackPointer - numArgs, stackPointer);

        final var result = fn.builtin(args);
        stackPointer = stackPointer - numArgs - 1;

        if (result != null) {
            push(result);
        } else {
            push(NULL);
        }
    }

    private List<MonkeyObject> slice(MonkeyObject[] slice, int start, int end) {
        return asList(slice).subList(start, end);
    }

    private void executeBinaryOperation(int op) {
        final var right = pop();
        final var left = pop();

        final var leftType = left.type();
        final var rightType = right.type();

        if (leftType == INTEGER_OBJ && rightType == INTEGER_OBJ) {
            executeBinaryInteger(op, left, right);
            return;
        }

        if (leftType == STRING_OBJ && rightType == STRING_OBJ) {
            executeBinaryString(op, left, right);
            return;
        }

        throw new RuntimeException("unsupported types for binary operation: %s %s".formatted(leftType, rightType));
    }

    private void executeBinaryInteger(int op, MonkeyObject left, MonkeyObject right) {
        final var leftValue = ((MonkeyInteger) left).value();
        final var rightValue = ((MonkeyInteger) right).value();

        final var result = switch (op) {
            case OP_ADD -> leftValue + rightValue;
            case OP_SUB -> leftValue - rightValue;
            case OP_MUL -> leftValue * rightValue;
            case OP_DIV -> leftValue / rightValue;
            default -> throw new RuntimeException("unknown integer operation [%s]".formatted(op));
        };
        push(new MonkeyInteger(result));
    }

    private void executeBinaryString(int op, MonkeyObject left, MonkeyObject right) {
        if (op != OP_ADD) {
            throw new RuntimeException("unknown string operator [%s]".formatted(op));
        }

        final var leftValue = ((MonkeyString) left).value();
        final var rightValue = ((MonkeyString) right).value();

        push(new MonkeyString(leftValue + rightValue));
    }

    private void executeComparison(int op) {
        final var right = pop();
        final var left = pop();

        if (left.type() == INTEGER_OBJ && right.type() == INTEGER_OBJ) {
            executeIntegerComparison(op, left, right);
            return;
        }

        switch (op) {
            case OP_EQUAL -> push(nativeBoolToBooleanObject(right == left));
            case OP_NOT_EQUAL -> push(nativeBoolToBooleanObject(right != left));
            default ->
                    throw new RuntimeException("unknown integer operation [%s] (%s %s)".formatted(op, left.type(), right.type()));
        }
    }

    private void executeIntegerComparison(int op, MonkeyObject left, MonkeyObject right) {
        final var leftValue = ((MonkeyInteger) left).value();
        final var rightValue = ((MonkeyInteger) right).value();

        switch (op) {
            case OP_EQUAL -> push(nativeBoolToBooleanObject(rightValue == leftValue));
            case OP_NOT_EQUAL -> push(nativeBoolToBooleanObject(rightValue != leftValue));
            case OP_GREATER_THAN -> push(nativeBoolToBooleanObject(leftValue > rightValue));
            default -> throw new RuntimeException("unknown operator [%s]".formatted(op));
        }
    }

    private MonkeyBoolean nativeBoolToBooleanObject(boolean input) {
        return input ? TRUE : FALSE;
    }

    private void executeBangOperator() {
        final var operand = pop();

        switch (operand) {
            case MonkeyBoolean monkeyBoolean -> {
                if (monkeyBoolean.value()) {
                    push(FALSE);
                } else {
                    push(TRUE);
                }
            }
            case MonkeyNull __ -> push(TRUE);
            default -> push(FALSE);
        }
    }

    private void executeMinusOperator() {
        final var operand = pop();

        if (operand.type() != INTEGER_OBJ) {
            throw new RuntimeException("unsupported type for negation: %s".formatted(operand.type()));
        }

        final var value = ((MonkeyInteger) operand).value();
        push(new MonkeyInteger(-value));
    }

    private void executeIndexExpression(MonkeyObject left, MonkeyObject index) {
        if (left.type() == ARRAY_OBJ && index.type() == INTEGER_OBJ) {
            executeArrayIndex(left, index);
            return;
        }
        if (left.type() == HASH_OBJ) {
            executeHashIndex(left, index);
            return;
        }
        throw new RuntimeException("index operator not supported [%s]".formatted(left.type()));
    }

    private void executeArrayIndex(MonkeyObject left, MonkeyObject index) {
        final var array = (MonkeyArray) left;
        final var i = ((MonkeyInteger) (index)).value();
        final var max = array.elements().size() - 1;

        if (i < 0 || i > max) {
            push(NULL);
            return;
        }
        push(array.elements().get(i));
    }

    private void executeHashIndex(MonkeyObject left, MonkeyObject index) {
        final var hash = (MonkeyHash) left;
        if (index instanceof Hashable hashable) {
            final var pair = hash.getHashPair(hashable.hashKey());
            if (pair == null) {
                push(NULL);
                return;
            }
            push(pair.value());
            return;
        }

        throw new RuntimeException("unusable as hash key [%s]".formatted(index));
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

    private void pushClosure(int constIndex, int numFree) {
        final var constant = constants.get(constIndex);
        if (constant instanceof CompilerFunction function) {

            final var free = iterate(0, i -> i < numFree, i -> i + 1)
                    .map(it -> stack[stackPointer - numFree + it])
                    .toArray(MonkeyObject[]::new);
            stackPointer -= numFree;

            final var closure = new Closure(function, free);
            push(closure);
            return;
        }
        throw new RuntimeException("not a function: %s".formatted(constant.getClass()));
    }

    private void push(MonkeyObject object) {
        if (stackPointer >= STACK_SIZE) {
            throw new RuntimeException("stack overflow");
        }
        stack[stackPointer] = object;
        stackPointer++;
    }

    private MonkeyObject pop() {
        final var object = stack[stackPointer - 1];
        stackPointer--;
        // explicitly not null'ing last element. Just for the book and tests. see usage of lastPoppedStackElement()
        return object;
    }
}
