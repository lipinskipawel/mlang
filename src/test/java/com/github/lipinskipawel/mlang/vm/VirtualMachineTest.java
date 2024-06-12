package com.github.lipinskipawel.mlang.vm;

import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBoolean;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyNull;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.parser.Parser;
import com.github.lipinskipawel.mlang.parser.ast.Program;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.github.lipinskipawel.mlang.compiler.Compiler.compiler;
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
            case Boolean bool -> testBooleanObject(actual, bool);
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

    private void testBooleanObject(MonkeyObject actual, boolean expected) {
        assertThat(actual).satisfies(
                obj -> assertThat(obj).isInstanceOf(MonkeyBoolean.class),
                obj -> {
                    var bool = (MonkeyBoolean) obj;
                    assertThat(bool.value()).isEqualTo(expected);
                }
        );
    }

    private Program parse(String input) {
        var lexer = lexer(input);
        var parser = new Parser(lexer);
        return parser.parseProgram();
    }
}