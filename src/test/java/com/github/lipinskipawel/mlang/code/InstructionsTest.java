package com.github.lipinskipawel.mlang.code;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.github.lipinskipawel.mlang.code.Instructions.make;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_CONSTANT;
import static org.junit.jupiter.params.provider.Arguments.of;

@DisplayName("Instructions Spec")
class InstructionsTest implements WithAssertions {

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
        final var instruction = make(op, operands);

        assertThat(instruction).satisfies(
                ins -> assertThat(ins.length).isEqualTo(expected.length),
                ins -> assertThat(ins[0]).isEqualTo(expected[0]),
                ins -> assertThat(ins[1]).isEqualTo(expected[1]),
                ins -> assertThat(ins[2]).isEqualTo(expected[2])
        );
    }
}
