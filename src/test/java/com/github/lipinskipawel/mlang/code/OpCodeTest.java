package com.github.lipinskipawel.mlang.code;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

@DisplayName("OpCode Spec")
class OpCodeTest implements WithAssertions {

    @Test
    @DisplayName("every op code is mapped to its definition")
    void every_opCode_has_definition() {
        final var definitionsFromOpCodes = Arrays.stream(OpCode.values())
                .map(OpCode::definition)
                .toList();

        assertThat(definitionsFromOpCodes).doesNotContainNull();
    }
}
