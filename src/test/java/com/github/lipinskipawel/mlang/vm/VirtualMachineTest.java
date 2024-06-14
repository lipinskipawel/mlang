package com.github.lipinskipawel.mlang.vm;

import com.github.lipinskipawel.mlang.evaluator.objects.HashKey;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyArray;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBoolean;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyHash;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyNull;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyString;
import com.github.lipinskipawel.mlang.parser.Parser;
import com.github.lipinskipawel.mlang.parser.ast.Program;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.lipinskipawel.mlang.compiler.Compiler.compiler;
import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.INTEGER_OBJ;
import static com.github.lipinskipawel.mlang.lexer.Lexer.lexer;
import static com.github.lipinskipawel.mlang.vm.VirtualMachine.NULL;
import static com.github.lipinskipawel.mlang.vm.VirtualMachine.virtualMachine;
import static org.junit.jupiter.params.provider.Arguments.of;

@DisplayName("Virtual Machine Spec")
class VirtualMachineTest implements WithAssertions {

    private record VmTestCase(
            String input,
            Object expected
    ) {
    }

    private static Stream<Arguments> arithmetic() {
        return Stream.of(
                of(new VmTestCase("1", 1)),
                of(new VmTestCase("2", 2)),
                of(new VmTestCase("1 + 2", 3)),
                of(new VmTestCase("1 - 2", -1)),
                of(new VmTestCase("1 * 2", 2)),
                of(new VmTestCase("4 / 2", 2)),
                of(new VmTestCase("50 / 2 * 2 + 10 - 5", 55)),
                of(new VmTestCase("5 + 5 + 5 + 5 - 10", 10)),
                of(new VmTestCase("2 * 2 * 2 * 2 * 2", 32)),
                of(new VmTestCase("5 * 2 + 10", 20)),
                of(new VmTestCase("5 + 2 * 10", 25)),
                of(new VmTestCase("5 * (2 + 10)", 60)),
                of(new VmTestCase("-5", -5)),
                of(new VmTestCase("-10", -10)),
                of(new VmTestCase("-50 + 100 + -50", 0)),
                of(new VmTestCase("(5 + 10 * 2 + 15 / 3) * 2 + -10", 50))
        );
    }

    @ParameterizedTest
    @MethodSource("arithmetic")
    @DisplayName("integer arithmetic")
    void integer_arithmetic(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private static Stream<Arguments> booleans() {
        return Stream.of(
                of(new VmTestCase("true", true)),
                of(new VmTestCase("false", false)),
                of(new VmTestCase("1 < 2", true)),
                of(new VmTestCase("1 > 2", false)),
                of(new VmTestCase("1 < 1", false)),
                of(new VmTestCase("1 > 1", false)),
                of(new VmTestCase("1 == 1", true)),
                of(new VmTestCase("1 != 1", false)),
                of(new VmTestCase("1 == 2", false)),
                of(new VmTestCase("1 != 2", true)),
                of(new VmTestCase("true == true", true)),
                of(new VmTestCase("false == false", true)),
                of(new VmTestCase("true == false", false)),
                of(new VmTestCase("true != false", true)),
                of(new VmTestCase("false != true", true)),
                of(new VmTestCase("(1 < 2) == true", true)),
                of(new VmTestCase("(1 < 2) == false", false)),
                of(new VmTestCase("(1 > 2) == true", false)),
                of(new VmTestCase("(1 > 2) == false", true)),
                of(new VmTestCase("!true", false)),
                of(new VmTestCase("!false", true)),
                of(new VmTestCase("!5", false)),
                of(new VmTestCase("!!true", true)),
                of(new VmTestCase("!!false", false)),
                of(new VmTestCase("!!5", true)),
                of(new VmTestCase("!(if (false) { 5; })", true))
        );
    }

    @ParameterizedTest
    @MethodSource("booleans")
    @DisplayName("boolean expression")
    void boolean_expression(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private static Stream<Arguments> conditionals() {
        return Stream.of(
                of(new VmTestCase("if (true) { 10 }", 10)),
                of(new VmTestCase("if (true) { 10 } else { 20 }", 10)),
                of(new VmTestCase("if (false) { 10 } else { 20 } ", 20)),
                of(new VmTestCase("if (1) { 10 }", 10)),
                of(new VmTestCase("if (1 < 2) { 10 }", 10)),
                of(new VmTestCase("if (1 < 2) { 10 } else { 20 }", 10)),
                of(new VmTestCase("if (1 > 2) { 10 } else { 20 }", 20)),
                of(new VmTestCase("if (1 > 2) { 10 }", NULL)),
                of(new VmTestCase("if (false) { 10 }", NULL)),
                of(new VmTestCase("if ((if (false) { 10 })) { 10 } else { 20 }", 20))
        );
    }

    @ParameterizedTest
    @MethodSource("conditionals")
    @DisplayName("conditional expressions")
    void conditional_expressions(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private static Stream<Arguments> globalLetStatements() {
        return Stream.of(
                of(new VmTestCase("let one = 1; one", 1)),
                of(new VmTestCase("let one = 1; let two = 2; one + two", 3)),
                of(new VmTestCase("let one = 1; let two = one + one; one + two", 3))
        );
    }

    @ParameterizedTest
    @MethodSource("globalLetStatements")
    @DisplayName("global let statements")
    void global_let_statement(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private static Stream<Arguments> strings() {
        return Stream.of(
                of(new VmTestCase(("""
                        "monkey"
                        """), "monkey")),
                of(new VmTestCase(("""
                        "mon" + "key"
                        """), "monkey")),
                of(new VmTestCase(("""
                        "mon" + "key" + "banana"
                        """), "monkeybanana"))
        );
    }

    @ParameterizedTest
    @MethodSource("strings")
    void string_expressions(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private static Stream<Arguments> arrays() {
        return Stream.of(
                of(new VmTestCase("[]", new int[0])),
                of(new VmTestCase("[1, 2, 3]", IntStream.of(1, 2, 3).toArray())),
                of(new VmTestCase("[1 + 2, 3 * 4, 5 + 6]", IntStream.of(3, 12, 11).toArray()))
        );
    }

    @ParameterizedTest
    @MethodSource("arrays")
    @DisplayName("array expression")
    void array_expression(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private static Stream<Arguments> hashs() {
        return Stream.of(
                of(new VmTestCase("{}", Map.<Integer, Integer>of())),
                of(new VmTestCase("{1: 2, 2: 3}", Map.of(1, 2, 2, 3))),
                of(new VmTestCase("{1 + 1: 2 * 2, 3 + 3: 4 * 4}", Map.of(2, 4, 6, 16)))
        );
    }

    @ParameterizedTest
    @MethodSource("hashs")
    @DisplayName("hash expressions")
    void hash_expressions(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private static Stream<Arguments> indexes() {
        return Stream.of(
                of(new VmTestCase("{}", Map.<Integer, Integer>of())),
                of(new VmTestCase("[1, 2, 3][1]", 2)),
                of(new VmTestCase("[1, 2, 3][0 + 2]", 3)),
                of(new VmTestCase("[[1, 1, 1]][0][0]", 1)),
                of(new VmTestCase("[][0]", NULL)),
                of(new VmTestCase("[1, 2, 3][99]", NULL)),
                of(new VmTestCase("[1][-1]", NULL)),
                of(new VmTestCase("{1: 1, 2: 2}[1]", 1)),
                of(new VmTestCase("{1: 1, 2: 2}[2]", 2)),
                of(new VmTestCase("{1: 1}[0]", NULL)),
                of(new VmTestCase("{}[0]", NULL))
        );
    }

    @ParameterizedTest
    @MethodSource("indexes")
    @DisplayName("indexes")
    void index_expressions(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private void runVirtualMachineTest(VmTestCase vmTestCase) {
        var program = parse(vmTestCase.input());

        var compiler = compiler();
        var compilerError = compiler.compile(program);
        compilerError.ifPresent(err -> fail("compiler error: [{}]", err));

        var virtualMachine = virtualMachine(compiler.bytecode());
        var vmError = virtualMachine.run();
        vmError.ifPresent(err -> fail("vm error: [{}]", err));

        var stackElement = virtualMachine.lastPoppedStackElement();

        testExpectedObject(vmTestCase.expected, stackElement);
    }

    private void testExpectedObject(Object expected, MonkeyObject actual) {
        switch (expected) {
            case Integer integer -> testIntegerObject(actual, integer);
            case String string -> testStringObject(actual, string);
            case Boolean bool -> testBooleanObject(actual, bool);
            case int[] array -> testArrayObject(actual, array);
            case Map<?, ?> map -> testMapObject(actual, map);
            case MonkeyNull monkeyNull -> assertThat(actual).isEqualTo(monkeyNull);
            default -> throw new IllegalStateException("Unexpected value: " + expected);
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

    private void testStringObject(MonkeyObject actual, String expected) {
        assertThat(actual).satisfies(it -> {
            assertThat(it).isInstanceOf(MonkeyString.class);

            var str = (MonkeyString) it;
            assertThat(str.value()).isEqualTo(expected);
        });
    }

    private void testBooleanObject(MonkeyObject actual, boolean expected) {
        assertThat(actual).satisfies(
                obj -> assertThat(obj).isInstanceOf(MonkeyBoolean.class),
                obj -> {
                    var bool = (MonkeyBoolean) obj;
                    assertThat(bool.value()).isEqualTo(expected);
                }
        );
    }

    private void testArrayObject(MonkeyObject actual, int[] expected) {
        assertThat(actual).satisfies(it -> {
            assertThat(it).isInstanceOf(MonkeyArray.class);

            var array = (MonkeyArray) actual;
            assertThat(array.elements().size()).isEqualTo(expected.length);

            for (var i = 0; i < expected.length; i++) {
                testIntegerObject(array.elements().get(i), expected[i]);
            }
        });
    }

    private void testMapObject(MonkeyObject actual, Map<?, ?> expected) {
        assertThat(actual).satisfies(it -> {
            assertThat(it).isInstanceOf(MonkeyHash.class);

            var hash = (MonkeyHash) it;
            assertThat(hash.size()).isEqualTo(expected.size());

            for (var pair : expected.entrySet()) {
                final var key = (int) pair.getKey();
                final var hashPair = hash.getHashPair(new HashKey(INTEGER_OBJ, key));

                assertThat(hashPair).isNotNull();
                testIntegerObject(hashPair.value(), (int) expected.get(key));
            }
        });
    }

    private Program parse(String input) {
        var lexer = lexer(input);
        var parser = new Parser(lexer);
        return parser.parseProgram();
    }
}