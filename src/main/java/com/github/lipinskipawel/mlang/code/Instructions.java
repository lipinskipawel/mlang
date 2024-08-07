package com.github.lipinskipawel.mlang.code;

import com.github.lipinskipawel.mlang.code.OpCode.Definition;

import java.nio.ByteBuffer;
import java.util.List;

import static java.lang.System.arraycopy;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.util.Arrays.stream;

public final class Instructions {
    byte[] instructions;

    private Instructions(byte[] instructions) {
        this.instructions = instructions;
    }

    public static Instructions noInstructions() {
        return new Instructions(new byte[0]);
    }

    public static Instructions instructions(byte[] instructions) {
        return new Instructions(instructions);
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
                case 1 -> instruction.put((byte) operands[i]);
                case 2 -> instruction.putShort((short) operands[i]);
            }
        }

        return instruction.array();
    }

    public record Operands(int[] operands, int offset) {
    }

    static Operands readOperands(Definition definition, Instructions instructions) {
        // this should be int[], not byte[]
        // I should change this implementation to use raw byte[]
        final var operands = ByteBuffer.allocate(definition.operandWidths().length * 4).order(BIG_ENDIAN);
        var offset = 0;

        for (var i = 0; i < definition.operandWidths().length; i++) {
            final var wrap = ByteBuffer.wrap(instructions.instructions).order(BIG_ENDIAN);
            var width = definition.operandWidths()[i];
            switch (width) {
                case 1: {
                    wrap.position(offset);
                    operands.putShort((short) 0);
                    operands.put((byte) 0);
                    operands.put(wrap.get());
                    break;
                }
                case 2: {
                    wrap.position(offset);
                    operands.putShort((short) 0);
                    operands.putShort(wrap.getShort());
                    break;
                }
            }
            offset += width;
        }

        return new Operands(toIntArray(operands.array()), offset);
    }

    private static int[] toIntArray(byte[] bytes) {
        final var byteBuffer = ByteBuffer.wrap(bytes).order(BIG_ENDIAN);
        final var intBuffer = byteBuffer.asIntBuffer();

        final var ints = new int[intBuffer.limit()];
        intBuffer.get(ints);

        return ints;
    }

    public static Instructions merge(List<Instructions> instructions) {
        final var length = instructions.stream()
                .mapToInt(Instructions::length)
                .sum();
        final var contacted = ByteBuffer.allocate(length).order(BIG_ENDIAN);
        instructions.stream()
                .map(it -> it.instructions)
                .forEach(contacted::put);

        return instructions(contacted.array());
    }

    public void append(Instructions additional) {
        final var byteBuffer = ByteBuffer.allocate(instructions.length + additional.length()).order(BIG_ENDIAN);
        byteBuffer.put(instructions);
        byteBuffer.put(additional.instructions);

        instructions = byteBuffer.array();
    }

    public int length() {
        return instructions.length;
    }

    public void replaceInstructions(int pos, byte[] newInstructions) {
        arraycopy(newInstructions, 0, instructions, pos, newInstructions.length);
    }

    public byte instructionAt(int position) {
        return instructions[position];
    }

    public byte[] slice(int start, int end) {
        final var byteBuffer = ByteBuffer.wrap(instructions).order(BIG_ENDIAN);
        byteBuffer.position(start);
        byteBuffer.limit(end);

        var slice = new byte[byteBuffer.remaining()];
        byteBuffer.get(slice);

        return slice;
    }

    public short readShort(int start) {
        return (short) (instructions[start] << 8 | (instructions[start + 1] & 0xFF));
    }

    @Override
    public String toString() {
        final var string = new StringBuilder();

        var i = 0;
        while (i < instructions.length) {
            final var definition = OpCode.definition(instructions[i]);
            final var operands = readOperands(definition, instructions(slice(i + 1, this.instructions.length)));

            string.append("%04d %s\n".formatted(i, fmtInstruction(definition, operands.operands())));

            i += 1 + operands.offset;
        }

        return string.toString();
    }

    private String fmtInstruction(Definition definition, int[] operands) {
        final var operandCount = definition.operandWidths().length;

        if (operands.length != operandCount) {
            return "ERROR: operand len %d does not match defined %d".formatted(operands.length, operandCount);
        }

        return switch (operandCount) {
            case 0 -> definition.name();
            case 1 -> "%s %d".formatted(definition.name(), operands[0]);
            case 2 -> "%s %d %d".formatted(definition.name(), operands[0], operands[1]);
            default -> "ERROR: unhandled operandCount for %s\n".formatted(definition.name());
        };
    }
}
