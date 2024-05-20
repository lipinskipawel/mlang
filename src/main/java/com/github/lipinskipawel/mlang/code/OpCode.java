package com.github.lipinskipawel.mlang.code;

import java.util.Map;

public enum OpCode {
    OP_CONSTANT((byte) 1); // one operand with 2 bytes (uint16) width

    final byte opCode;

    OpCode(byte opCode) {
        this.opCode = opCode;
    }

    public record Definition(String name, int[] operandWidths/* element = operand, value = width */) {
    }

    private static final Map<OpCode, Definition> DEFINITIONS = Map.of(
            OP_CONSTANT, new Definition("OpConstant", new int[]{2})
    );

    public Definition definition() {
        return switch (this) {
            case OP_CONSTANT -> DEFINITIONS.get(this);
        };
    }

    public static OpCode opCode(byte oneByte) {
        return switch (oneByte) {
            case 1 -> OP_CONSTANT;
            default -> throw new IllegalArgumentException("No opcode defined for [%s]".formatted(oneByte));
        };
    }

    public static Definition definition(byte op) {
        return switch (op) {
            case 1 -> DEFINITIONS.get(OP_CONSTANT);
            default -> throw new IllegalStateException("Unexpected value: " + op);
        };
    }
}
