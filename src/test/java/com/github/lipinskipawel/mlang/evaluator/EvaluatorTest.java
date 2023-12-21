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

import static com.github.lipinskipawel.mlang.evaluator.Evaluator.eval;
import static com.github.lipinskipawel.mlang.lexer.Lexer.lexer;
import static org.junit.jupiter.params.provider.Arguments.arguments;

final class EvaluatorTest implements WithAssertions {

    static Stream<Arguments> integers() {
        return Stream.of(
                arguments("5", 5),
                arguments("10", 10)
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
                arguments("false", false)
        );
    }

    @ParameterizedTest
    @MethodSource("booleans")
    void should_eval_boolean_expression(String input, boolean expected) {
        var evaluated = testEval(input);

        testBooleanObject(evaluated, expected);
    }

    private MonkeyObject testEval(String input) {
        var lexer = lexer(input);
        var parser = new Parser(lexer);
        var program = parser.parseProgram();

        return eval(program);
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
