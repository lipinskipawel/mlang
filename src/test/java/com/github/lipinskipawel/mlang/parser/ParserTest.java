package com.github.lipinskipawel.mlang.parser;

import com.github.lipinskipawel.mlang.ast.expression.BooleanExpression;
import com.github.lipinskipawel.mlang.ast.expression.Expression;
import com.github.lipinskipawel.mlang.ast.expression.Identifier;
import com.github.lipinskipawel.mlang.ast.expression.InfixExpression;
import com.github.lipinskipawel.mlang.ast.expression.IntegerLiteral;
import com.github.lipinskipawel.mlang.ast.expression.PrefixExpression;
import com.github.lipinskipawel.mlang.ast.statement.ExpressionStatement;
import com.github.lipinskipawel.mlang.ast.statement.LetStatement;
import com.github.lipinskipawel.mlang.ast.statement.ReturnStatement;
import com.github.lipinskipawel.mlang.ast.statement.Statement;
import com.github.lipinskipawel.mlang.token.Token;
import com.github.lipinskipawel.mlang.token.TokenType;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.github.lipinskipawel.mlang.ast.Program.givenProgram;
import static com.github.lipinskipawel.mlang.lexer.Lexer.lexer;
import static com.github.lipinskipawel.mlang.token.TokenType.IDENT;
import static org.junit.jupiter.params.provider.Arguments.arguments;

final class ParserTest implements WithAssertions {

    @Test
    void should_parse_let_statement_without_expression() {
        var sourceCode = """
                let x = 5;
                let y = 10;
                let foobar = 838383;
                """;
        var expectedIdentifiers = List.of("x", "y", "foobar");
        var lexer = lexer(sourceCode);
        var parser = new Parser(lexer);

        var program = parser.parseProgram();
        checkParseErrors(parser);

        assertThat(program).isNotNull();
        assertThat(program.programStatements().size()).isEqualTo(3);

        for (var i = 0; i < 3; i++) {
            var statement = program.programStatements().get(i);
            var expectedIdentifier = expectedIdentifiers.get(i);
            testLetStatement(statement, expectedIdentifier);
        }
    }

    private void testLetStatement(Statement statement, String name) {
        if (!statement.tokenLiteral().equals("let")) {
            fail("statement.tokenLiteral() not 'let'. got=%s", statement.tokenLiteral());
        }
        if (!(statement instanceof LetStatement)) {
            fail("statement not a %s. got=%s", LetStatement.class, statement.getClass());
        }
        assertThat(statement).isInstanceOf(LetStatement.class);
        var letStatement = (LetStatement) statement;
        if (!letStatement.name().value().equals(name)) {
            fail("letStatement.name.value not '%s'. got=%s", name, letStatement.name().value());
        }
        if (!letStatement.name().tokenLiteral().equals(name)) {
            fail("letStatement.name.tokenLiteral not '%s'. got=%s", name, letStatement.name().tokenLiteral());
        }
    }

    @Test
    void should_parse_return_statement_without_expression() {
        var sourceCode = """
                return 5;
                return 10;
                return 9999;
                """;
        var lexer = lexer(sourceCode);
        var parser = new Parser(lexer);

        var program = parser.parseProgram();
        checkParseErrors(parser);

        assertThat(program).isNotNull();
        assertThat(program.programStatements().size()).isEqualTo(3);

        for (var i = 0; i < 3; i++) {
            var statement = program.programStatements().get(i);
            assertThat(statement).isInstanceOf(ReturnStatement.class);
            var returnStatement = (ReturnStatement) statement;
            assertThat(returnStatement.tokenLiteral()).isEqualTo("return");
        }
    }

    @Test
    void should_print_simple_statement() {
        var program = givenProgram(List.of(
                new LetStatement(
                        new Token(TokenType.LET, "let"),
                        new Identifier(
                                new Token(IDENT, "myVar"),
                                "myVar"),
                        new Identifier(
                                new Token(IDENT, "anotherValue"),
                                "anotherValue"
                        )
                )
        ));

        assertThat(program.string()).isEqualTo("let myVar = anotherValue;");
    }

    @Test
    void should_parse_identifier_expression() {
        var input = "foobar;";
        var lexer = lexer(input);
        var parser = new Parser(lexer);

        var program = parser.parseProgram();
        checkParseErrors(parser);

        assertThat(program.programStatements().size()).isEqualTo(1);

        var statement = program.programStatements().get(0);
        assertThat(statement).isInstanceOf(ExpressionStatement.class);

        var expressionStatement = (ExpressionStatement) statement;
        testLiteralExpression(expressionStatement.expression(), "foobar");
    }

    @Test
    void should_parse_integer_literals_expression() {
        var input = "5;";
        var lexer = lexer(input);
        var parser = new Parser(lexer);

        var program = parser.parseProgram();
        checkParseErrors(parser);

        assertThat(program.programStatements().size()).isEqualTo(1);

        var statement = program.programStatements().get(0);
        assertThat(statement).isInstanceOf(ExpressionStatement.class);

        var expressionStatement = (ExpressionStatement) statement;
        testLiteralExpression(expressionStatement.expression(), 5);
    }

    static Stream<Arguments> prefixExpressions() {
        return Stream.of(
                arguments("!5;", "!", 5),
                arguments("-15;", "-", 15),
                arguments("!true;", "!", true),
                arguments("!false;", "!", false)
        );
    }

    @ParameterizedTest
    @MethodSource("prefixExpressions")
    void should_parse_prefix_expression(String input, String operator, Object integerValue) {
        var lexer = lexer(input);
        var parser = new Parser(lexer);

        var program = parser.parseProgram();
        checkParseErrors(parser);

        assertThat(program.programStatements().size()).isEqualTo(1);

        var statement = program.programStatements().get(0);
        assertThat(statement).isInstanceOf(ExpressionStatement.class);

        var expressionStatement = (ExpressionStatement) statement;
        assertThat(expressionStatement.expression()).isInstanceOf(PrefixExpression.class);

        var prefixExpression = (PrefixExpression) expressionStatement.expression();
        assertThat(prefixExpression.operator()).isEqualTo(operator);
        testLiteralExpression(prefixExpression.right(), integerValue);
    }

    static Stream<Arguments> infixExpressions() {
        return Stream.of(
                arguments("5 + 5;", 5, "+", 5),
                arguments("5 - 5;", 5, "-", 5),
                arguments("5 * 5;", 5, "*", 5),
                arguments("5 / 5;", 5, "/", 5),
                arguments("5 > 5;", 5, ">", 5),
                arguments("5 < 5;", 5, "<", 5),
                arguments("5 == 5;", 5, "==", 5),
                arguments("5 != 5;", 5, "!=", 5),
                arguments("true == true", true, "==", true),
                arguments("true != false", true, "!=", false),
                arguments("false == false", false, "==", false)
        );
    }

    @ParameterizedTest
    @MethodSource("infixExpressions")
    void should_parse_infix_expression(String input, Object leftValue, String operator, Object rightValue) {
        var lexer = lexer(input);
        var parser = new Parser(lexer);

        var program = parser.parseProgram();
        checkParseErrors(parser);

        assertThat(program.programStatements().size()).isEqualTo(1);

        var statement = program.programStatements().get(0);
        assertThat(statement).isInstanceOf(ExpressionStatement.class);

        var expressionStatement = (ExpressionStatement) statement;
        assertThat(expressionStatement.expression()).isInstanceOf(InfixExpression.class);

        var infixExpression = (InfixExpression) expressionStatement.expression();
        testInfixExpression(infixExpression, leftValue, operator, rightValue);
    }

    static Stream<Arguments> precedenceTestCases() {
        return Stream.of(
                arguments("-a * b", "((-a) * b)"),
                arguments("!-a", "(!(-a))"),
                arguments("a + b + c", "((a + b) + c)"),
                arguments("a + b - c", "((a + b) - c)"),
                arguments("a * b * c", "((a * b) * c)"),
                arguments("a * b / c", "((a * b) / c)"),
                arguments("a + b / c", "(a + (b / c))"),
                arguments("a + b * c + d / e - f", "(((a + (b * c)) + (d / e)) - f)"),
                arguments("3 + 4; -5 * 5", "(3 + 4)((-5) * 5)"),
                arguments("5 > 4 == 3 < 4", "((5 > 4) == (3 < 4))"),
                arguments("5 < 4 != 3 > 4", "((5 < 4) != (3 > 4))"),
                arguments("3 + 4 * 5 == 3 * 1 + 4 * 5", "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))"),
                arguments("true", "true"),
                arguments("false", "false"),
                arguments("3 > 5 == false", "((3 > 5) == false)"),
                arguments("3 < 5 == true", "((3 < 5) == true)"),

                arguments("1 + (2 + 3) + 4", "((1 + (2 + 3)) + 4)"),
                arguments("(5 + 5) * 2", "((5 + 5) * 2)"),
                arguments("2 / (5 + 5)", "(2 / (5 + 5))"),
                arguments("-(5 + 5)", "(-(5 + 5))"),
                arguments("!(true == true)", "(!(true == true))")
        );
    }

    @ParameterizedTest
    @MethodSource("precedenceTestCases")
    void should_parse_operator_precedence_correctly(String input, String expected) {
        var lexer = lexer(input);
        var parser = new Parser(lexer);

        var program = parser.parseProgram();
        checkParseErrors(parser);

        assertThat(program.string()).isEqualTo(expected);
    }

    @Test
    void should_parse_boolean_expression() {
        var input = "true;";
        var lexer = lexer(input);
        var parser = new Parser(lexer);

        var program = parser.parseProgram();
        checkParseErrors(parser);

        assertThat(program.programStatements().size()).isEqualTo(1);

        var statement = program.programStatements().get(0);
        assertThat(statement).isInstanceOf(ExpressionStatement.class);

        var expressionStatement = (ExpressionStatement) statement;
        testLiteralExpression(expressionStatement.expression(), true);
    }

    private void testInfixExpression(Expression expression, Object left, String operator, Object right) {
        assertThat(expression).isInstanceOf(InfixExpression.class);
        var infixExpression = (InfixExpression) expression;

        testLiteralExpression(infixExpression.left(), left);
        assertThat(infixExpression.operator()).isEqualTo(operator);
        testLiteralExpression(infixExpression.right(), right);
    }

    private void testLiteralExpression(Expression expression, Object object) {
        switch (object) {
            case Integer integer -> testIntegerLiteral(expression, integer);
            case String string -> testIdentifier(expression, string);
            case Boolean bool -> testBoolean(expression, bool);
            default -> fail("type of expression not supported. got=%s".formatted(object.getClass()));
        }
    }

    private void testIntegerLiteral(Expression expression, int value) {
        assertThat(expression).isInstanceOf(IntegerLiteral.class);
        var integerLiteral = (IntegerLiteral) expression;

        assertThat(integerLiteral.value()).isEqualTo(value);
        assertThat(integerLiteral.tokenLiteral()).isEqualTo(String.valueOf(value));
    }

    private void testIdentifier(Expression expression, String value) {
        assertThat(expression).isInstanceOf(Identifier.class);
        var identifier = (Identifier) expression;

        assertThat(identifier.value()).isEqualTo(value);
        assertThat(identifier.tokenLiteral()).isEqualTo(value);
    }

    private void testBoolean(Expression expression, boolean bool) {
        assertThat(expression).isInstanceOf(BooleanExpression.class);
        var booleanExpression = (BooleanExpression) expression;

        assertThat(booleanExpression.value()).isEqualTo(bool);
        assertThat(booleanExpression.tokenLiteral()).isEqualTo(String.valueOf(bool));
    }

    private void checkParseErrors(Parser parser) {
        var errors = parser.errors();
        if (errors.isEmpty()) {
            return;
        }

        var headerErrorLine = "parser has %d errors".formatted(errors.size());
        var details = errors.stream()
                .map("parser error: \"%s\"\n"::formatted)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append);
        fail(headerErrorLine + "\n" + details);
    }
}
