package com.github.lipinskipawel.mlang.code;

import java.util.Map;

public enum OpCode {
    OP_CONSTANT((byte) 1),
    OP_ADD((byte) 2),
    OP_POP((byte) 3),
    OP_SUB((byte) 4),
    OP_MUL((byte) 5),
    OP_DIV((byte) 6);

    final byte opCode;

    OpCode(byte opCode) {
        this.opCode = opCode;
    }

    public record Definition(String name, int[] operandWidths/* element = operand, value = width */) {
    }

    private static final Map<OpCode, Definition> DEFINITIONS = Map.of(
            OP_CONSTANT, new Definition("OpConstant", new int[]{2}),  // one operand with 2 bytes (uint16) width
            OP_ADD, new Definition("OpAdd", new int[0]),
            OP_POP, new Definition("OpPop", new int[0]),
            OP_SUB, new Definition("OpSub", new int[0]),
            OP_MUL, new Definition("OpMul", new int[0]),
            OP_DIV, new Definition("OpDiv", new int[0])
    );

    public Definition definition() {
        return switch (this) {
            case OP_CONSTANT, OP_ADD, OP_POP, OP_SUB, OP_MUL, OP_DIV -> DEFINITIONS.get(this);
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
            default -> throw new IllegalStateException("Unexpected value: " + op);
        };
    }
}
