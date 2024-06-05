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
    OP_BANG((byte) 13);

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
            entry(OP_BANG, new Definition("OpBang", new int[0]))
    );

    public Definition definition() {
        return switch (this) {
            case OP_CONSTANT, OP_ADD, OP_POP, OP_SUB, OP_MUL, OP_DIV,
                 OP_TRUE, OP_FALSE, OP_EQUAL, OP_NOT_EQUAL, OP_GREATER_THAN,
                 OP_MINUS, OP_BANG -> DEFINITIONS.get(this);
        };
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
            default -> throw new IllegalStateException("Unexpected value: " + op);
        };
    }
}
