package com.github.lipinskipawel.mlang.parser;

import com.github.lipinskipawel.mlang.ast.statement.LetStatement;
import com.github.lipinskipawel.mlang.ast.statement.ReturnStatement;
import com.github.lipinskipawel.mlang.ast.statement.Statement;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.lipinskipawel.mlang.lexer.Lexer.lexer;

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

    private void checkParseErrors(Parser parser) {
        var errors = parser.errors();
        if (errors.isEmpty()) {
            return;
        }

        var headerErrorLine = "parser has %d errors".formatted(errors.size());
        var details = errors.stream()
                .map("parser error %s\n"::formatted)
                .collect(StringBuffer::new, StringBuffer::append, StringBuffer::append);
        fail(headerErrorLine + "\n" + details);
    }
}
