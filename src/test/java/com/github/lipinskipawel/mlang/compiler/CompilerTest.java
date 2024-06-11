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
import static com.github.lipinskipawel.mlang.code.OpCode.OP_ADD;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_BANG;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_CONSTANT;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_DIV;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_EQUAL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_FALSE;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_GET_GLOBAL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_GREATER_THAN;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_JUMP;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_JUMP_NOT_TRUTHY;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_MINUS;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_MUL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_NOT_EQUAL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_NULL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_POP;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_SET_GLOBAL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_SUB;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_TRUE;
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

    private static Stream<Arguments> arithmetic() {
        return Stream.of(
                of(new CompilerTestCase("1 + 2", List.of(1, 2), List.of(
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        instructions(make(OP_CONSTANT, new int[]{1})),
                        instructions(make(OP_ADD, new int[0])),
                        instructions(make(OP_POP, new int[0]))
                ))),
                of(new CompilerTestCase("1; 2", List.of(1, 2), List.of(
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        instructions(make(OP_POP, new int[0])),
                        instructions(make(OP_CONSTANT, new int[]{1})),
                        instructions(make(OP_POP, new int[0]))
                ))),
                of(new CompilerTestCase("1 - 2", List.of(1, 2), List.of(
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        instructions(make(OP_CONSTANT, new int[]{1})),
                        instructions(make(OP_SUB, new int[0])),
                        instructions(make(OP_POP, new int[0]))
                ))),
                of(new CompilerTestCase("1 * 2", List.of(1, 2), List.of(
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        instructions(make(OP_CONSTANT, new int[]{1})),
                        instructions(make(OP_MUL, new int[0])),
                        instructions(make(OP_POP, new int[0]))
                ))),
                of(new CompilerTestCase("2 / 1", List.of(2, 1), List.of(
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        instructions(make(OP_CONSTANT, new int[]{1})),
                        instructions(make(OP_DIV, new int[0])),
                        instructions(make(OP_POP, new int[0]))
                ))),
                of(new CompilerTestCase("-1", List.of(1), List.of(
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        instructions(make(OP_MINUS, new int[0])),
                        instructions(make(OP_POP, new int[0]))
                )))
        );
    }

    @ParameterizedTest
    @MethodSource("arithmetic")
    @DisplayName("integer arithmetic")
    void integer_arithmetic(CompilerTestCase compilerTestCase) {
        runCompiler(compilerTestCase);
    }

    private static Stream<Arguments> booleans() {
        return Stream.of(
                of(new CompilerTestCase("true", List.of(), List.of(
                        instructions(make(OP_TRUE, new int[0])),
                        instructions(make(OP_POP, new int[0]))
                ))),
                of(new CompilerTestCase("false", List.of(), List.of(
                        instructions(make(OP_FALSE, new int[0])),
                        instructions(make(OP_POP, new int[0]))
                ))),
                of(new CompilerTestCase("1 > 2", List.of(1, 2), List.of(
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        instructions(make(OP_CONSTANT, new int[]{1})),
                        instructions(make(OP_GREATER_THAN, new int[0])),
                        instructions(make(OP_POP, new int[0]))
                ))),
                of(new CompilerTestCase("1 < 2", List.of(2, 1), List.of(
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        instructions(make(OP_CONSTANT, new int[]{1})),
                        instructions(make(OP_GREATER_THAN, new int[0])),
                        instructions(make(OP_POP, new int[0]))
                ))),
                of(new CompilerTestCase("1 == 2", List.of(1, 2), List.of(
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        instructions(make(OP_CONSTANT, new int[]{1})),
                        instructions(make(OP_EQUAL, new int[0])),
                        instructions(make(OP_POP, new int[0]))
                ))),
                of(new CompilerTestCase("1 != 2", List.of(1, 2), List.of(
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        instructions(make(OP_CONSTANT, new int[]{1})),
                        instructions(make(OP_NOT_EQUAL, new int[0])),
                        instructions(make(OP_POP, new int[0]))
                ))),
                of(new CompilerTestCase("true == false", List.of(), List.of(
                        instructions(make(OP_TRUE, new int[0])),
                        instructions(make(OP_FALSE, new int[0])),
                        instructions(make(OP_EQUAL, new int[0])),
                        instructions(make(OP_POP, new int[0]))
                ))),
                of(new CompilerTestCase("true != false", List.of(), List.of(
                        instructions(make(OP_TRUE, new int[0])),
                        instructions(make(OP_FALSE, new int[0])),
                        instructions(make(OP_NOT_EQUAL, new int[0])),
                        instructions(make(OP_POP, new int[0]))
                ))),
                of(new CompilerTestCase("!true", List.of(), List.of(
                        instructions(make(OP_TRUE, new int[0])),
                        instructions(make(OP_BANG, new int[0])),
                        instructions(make(OP_POP, new int[0]))
                )))
        );
    }

    @ParameterizedTest
    @MethodSource("booleans")
    @DisplayName("boolean expression")
    void boolean_expression(CompilerTestCase compilerTestCase) {
        runCompiler(compilerTestCase);
    }

    private static Stream<Arguments> conditionals() {
        return Stream.of(
                of(new CompilerTestCase("if (true) { 10; }; 3333;", List.of(10, 3333), List.of(
                        // 0000
                        instructions(make(OP_TRUE, new int[0])),
                        // 0001
                        instructions(make(OP_JUMP_NOT_TRUTHY, new int[]{10})),
                        // 0004
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        // 0007
                        instructions(make(OP_JUMP, new int[]{11})),
                        // 0010
                        instructions(make(OP_NULL, new int[0])),
                        // 0011
                        instructions(make(OP_POP, new int[0])),
                        // 0012
                        instructions(make(OP_CONSTANT, new int[]{1})),
                        // 0015
                        instructions(make(OP_POP, new int[0]))
                ))),
                of(new CompilerTestCase("if (true) { 10; } else { 20; }; 3333;", List.of(10, 20, 3333), List.of(
                        // 0000
                        instructions(make(OP_TRUE, new int[0])),
                        // 0001
                        instructions(make(OP_JUMP_NOT_TRUTHY, new int[]{10})),
                        // 0004
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        // 0007
                        instructions(make(OP_JUMP, new int[]{13})),
                        // 0010
                        instructions(make(OP_CONSTANT, new int[]{1})),
                        // 0013
                        instructions(make(OP_POP, new int[0])),
                        // 0014
                        instructions(make(OP_CONSTANT, new int[]{2})),
                        // 0017
                        instructions(make(OP_POP, new int[0]))
                )))
        );
    }

    @ParameterizedTest
    @MethodSource("conditionals")
    @DisplayName("conditional expressions")
    void conditional_expressions(CompilerTestCase compilerTestCase) {
        runCompiler(compilerTestCase);
    }

    private static Stream<Arguments> globalLetStatements() {
        return Stream.of(
                of(new CompilerTestCase("""
                        let one = 1;
                        let two = 2;
                        """, List.of(1, 2), List.of(
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        instructions(make(OP_SET_GLOBAL, new int[]{0})),
                        instructions(make(OP_CONSTANT, new int[]{1})),
                        instructions(make(OP_SET_GLOBAL, new int[]{1}))
                ))),
                of(new CompilerTestCase("""
                        let one = 1;
                        one;
                        """, List.of(1), List.of(
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        instructions(make(OP_SET_GLOBAL, new int[]{0})),
                        instructions(make(OP_GET_GLOBAL, new int[]{0})),
                        instructions(make(OP_POP, new int[0]))))),
                of(new CompilerTestCase("""
                        let one = 1;
                        let two = one;
                        two;
                        """, List.of(1), List.of(
                        instructions(make(OP_CONSTANT, new int[]{0})),
                        instructions(make(OP_SET_GLOBAL, new int[]{0})),
                        instructions(make(OP_GET_GLOBAL, new int[]{0})),
                        instructions(make(OP_SET_GLOBAL, new int[]{1})),
                        instructions(make(OP_GET_GLOBAL, new int[]{1})),
                        instructions(make(OP_POP, new int[]{})))))
        );
    }

    @ParameterizedTest
    @MethodSource("globalLetStatements")
    @DisplayName("global let statements")
    void global_let_statements(CompilerTestCase compilerTestCase) {
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

        final var actualLength = actual.bytes().length;
        final var expectedLength = concated.bytes().length;
        if (actualLength != expectedLength) {
            fail("wrong instruction length\nwant\n%s\ngot\n%s\n", concated, actual);
        }
        assertThat(actualLength).isEqualTo(expectedLength);
        for (var i = 0; i < actual.bytes().length; i++) {
            final byte actualByte = actual.bytes()[i];
            final byte expectedByte = concated.bytes()[i];
            if (actualByte != expectedByte) {
                fail("wrong instruction at %d. \nwant\n%s\ngot\n%s\n", i, concated, actual);
            }
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