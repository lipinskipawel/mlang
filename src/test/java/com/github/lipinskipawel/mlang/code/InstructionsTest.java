package com.github.lipinskipawel.mlang.code;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.github.lipinskipawel.mlang.code.Instructions.instructions;
import static com.github.lipinskipawel.mlang.code.Instructions.make;
import static com.github.lipinskipawel.mlang.code.Instructions.merge;
import static com.github.lipinskipawel.mlang.code.Instructions.readOperands;
import static com.github.lipinskipawel.mlang.code.Instructions.slice;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_ADD;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_CONSTANT;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_GET_LOCAL;
import static org.junit.jupiter.params.provider.Arguments.of;

@DisplayName("Instructions Spec")
class InstructionsTest implements WithAssertions {

    private static Stream<Arguments> singleInstruction() {
        return Stream.of(
                of(OP_CONSTANT, new int[]{65534}, new byte[]{OP_CONSTANT.opCode, (byte) 255, (byte) 254}),
                of(OP_CONSTANT, new int[]{0xFFFE}, new byte[]{OP_CONSTANT.opCode, (byte) 0xFF, (byte) 0xFE}),
                of(OP_ADD, new int[0], new byte[]{OP_ADD.opCode}),
                of(OP_GET_LOCAL, new int[]{255}, new byte[]{OP_GET_LOCAL.opCode, (byte) 255})
        );
    }

    @ParameterizedTest
    @MethodSource("singleInstruction")
    @DisplayName("make single instruction")
    void make_single_instruction(OpCode op, int[] operands, byte[] expected) {
        var instruction = make(op, operands);

        assertThat(instruction).satisfies(
                ins -> assertThat(ins.length).isEqualTo(expected.length),
                ins -> {
                    for (var i = 0; i < ins.length; i++) {
                        assertThat(ins[i]).isEqualTo(expected[i]);
                    }
                }
        );
    }

    private static Stream<Arguments> readOperandsCases() {
        return Stream.of(
                of(OP_CONSTANT, new int[]{65535}, 2),
                of(OP_GET_LOCAL, new int[]{255}, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("readOperandsCases")
    @DisplayName("read operands")
    void read_operands(OpCode op, int[] operands, int bytesRead) {
        var instruction = make(op, operands);

        var operandsRead = readOperands(op.definition(), instructions(slice(instruction, 1, instruction.length)));

        assertThat(operandsRead.offset()).isEqualTo(bytesRead);
        for (var i = 0; i < operands.length; i++) {
            assertThat(operandsRead.operands()[i]).isEqualTo(operands[i]);
        }
    }

    @Test
    @DisplayName("pretty print instructions")
    void testInstructionString() {
        var expected = """
                0000 OpAdd
                0001 OpGetLocal 1
                0003 OpConstant 2
                0006 OpConstant 65535
                """;
        var instructions = List.of(
                instructions(make(OP_ADD, new int[0])),
                instructions(make(OP_GET_LOCAL, new int[]{1})),
                instructions(make(OP_CONSTANT, new int[]{2})),
                instructions(make(OP_CONSTANT, new int[]{65535}))
        );

        var allInstructions = merge(instructions);

        assertThat(allInstructions.toString()).isEqualTo(expected);
    }
}
