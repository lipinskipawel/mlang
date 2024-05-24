package com.github.lipinskipawel.mlang.vm;

import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
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
import static com.github.lipinskipawel.mlang.vm.VirtualMachine.virtualMachine;
import static org.junit.jupiter.params.provider.Arguments.of;

@DisplayName("Virtual Machine Spec")
class VirtualMachineTest implements WithAssertions {

    private record VmTestCase(
            String input,
            Object expected
    ) {
    }

    private static Stream<Arguments> tests() {
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
                of(new VmTestCase("5 * (2 + 10)", 60))
        );
    }

    @ParameterizedTest
    @MethodSource("tests")
    @DisplayName("integer arithmetic")
    void integer_arithmetic(VmTestCase vmTestCase) {
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

    private Program parse(String input) {
        var lexer = lexer(input);
        var parser = new Parser(lexer);
        return parser.parseProgram();
    }
}