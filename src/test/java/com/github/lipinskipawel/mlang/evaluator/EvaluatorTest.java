package com.github.lipinskipawel.mlang.evaluator;

import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBoolean;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.parser.Parser;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

    private MonkeyObject testEval(String input) {
        var lexer = lexer(input);
        var parser = new Parser(lexer);
        var program = parser.parseProgram();

        var evaluator = evaluator();
        return evaluator.eval(program);
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
}
