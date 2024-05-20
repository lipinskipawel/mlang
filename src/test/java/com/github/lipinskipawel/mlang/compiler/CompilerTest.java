package com.github.lipinskipawel.mlang.compiler;

import com.github.lipinskipawel.mlang.code.Instructions;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.parser.Parser;
import com.github.lipinskipawel.mlang.parser.ast.Program;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.github.lipinskipawel.mlang.code.Instructions.instructions;
import static com.github.lipinskipawel.mlang.code.Instructions.make;
import static com.github.lipinskipawel.mlang.code.Instructions.merge;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_CONSTANT;
import static com.github.lipinskipawel.mlang.compiler.Compiler.compiler;
import static com.github.lipinskipawel.mlang.lexer.Lexer.lexer;
import static org.junit.jupiter.params.provider.Arguments.of;

@DisplayName("Compiler Spec")
class CompilerTest implements WithAssertions {

    private record CompilerTestCase(
            String input,
            List<Object> expectedConstants,
            List<Instructions> expectedInstructions
    ) {
    }

    private static Stream<Arguments> tests() {
        return Stream.of(
                of(new CompilerTestCase("1 + 2", List.of(1, 2), List.of(
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        instructions(make(OP_CONSTANT, new int[]{1})))))
        );
    }

    @ParameterizedTest
    @MethodSource("tests")
    @DisplayName("integer arithmetic")
    void integer_arithmetic(CompilerTestCase compilerTestCase) {
        runCompiler(compilerTestCase);
    }

    private void runCompiler(CompilerTestCase compilerTestCase) {
        var program = parse(compilerTestCase.input());

        var compiler = compiler();
        var error = compiler.compile(program);
        if (error.isPresent()) {
            fail("compiler error: [{}]", error);
        }
        var bytecode = compiler.bytecode();

        testInstructions(compilerTestCase.expectedInstructions(), bytecode.instructions());
        testConstants(compilerTestCase.expectedConstants(), bytecode.constants());
    }

    private void testInstructions(List<Instructions> expected, Instructions actual) {
        var concated = merge(expected);

        assertThat(actual.bytes().length).isEqualTo(concated.bytes().length);
        for (var i = 0; i < actual.bytes().length; i++) {
            assertThat(actual.bytes()[i]).isEqualTo(concated.bytes()[i]);
        }
    }

    private void testConstants(List<Object> expected, List<MonkeyObject> actual) {
        assertThat(actual.size()).isEqualTo(expected.size());

        for (var i = 0; i < expected.size(); i++) {
            var constant = expected.get(i);
            switch (constant) {
                case Integer integer -> testIntegerObject(actual.get(i), integer);
                default -> throw new IllegalStateException("Unexpected value: " + constant);
            }
        }
    }

    private void testIntegerObject(MonkeyObject actual, int expected) {
        assertThat(actual).satisfies(
                obj -> assertThat(obj).isInstanceOf(MonkeyInteger.class),
                obj -> {
                    var integer = (MonkeyInteger) obj;
                    assertThat(integer.value()).isEqualTo(expected);
                }
        );
    }

    private Program parse(String input) {
        var lexer = lexer(input);
        var parser = new Parser(lexer);
        return parser.parseProgram();
    }
}