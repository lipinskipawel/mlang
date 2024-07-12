package com.github.lipinskipawel.mlang.vm;

import com.github.lipinskipawel.mlang.evaluator.objects.HashKey;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyArray;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBoolean;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyError;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyHash;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyNull;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyString;
import com.github.lipinskipawel.mlang.parser.Parser;
import com.github.lipinskipawel.mlang.parser.ast.Program;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

    private static Stream<Arguments> callFunctionsWithoutArguments() {
        return Stream.of(
                of(new VmTestCase("""
                        let fivePlusTen = fn () { 5 + 10; };
                        fivePlusTen();
                        """, 15)),
                of(new VmTestCase("""
                        let one = fn() { 1; };
                        let two = fn() { 2; };
                        one() + two()
                        """, 3)),
                of(new VmTestCase("""
                        let a = fn() { 1 };
                        let b = fn() { a() + 1 };
                        let c = fn() { b() + 1 };
                        c();
                        """, 3)),
                of(new VmTestCase("""
                        let earlyExit = fn() { return 99; 100; };
                        earlyExit();
                        """, 99)),
                of(new VmTestCase("""
                        let earlyExit = fn() { return 99; return 100; };
                        earlyExit();
                        """, 99))
        );
    }

    @ParameterizedTest
    @MethodSource("callFunctionsWithoutArguments")
    @DisplayName("call functions without arguments")
    void call_functions_without_arguments(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private static Stream<Arguments> callFunctionsWithoutArgumentsAndWithoutReturnValue() {
        return Stream.of(
                of(new VmTestCase("""
                        let noReturn = fn() { };
                        noReturn();
                        """, NULL)),
                of(new VmTestCase("""
                        let noReturn = fn() { };
                        let noReturnTwo = fn() { noReturn(); };
                        noReturn();
                        noReturnTwo();
                        """, NULL))
        );
    }

    @ParameterizedTest
    @MethodSource("callFunctionsWithoutArgumentsAndWithoutReturnValue")
    @DisplayName("call functions without arguments and without return value")
    void call_functions_without_arguments_and_without_return_value(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private static Stream<Arguments> firstClassFunctions() {
        return Stream.of(
                of(new VmTestCase("""
                        let returnsOne = fn() { 1; };
                        let returnsOneReturner = fn() { returnsOne; };
                        returnsOneReturner()();
                        """, 1)),
                of(new VmTestCase("""
                        let returnsOneReturner = fn() {
                          let returnsOne = fn() { 1; };
                          returnsOne;
                        };
                        returnsOneReturner()();
                        """, 1))
        );
    }

    @ParameterizedTest
    @MethodSource("firstClassFunctions")
    @DisplayName("first class function")
    void first_class_function(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private static Stream<Arguments> callClosureWithBindings() {
        return Stream.of(
                of(new VmTestCase("""
                        let one = fn() { let one = 1; one };
                        one();
                        """, 1)),
                of(new VmTestCase("""
                        let oneAndTwo = fn() { let one = 1; let two = 2; one + two; };
                        oneAndTwo();
                        """, 3)),
                of(new VmTestCase("""
                        let oneAndTwo = fn() { let one = 1; let two = 2; one + two; };
                        let threeAndFour = fn() { let three = 3; let four = 4; three + four; };
                        oneAndTwo() + threeAndFour();
                        """, 10)),
                of(new VmTestCase("""
                        let firstFoobar = fn() { let foobar = 50; foobar; };
                        let secondFoobar = fn() { let foobar = 100; foobar; };
                        firstFoobar() + secondFoobar();
                        """, 150)),
                of(new VmTestCase("""
                        let globalSeed = 50;
                        let minusOne = fn() {
                            let num = 1;
                            globalSeed - num;
                        }
                        let minusTwo = fn() {
                            let num = 2;
                            globalSeed - num;
                        }
                        minusOne() + minusTwo();
                        """, 97))
        );
    }

    @ParameterizedTest
    @MethodSource("callClosureWithBindings")
    @DisplayName("call function with bindings")
    void call_functions_with_binding(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private static Stream<Arguments> callClosureWithArgumentsAndBindings() {
        return Stream.of(
                of(new VmTestCase("""
                        let identity = fn(a) { a; };
                        identity(4);
                        """, 4)),
                of(new VmTestCase("""
                        let sum = fn(a, b) { a + b; };
                        sum(1, 2);
                        """, 3)),
                of(new VmTestCase("""
                        let sum = fn(a, b) {
                            let c = a + b;
                            c;
                        };
                        sum(1, 2);
                        """, 3)),
                of(new VmTestCase("""
                        let sum = fn(a, b) {
                            let c = a + b;
                            c;
                        };
                        sum(1, 2) + sum(3, 4);
                        """, 10)),
                of(new VmTestCase("""
                        let sum = fn(a, b) {
                            let c = a + b;
                            c;
                        };
                        let outer = fn() {
                            sum(1, 2) + sum(3, 4);
                        };
                        outer();
                        """, 10)),
                of(new VmTestCase("""
                        let globalNum = 10;

                        let sum = fn(a, b) {
                            let c = a + b;
                            c + globalNum;
                        };

                        let outer = fn() {
                            sum(1, 2) + sum(3, 4) + globalNum;
                        };

                        outer() + globalNum;
                        """, 50))
        );
    }

    @ParameterizedTest
    @MethodSource("callClosureWithArgumentsAndBindings")
    @DisplayName("call function with arguments and bindings")
    void call_function_with_arguments_and_bindings(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private static Stream<Arguments> callClosureWithWrongNumberOfArguments() {
        return Stream.of(
                of(new VmTestCase("fn() { 1; }(1);", "wrong number of arguments want=0, got=1")),
                of(new VmTestCase("fn(a) { a; }()", "wrong number of arguments want=1, got=0")),
                of(new VmTestCase("fn(a, b) { a + b; }(1);", "wrong number of arguments want=2, got=1"))
        );
    }

    @ParameterizedTest
    @MethodSource("callClosureWithWrongNumberOfArguments")
    void call_function_with_wrong_number_of_arguments(VmTestCase vmTestCase) {
        var program = parse(vmTestCase.input());

        var compiler = compiler();
        var compilerError = compiler.compile(program);
        compilerError.ifPresent(err -> fail("compiler error: [{}]", err));

        var virtualMachine = virtualMachine(compiler.bytecode());

        var vmError = catchException(virtualMachine::run);

        assertThat(vmError)
                .isInstanceOf(RuntimeException.class)
                .hasMessage(vmTestCase.expected.toString());
    }

    private static Stream<Arguments> builtinFunctions() {
        return Stream.of(
                of(new VmTestCase("len(\"\")", 0)),
                of(new VmTestCase("len(\"four\")", 4)),
                of(new VmTestCase("len(\"Hello world\")", 11)),
                of(new VmTestCase("len(1)", new MonkeyError("argument to 'len' not supported, got INTEGER"))),
                of(new VmTestCase("len(\"one\", \"two\")", new MonkeyError("wrong number of arguments. got=2, want=1"))),
                of(new VmTestCase("len([1, 2, 3])", 3)),
                of(new VmTestCase("len([])", 0)),
                of(new VmTestCase("puts(\"hello\", \"world !\")", NULL)),
                of(new VmTestCase("first([1, 2, 3])", 1)),
                of(new VmTestCase("first([])", NULL)),
                of(new VmTestCase("first(1)", new MonkeyError("argument to 'first' must be ARRAY, got INTEGER"))),
                of(new VmTestCase("last([1, 2, 3])", 3)),
                of(new VmTestCase("last([])", NULL)),
                of(new VmTestCase("last(1)", new MonkeyError("argument to 'last' must be ARRAY, got INTEGER"))),
                of(new VmTestCase("rest([1, 2, 3])", new int[]{2, 3})),
                of(new VmTestCase("rest([])", NULL)),
                of(new VmTestCase("push([], 1)", new int[]{1})),
                of(new VmTestCase("push(1, 1)", new MonkeyError("argument to 'push' must be ARRAY, got INTEGER")))
        );
    }

    @ParameterizedTest
    @MethodSource("builtinFunctions")
    @DisplayName("builtin functions")
    void builtin_functions(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private static Stream<Arguments> closures() {
        return Stream.of(
                of(new VmTestCase("""
                        let newClosure = fn(a) {
                          fn() { a; };
                        };
                        let closure = newClosure(99);
                        closure();
                        """, 99)),
                of(new VmTestCase("""
                        let newAdder = fn(a, b) {
                            fn(c) { a + b + c };
                        };
                        let adder = newAdder(1, 2);
                        adder(8);
                        """, 11)),
                of(new VmTestCase("""
                        let newAdder = fn(a, b) {
                            let c = a + b;
                            fn(d) { c + d };
                        };
                        let adder = newAdder(1, 2);
                        adder(8);
                        """, 11)),
                of(new VmTestCase("""
                        let newAdderOuter = fn(a, b) {
                            let c = a + b;
                            fn(d) {
                                let e = d + c;
                                fn(f) { e + f; };
                            };
                        };
                        let newAdderInner = newAdderOuter(1, 2)
                        let adder = newAdderInner(3);
                        adder(8);
                        """, 14)),
                of(new VmTestCase("""
                        let a = 1;
                        let newAdderOuter = fn(b) {
                            fn(c) {
                                fn(d) { a + b + c + d };
                            };
                        };
                        let newAdderInner = newAdderOuter(2)
                        let adder = newAdderInner(3);
                        adder(8);
                        """, 14)),
                of(new VmTestCase("""
                        let newClosure = fn(a, b) {
                            let one = fn() { a; };
                            let two = fn() { b; };
                            fn() { one() + two(); };
                        };
                        let closure = newClosure(9, 90);
                        closure();
                        """, 99))
        );
    }

    @ParameterizedTest
    @MethodSource("closures")
    @DisplayName("closures")
    void closure(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    private static Stream<Arguments> recursiveFunctions() {
        return Stream.of(
                of(new VmTestCase("""
                        let countDown = fn(x) {
                          if (x == 0) {
                            return 0;
                          } else {
                            return countDown(x - 1);
                          }
                        };
                        countDown(1);
                        """, 0)),
                of(new VmTestCase("""
                        let countDown = fn(x) {
                            if (x == 0) {
                                return 0;
                            } else {
                                countDown(x - 1);
                            }
                        };
                        let wrapper = fn() {
                            countDown(1);
                        };
                        wrapper();
                        """, 0)),
                of(new VmTestCase("""
                        let wrapper = fn() {
                            let countDown = fn(x) {
                                if (x == 0) {
                                    return 0;
                                } else {
                                    countDown(x - 1);
                                }
                            };
                            countDown(1);
                        };
                        wrapper();
                        """, 0))
        );
    }

    @ParameterizedTest
    @MethodSource("recursiveFunctions")
    @DisplayName("recursive functions")
    void recursive_functions(VmTestCase vmTestCase) {
        runVirtualMachineTest(vmTestCase);
    }

    @Test
    void recursive_fibonacci() {
        runVirtualMachineTest(new VmTestCase("""
                let fibonacci = fn(x) {
                    if (x == 0) {
                        return 0;
                    } else {
                        if (x == 1) {
                            return 1;
                        } else {
                            fibonacci(x - 1) + fibonacci(x - 2);
                        }
                    }
                };
                fibonacci(15);
                """, 610));
    }

    private void runVirtualMachineTest(VmTestCase vmTestCase) {
        var program = parse(vmTestCase.input());

        var compiler = compiler();
        var compilerError = compiler.compile(program);
        compilerError.ifPresent(err -> fail("compiler error: [{}]", err));

        var virtualMachine = virtualMachine(compiler.bytecode());
        virtualMachine.run();

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
            case MonkeyError monkeyError -> assertThat(actual).isEqualTo(monkeyError);
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