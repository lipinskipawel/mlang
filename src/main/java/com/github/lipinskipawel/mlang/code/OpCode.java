package com.github.lipinskipawel.mlang.code;

import java.util.Map;

import static java.util.Map.entry;

public enum OpCode {
    OP_CONSTANT((byte) 1),
    OP_ADD((byte) 2),
    OP_POP((byte) 3),
    OP_SUB((byte) 4),
    OP_MUL((byte) 5),
    OP_DIV((byte) 6),
    OP_TRUE((byte) 7),
    OP_FALSE((byte) 8),
    OP_EQUAL((byte) 9),
    OP_NOT_EQUAL((byte) 10),
    OP_GREATER_THAN((byte) 11),
    OP_MINUS((byte) 12),
    OP_BANG((byte) 13),
    OP_JUMP_NOT_TRUTHY((byte) 14),
    OP_JUMP((byte) 15),
    OP_NULL((byte) 16),
    OP_GET_GLOBAL((byte) 17),
    OP_SET_GLOBAL((byte) 18),
    OP_ARRAY((byte) 19),
    OP_HASH((byte) 20),
    OP_INDEX((byte) 21),
    OP_CALL((byte) 22),
    OP_RETURN_VALUE((byte) 23),
    OP_RETURN((byte) 24),
    OP_GET_LOCAL((byte) 25),
    OP_SET_LOCAL((byte) 26),
    OP_GET_BUILTIN((byte) 27);

    final byte opCode;

    OpCode(byte opCode) {
        this.opCode = opCode;
    }

    public record Definition(String name, int[] operandWidths/* element = operand, value = width */) {
    }

    private static final Map<OpCode, Definition> DEFINITIONS = Map.ofEntries(
            entry(OP_CONSTANT, new Definition("OpConstant", new int[]{2})),  // one operand with 2 bytes (uint16) width
            entry(OP_ADD, new Definition("OpAdd", new int[0])),
            entry(OP_POP, new Definition("OpPop", new int[0])),
            entry(OP_SUB, new Definition("OpSub", new int[0])),
            entry(OP_MUL, new Definition("OpMul", new int[0])),
            entry(OP_DIV, new Definition("OpDiv", new int[0])),
            entry(OP_TRUE, new Definition("OpTrue", new int[0])),
            entry(OP_FALSE, new Definition("OpFalse", new int[0])),
            entry(OP_EQUAL, new Definition("OpEqual", new int[0])),
            entry(OP_NOT_EQUAL, new Definition("OpNotEqual", new int[0])),
            entry(OP_GREATER_THAN, new Definition("OpGreaterThan", new int[0])),
            entry(OP_MINUS, new Definition("OpMinus", new int[0])),
            entry(OP_BANG, new Definition("OpBang", new int[0])),
            entry(OP_JUMP_NOT_TRUTHY, new Definition("OpJumpNotTruthy", new int[]{2})),
            entry(OP_JUMP, new Definition("OpJump", new int[]{2})),
            entry(OP_NULL, new Definition("OpNull", new int[0])),
            entry(OP_GET_GLOBAL, new Definition("OpGetGlobal", new int[]{2})),
            entry(OP_SET_GLOBAL, new Definition("OpSetGlobal", new int[]{2})),
            entry(OP_ARRAY, new Definition("OpArray", new int[]{2})),
            entry(OP_HASH, new Definition("OpHash", new int[]{2})), // num key + val
            entry(OP_INDEX, new Definition("OpIndex", new int[0])),
            entry(OP_CALL, new Definition("OpCall", new int[]{1})),
            entry(OP_RETURN_VALUE, new Definition("OpReturnValue", new int[0])),
            entry(OP_RETURN, new Definition("OpReturn", new int[0])),
            entry(OP_GET_LOCAL, new Definition("OpGetLocal", new int[]{1})),
            entry(OP_SET_LOCAL, new Definition("OpSetLocal", new int[]{1})),
            entry(OP_GET_BUILTIN, new Definition("OpGetBuiltin", new int[]{1}))
    );

    public Definition definition() {
        return DEFINITIONS.get(this);
    }

    public static OpCode opCode(byte oneByte) {
        return switch (oneByte) {
            case 1 -> OP_CONSTANT;
            case 2 -> OP_ADD;
            case 3 -> OP_POP;
            case 4 -> OP_SUB;
            case 5 -> OP_MUL;
            case 6 -> OP_DIV;
            case 7 -> OP_TRUE;
            case 8 -> OP_FALSE;
            case 9 -> OP_EQUAL;
            case 10 -> OP_NOT_EQUAL;
            case 11 -> OP_GREATER_THAN;
            case 12 -> OP_MINUS;
            case 13 -> OP_BANG;
            case 14 -> OP_JUMP_NOT_TRUTHY;
            case 15 -> OP_JUMP;
            case 16 -> OP_NULL;
            case 17 -> OP_GET_GLOBAL;
            case 18 -> OP_SET_GLOBAL;
            case 19 -> OP_ARRAY;
            case 20 -> OP_HASH;
            case 21 -> OP_INDEX;
            case 22 -> OP_CALL;
            case 23 -> OP_RETURN_VALUE;
            case 24 -> OP_RETURN;
            case 25 -> OP_GET_LOCAL;
            case 26 -> OP_SET_LOCAL;
            case 27 -> OP_GET_BUILTIN;
            default -> throw new IllegalArgumentException("No opcode defined for [%s]".formatted(oneByte));
        };
    }

    public static Definition definition(byte op) {
        return switch (op) {
            case 1 -> DEFINITIONS.get(OP_CONSTANT);
            case 2 -> DEFINITIONS.get(OP_ADD);
            case 3 -> DEFINITIONS.get(OP_POP);
            case 4 -> DEFINITIONS.get(OP_SUB);
            case 5 -> DEFINITIONS.get(OP_MUL);
            case 6 -> DEFINITIONS.get(OP_DIV);
            case 7 -> DEFINITIONS.get(OP_TRUE);
            case 8 -> DEFINITIONS.get(OP_FALSE);
            case 9 -> DEFINITIONS.get(OP_EQUAL);
            case 10 -> DEFINITIONS.get(OP_NOT_EQUAL);
            case 11 -> DEFINITIONS.get(OP_GREATER_THAN);
            case 12 -> DEFINITIONS.get(OP_MINUS);
            case 13 -> DEFINITIONS.get(OP_BANG);
            case 14 -> DEFINITIONS.get(OP_JUMP_NOT_TRUTHY);
            case 15 -> DEFINITIONS.get(OP_JUMP);
            case 16 -> DEFINITIONS.get(OP_NULL);
            case 17 -> DEFINITIONS.get(OP_GET_GLOBAL);
            case 18 -> DEFINITIONS.get(OP_SET_GLOBAL);
            case 19 -> DEFINITIONS.get(OP_ARRAY);
            case 20 -> DEFINITIONS.get(OP_HASH);
            case 21 -> DEFINITIONS.get(OP_INDEX);
            case 22 -> DEFINITIONS.get(OP_CALL);
            case 23 -> DEFINITIONS.get(OP_RETURN_VALUE);
            case 24 -> DEFINITIONS.get(OP_RETURN);
            case 25 -> DEFINITIONS.get(OP_GET_LOCAL);
            case 26 -> DEFINITIONS.get(OP_SET_LOCAL);
            case 27 -> DEFINITIONS.get(OP_GET_BUILTIN);
            default -> throw new IllegalStateException("Unexpected value: " + op);
        };
    }
}
