package com.github.lipinskipawel.mlang.evaluator;

import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyArray;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBoolean;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyError;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyFunction;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyHash;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyNull;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyString;
import com.github.lipinskipawel.mlang.parser.Parser;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.lipinskipawel.mlang.evaluator.Evaluator.evaluator;
import static com.github.lipinskipawel.mlang.lexer.Lexer.lexer;
import static org.junit.jupiter.params.provider.Arguments.arguments;

final class EvaluatorTest implements WithAssertions {

    static Stream<Arguments> integers() {
        return Stream.of(
                arguments("5", 5),
                arguments("10", 10),
                arguments("-5", -5),
                arguments("-10", -10),
                arguments("5 + 5 + 5 + 5 - 10", 10),
                arguments("2 * 2 * 2 * 2 * 2", 32),
                arguments("-50 + 100 + -50", 0),
                arguments("5 * 2 + 10", 20),
                arguments("5 + 2 * 10", 25),
                arguments("20 + 2 * -10", 0),
                arguments("50 / 2 * 2 + 10", 60),
                arguments("2 * (5 + 10)", 30),
                arguments("3 * 3 * 3 + 10", 37),
                arguments("3 * (3 * 3) + 10", 37),
                arguments("(5 + 10 * 2 + 15 / 3) * 2 + -10", 50)
        );
    }

    @ParameterizedTest
    @MethodSource("integers")
    void should_eval_integer_expression(String input, int expected) {
        var evaluated = testEval(input);

        testIntegerObject(evaluated, expected);
    }

    static Stream<Arguments> booleans() {
        return Stream.of(
                arguments("true", true),
                arguments("false", false),
                arguments("1 < 2", true),
                arguments("1 > 2", false),
                arguments("1 < 1", false),
                arguments("1 > 1", false),
                arguments("1 == 1", true),
                arguments("1 != 1", false),
                arguments("1 == 2", false),
                arguments("1 != 2", true),
                arguments("true == true", true),
                arguments("false == false", true),
                arguments("true == false", false),
                arguments("true != false", true),
                arguments("false != true", true),
                arguments("(1 < 2) == true", true),
                arguments("(1 < 2) == false", false),
                arguments("(1 > 2) == true", false),
                arguments("(1 > 2) == false", true)
        );
    }

    @ParameterizedTest
    @MethodSource("booleans")
    void should_eval_boolean_expression(String input, boolean expected) {
        var evaluated = testEval(input);

        testBooleanObject(evaluated, expected);
    }

    static Stream<Arguments> bangs() {
        return Stream.of(
                arguments("!true", false),
                arguments("!false", true),
                arguments("!5", false),
                arguments("!!true", true),
                arguments("!!false", false),
                arguments("!!5", true)
        );
    }

    @ParameterizedTest
    @MethodSource("bangs")
    void should_eval_bang_operator(String input, boolean expected) {
        var evaluated = testEval(input);

        testBooleanObject(evaluated, expected);
    }

    static Stream<Arguments> ifElse() {
        return Stream.of(
                arguments("if (true) { 10 }", 10),
                arguments("if (false) { 10 }", null),
                arguments("if (1) { 10 }", 10),
                arguments("if (1 < 2) { 10 }", 10),
                arguments("if (1 > 2) { 10 }", null),
                arguments("if (1 > 2) { 10 } else { 20 }", 20),
                arguments("if (1 < 2) { 10 } else { 20 }", 10)
        );
    }

    @ParameterizedTest
    @MethodSource("ifElse")
    void should_eval_if_else_expression(String input, Object expected) {
        var evaluated = testEval(input);
        if (expected != null) {
            testIntegerObject(evaluated, (int) expected);
            return;
        }
        testNullObject(evaluated);
    }

    static Stream<Arguments> returnStatements() {
        return Stream.of(
                arguments("return 10;", 10),
                arguments("return 10; 9;", 10),
                arguments("return 2 * 5; 9;", 10),
                arguments("9; return 2 * 5; 9;", 10),
                arguments("""
                        if (10 > 1) {
                          if (10 > 1) {
                            return 10;
                          }

                          return 1;
                        }""", 10),
                arguments("""
                        let f = fn(x) {
                          return x;
                          x + 10;
                        };
                        f(10);""", 10),
                arguments("""
                        let f = fn(x) {
                          let result = x + 10;
                          return result;
                          return 10;
                        };
                        f(10);""", 20)
        );
    }

    @ParameterizedTest
    @MethodSource("returnStatements")
    void should_eval_return_statements(String input, int expected) {
        var evaluated = testEval(input);

        testIntegerObject(evaluated, expected);
    }

    static Stream<Arguments> errors() {
        return Stream.of(
                arguments("5 + true;", "type mismatch: INTEGER + BOOLEAN"),
                arguments("5 + true; 5;", "type mismatch: INTEGER + BOOLEAN"),
                arguments("-true", "unknown operator: -BOOLEAN"),
                arguments("true + false;", "unknown operator: BOOLEAN + BOOLEAN"),
                arguments("5; true + false; 5", "unknown operator: BOOLEAN + BOOLEAN"),
                arguments("if (10 > 1) { true + false; }", "unknown operator: BOOLEAN + BOOLEAN"),
                arguments("""
                        if (10 > 1) {
                            if (10 > 1) {
                                return true + false;
                            }
                            return 1;
                        }
                        """, "unknown operator: BOOLEAN + BOOLEAN"),
                arguments("foobar", "identifier not found: foobar"),
                arguments("""
                        "hello" - "world"
                        """, "unknown operator: STRING - STRING"),
                arguments("""
                        {"name": "Monkey"}[fn(x) { x }];
                        """, "unusable as hash key: FUNCTION")
        );
    }

    @ParameterizedTest
    @MethodSource("errors")
    void should_eval_error_handler(String input, String expected) {
        var evaluated = testEval(input);

        assertThat(evaluated).satisfies(
                eval -> assertThat(eval).isInstanceOf(MonkeyError.class),
                eval -> {
                    var errorMsg = (MonkeyError) eval;
                    assertThat(errorMsg.message()).isEqualTo(expected);
                }
        );
    }

    static Stream<Arguments> letStatements() {
        return Stream.of(
                arguments("let a = 5; a;", 5),
                arguments("let a = 5 * 5; a;", 25),
                arguments("let a = 5; let b = a; b;", 5),
                arguments("let a = 5; let b = a; let c = a + b + 5; c;", 15)
        );
    }

    @ParameterizedTest
    @MethodSource("letStatements")
    void should_eval_let_statements(String input, int expected) {
        var evaluated = testEval(input);

        testIntegerObject(evaluated, expected);
    }

    @Test
    void should_eval_functions() {
        var input = "fn(x) { x + 2; };";

        var evaluated = testEval(input);

        assertThat(evaluated).satisfies(
                output -> assertThat(output).isInstanceOf(MonkeyFunction.class),
                output -> {
                    var fn = (MonkeyFunction) output;
                    assertThat(fn.parameters().size()).isEqualTo(1);
                    assertThat(fn.parameters().get(0).string()).isEqualTo("x");
                    assertThat(fn.block().string()).isEqualTo("(x + 2)");
                }
        );
    }

    static Stream<Arguments> functions() {
        return Stream.of(
                arguments("let identity = fn(x) { x; }; identity(5);", 5),
                arguments("let identity = fn(x) { return x; }; identity(5);", 5),
                arguments("let double = fn(x) { x * 2; }; double(5);", 10),
                arguments("let add = fn(x, y) { x + y; }; add(5, 5);", 10),
                arguments("let add = fn(x, y) { x + y; }; add(5 + 5, add(5, 5));", 20)
        );
    }

    @ParameterizedTest
    @MethodSource("functions")
    void should_eval_function_calls(String input, int expected) {
        var evaluated = testEval(input);

        testIntegerObject(evaluated, expected);
    }

    private void testNullObject(MonkeyObject object) {
        assertThat(object).isInstanceOf(MonkeyNull.class);
    }

    @Test
    void should_eval_closures() {
        var input = """
                let newAdder = fn(x) {
                  fn(y) { x + y; };
                }

                let addTwo = newAdder(2);
                addTwo(2);
                """;

        var evaluated = testEval(input);

        testIntegerObject(evaluated, 4);
    }

    @Test
    void should_evaluate_strings() {
        var input = """
                "Hello world";
                """;

        var evaluated = testEval(input);

        testStringObject(evaluated, "Hello world");
    }

    @Test
    void should_evaluate_string_concatenation() {
        var input = """
                "Hello" + " " + "world";
                """;

        var evaluated = testEval(input);

        testStringObject(evaluated, "Hello world");
    }

    static Stream<Arguments> builtin() {
        return Stream.of(
                arguments("len(\"\")", 0),
                arguments("len(\"four\")", 4),
                arguments("len(\"hello world\")", 11),
                arguments("len(1)", "argument to [len] not supported, got INTEGER"),
                arguments("len(\"one\", \"two\")", "wrong number of arguments. got=2, want=1"),
                arguments("len([1, 2, 3])", 3),
                arguments("len([])", 0),
                arguments("first([1, 2, 3])", 1),
                arguments("first([])", null),
                arguments("first(1)", "argument to 'first' must be ARRAY, got INTEGER"),
                arguments("last([1, 2, 3])", 3),
                arguments("last([])", null),
                arguments("last(1)", "argument to 'last' must be ARRAY, got INTEGER"),
                arguments("rest([1, 2, 3])", List.of(2, 3)),
                arguments("rest([])", null),
                arguments("push([], 1)", List.of(1)),
                arguments("push(1, 1)", "argument to 'push' must be ARRAY, got INTEGER")
        );
    }

    @ParameterizedTest
    @MethodSource("builtin")
    @SuppressWarnings("unchecked")
    void should_evaluate_builtin_functions(String input, Object expected) {
        var evaluated = testEval(input);

        switch (expected) {
            case null -> testNullObject(evaluated);
            case Integer integer -> testIntegerObject(evaluated, integer);
            case String __ -> assertThat(evaluated).satisfies(
                    object -> assertThat(object).isInstanceOf(MonkeyError.class),
                    object -> {
                        var error = (MonkeyError) object;
                        assertThat(error.message()).isEqualTo(expected);
                    }
            );
            case List list -> {
                final List<Integer> integers = list;
                final var evaluatedArray = (MonkeyArray) evaluated;

                assertThat(evaluatedArray).satisfies(
                        array -> assertThat(array.elements().size()).isEqualTo(integers.size()),
                        array -> {
                            for (var i = 0; i < integers.size(); i++) {
                                testIntegerObject(array.elements().get(i), integers.get(i));
                            }
                        }
                );
            }
            default -> fail("recheck test case input parameters");
        }
    }

    @Test
    void should_evaluate_array_literal() {
        var input = "[1, 2 * 2, 3 + 3]";

        var evaluated = testEval(input);

        assertThat(evaluated).isInstanceOf(MonkeyArray.class);
        var elements = ((MonkeyArray) evaluated).elements();

        assertThat(elements.size()).isEqualTo(3);
        testIntegerObject(elements.get(0), 1);
        testIntegerObject(elements.get(1), 4);
        testIntegerObject(elements.get(2), 6);
    }

    static Stream<Arguments> arrayIndex() {
        return Stream.of(
                arguments("[1, 2, 3][0]", 1),
                arguments("[1, 2, 3][1]", 2),
                arguments("[1, 2, 3][2]", 3),
                arguments("let i = 0; [1][i];", 1),
                arguments("[1, 2, 3][1 + 1];", 3),
                arguments("let myArray = [1, 2, 3]; myArray[2];", 3),
                arguments("let myArray = [1, 2, 3]; myArray[0] + myArray[1] + myArray[2];", 6),
                arguments("let myArray = [1, 2, 3]; let i = myArray[0]; myArray[i]", 2),
                arguments("[1, 2, 3][3]", null),
                arguments("[1, 2, 3][-1]", null)
        );
    }

    @ParameterizedTest
    @MethodSource("arrayIndex")
    void should_evaluate_array_index_expression(String input, Object expected) {
        var evaluated = testEval(input);

        switch (expected) {
            case Integer integer -> testIntegerObject(evaluated, integer);
            case null -> testNullObject(evaluated);
            default -> fail("recheck test case input parameters");
        }
    }

    @Test
    void should_evaluate_hash_literal() {
        var input = """
                let two = "two";
                {
                  "one": 10 - 9,
                  two: 1 + 1,
                  "thr" + "ee": 6 / 2,
                  4: 4,
                  true: 5,
                  false: 6
                }""";
        var expected = Map.of(
                new MonkeyString("one").hashKey(), 1,
                new MonkeyString("two").hashKey(), 2,
                new MonkeyString("three").hashKey(), 3,
                new MonkeyInteger(4).hashKey(), 4,
                new MonkeyBoolean(true).hashKey(), 5,
                new MonkeyBoolean(false).hashKey(), 6
        );

        var evaluated = testEval(input);

        assertThat(evaluated).isInstanceOf(MonkeyHash.class);
        var monkeyHash = ((MonkeyHash) evaluated);

        assertThat(monkeyHash.size()).isEqualTo(expected.size());
        for (var entry : expected.entrySet()) {
            assertThat(monkeyHash.getHashPair(entry.getKey()).value()).isNotNull();
            testIntegerObject(monkeyHash.getHashPair(entry.getKey()).value(), entry.getValue());
        }
    }

    static Stream<Arguments> hashIndex() {
        return Stream.of(
                arguments("""
                        {"foo": 5}["foo"]""", 5),
                arguments("""
                        {"foo": 5}["bar"]""", null),
                arguments("""
                        let key = "foo"; {
                          "foo":5
                        }[key]""", 5),
                arguments("""
                        {}["foo"]""", null),
                arguments("""
                        {5:5}[5]""", 5),
                arguments("""
                        {true:5}[true]""", 5),
                arguments("""
                        {false:5}[false]""", 5)
        );
    }

    @ParameterizedTest
    @MethodSource("hashIndex")
    void should_evaluate_hash_index_expression(String input, Object expected) {
        var evaluated = testEval(input);

        switch (expected) {
            case Integer integer -> testIntegerObject(evaluated, integer);
            case null -> testNullObject(evaluated);
            default -> fail("recheck test case input parameters");
        }
    }

    private MonkeyObject testEval(String input) {
        var lexer = lexer(input);
        var parser = new Parser(lexer);
        var program = parser.parseProgram();
        var env = new Environment();

        var evaluator = evaluator();
        return evaluator.eval(program, env);
    }

    private void testIntegerObject(MonkeyObject monkeyObject, int expected) {
        assertThat(monkeyObject).satisfies(
                object -> assertThat(object).isInstanceOf(MonkeyInteger.class),
                object -> {
                    var integer = (MonkeyInteger) object;
                    assertThat(integer.value()).isEqualTo(expected);
                }
        );
    }

    private void testBooleanObject(MonkeyObject monkeyObject, boolean expected) {
        assertThat(monkeyObject).satisfies(
                object -> assertThat(object).isInstanceOf(MonkeyBoolean.class),
                object -> {
                    var bool = (MonkeyBoolean) object;
                    assertThat(bool.value()).isEqualTo(expected);
                }
        );
    }

    private void testStringObject(MonkeyObject monkeyObject, String expected) {
        assertThat(monkeyObject).satisfies(
                object -> assertThat(object).isInstanceOf(MonkeyString.class),
                object -> {
                    var string = (MonkeyString) object;
                    assertThat(string.value()).isEqualTo(expected);
                }
        );
    }

    @Test
    void should_construct_reduce_and_sum_functions() {
        var input = """
                let reduce = fn(arr, initial, fun) {
                  let iter = fn(arr, result) {
                    if (len(arr) == 0) {
                      result;
                    } else {
                      return iter(rest(arr), fun(result, first(arr)));
                    }
                  };

                  iter(arr, initial);
                };

                let sum = fn(arr) {
                  reduce(arr, 0, fn(initial, el) { initial + el; });
                };

                let a = [1, 2, 3];
                let double = fn(x) { x * 2; };

                let summed = sum([1, 2, 3]);
                """;

        var evaluated = testEval(input);

        testIntegerObject(evaluated, 6);
    }

    @Test
    void should_construct_map_functions() {
        var input = """
                let map = fn(arr, fun) {
                  let iter = fn(arr, accumulated) {
                    if (len(arr) == 0) {
                      accumulated;
                    } else {
                      iter(rest(arr), push(accumulated, fun(first(arr))));
                    }
                  };

                  iter(arr, []);
                };

                let a = [1, 2, 9];
                let double = fn(x) { x * 2; };

                map(a, double);
                """;

        var evaluated = testEval(input);

        assertThat(evaluated).isInstanceOf(MonkeyArray.class);
        var elements = ((MonkeyArray) evaluated).elements();

        assertThat(elements.size()).isEqualTo(3);
        testIntegerObject(elements.get(0), 2);
        testIntegerObject(elements.get(1), 4);
        testIntegerObject(elements.get(2), 18);
    }
}
