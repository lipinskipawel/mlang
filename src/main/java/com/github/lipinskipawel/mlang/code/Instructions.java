package com.github.lipinskipawel.mlang.code;

import java.nio.ByteBuffer;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.util.Arrays.stream;

public final class Instructions {
    private final byte[] instructions;

    private Instructions(byte[] instructions) {
        this.instructions = instructions;
    }

    public static byte[] make(OpCode op, int[] operands) {
        final var definition = op.definition();

        final var instructionLen = 1 + stream(definition.operandWidths()).sum();
        final var instruction = ByteBuffer
                .allocate(instructionLen)
                .order(BIG_ENDIAN);

        instruction.put(op.opCode);
        for (var i = 0; i < operands.length; i++) {
            var width = definition.operandWidths()[i];
            switch (width) {
                case 2 -> instruction.putShort((short) operands[i]);
            }
        }

        return instruction.array();
    }
}
