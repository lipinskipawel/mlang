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
import static com.github.lipinskipawel.mlang.code.OpCode.OP_CONSTANT;
import static org.junit.jupiter.params.provider.Arguments.of;

@DisplayName("Instructions Spec")
class InstructionsTest implements WithAssertions {

    @Test
    @DisplayName("pretty print instructions")
    void pretty_print_instructions() {
        var expected = """
                0000 OpConstant 1
                0003 OpConstant 2
                0006 OpConstant 65535
                """;
        var instructions = List.of(
                instructions(make(OP_CONSTANT, new int[]{1})),
                instructions(make(OP_CONSTANT, new int[]{2})),
                instructions(make(OP_CONSTANT, new int[]{65535}))
        );

        var allInstructions = merge(instructions);

        assertThat(allInstructions.toString()).isEqualTo(expected);
    }

    private static Stream<Arguments> readOperandsCases() {
        return Stream.of(
                of(OP_CONSTANT, new int[]{65535}, 2)
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

    private static Stream<Arguments> singleInstruction() {
        return Stream.of(
                of(OP_CONSTANT, new int[]{65534}, new byte[]{OP_CONSTANT.opCode, (byte) 255, (byte) 254}),
                of(OP_CONSTANT, new int[]{0xFFFE}, new byte[]{OP_CONSTANT.opCode, (byte) 0xFF, (byte) 0xFE})
        );
    }

    @ParameterizedTest
    @MethodSource("singleInstruction")
    @DisplayName("create single instruction")
    void create_single_instruction(OpCode op, int[] operands, byte[] expected) {
        var instruction = make(op, operands);

        assertThat(instruction).satisfies(
                ins -> assertThat(ins.length).isEqualTo(expected.length),
                ins -> assertThat(ins[0]).isEqualTo(expected[0]),
                ins -> assertThat(ins[1]).isEqualTo(expected[1]),
                ins -> assertThat(ins[2]).isEqualTo(expected[2])
        );
    }
}
