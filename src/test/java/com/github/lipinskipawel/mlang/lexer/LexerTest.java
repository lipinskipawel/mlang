package com.github.lipinskipawel.mlang.lexer;

import com.github.lipinskipawel.mlang.token.TokenType;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.lipinskipawel.mlang.lexer.Lexer.lexer;
import static com.github.lipinskipawel.mlang.token.TokenType.ASSIGN;
import static com.github.lipinskipawel.mlang.token.TokenType.COMMA;
import static com.github.lipinskipawel.mlang.token.TokenType.EOF;
import static com.github.lipinskipawel.mlang.token.TokenType.FUNCTION;
import static com.github.lipinskipawel.mlang.token.TokenType.IDENT;
import static com.github.lipinskipawel.mlang.token.TokenType.INT;
import static com.github.lipinskipawel.mlang.token.TokenType.LBRACE;
import static com.github.lipinskipawel.mlang.token.TokenType.LET;
import static com.github.lipinskipawel.mlang.token.TokenType.LPAREN;
import static com.github.lipinskipawel.mlang.token.TokenType.PLUS;
import static com.github.lipinskipawel.mlang.token.TokenType.RBRACE;
import static com.github.lipinskipawel.mlang.token.TokenType.RPAREN;
import static com.github.lipinskipawel.mlang.token.TokenType.SEMICOLON;

final class LexerTest implements WithAssertions {

    @Test
    void should_correctly_recognize_tokens_from_simple_symbol_string() {
        var input = "=+(){},;";
        record TokenLiteral(TokenType tokenType, String literal) {
        }
        var expectedTokens = List.of(
                new TokenLiteral(ASSIGN, "="),
                new TokenLiteral(PLUS, "+"),
                new TokenLiteral(LPAREN, "("),
                new TokenLiteral(RPAREN, ")"),
                new TokenLiteral(LBRACE, "{"),
                new TokenLiteral(RBRACE, "}"),
                new TokenLiteral(COMMA, ","),
                new TokenLiteral(SEMICOLON, ";"),
                new TokenLiteral(EOF, "")
        );
        var lexer = lexer(input);

        for (var expectedTokenLiteral : expectedTokens) {
            var token = lexer.nextToken();
            if (token.type() != expectedTokenLiteral.tokenType) {
                fail("token type wrong. expected=[%s], got=[%s]", expectedTokenLiteral.tokenType, token.type());
            }
            if (!token.literal().equals(expectedTokenLiteral.literal)) {
                fail("literal wrong. expected=[%s], got=[%s]", expectedTokenLiteral.literal, token.literal());
            }
        }
    }

    @Test
    void should_correctly_recognize_tokens_from_source_code() {
        var input = """
                let five = 5;
                let ten = 10;
                
                let add = fn(x, y) {
                  x + y;
                };
                
                let result = add(five, ten);
                """;
        record TokenLiteral(TokenType tokenType, String literal) {
        }
        var expectedTokens = List.of(
                new TokenLiteral(LET, "let"),
                new TokenLiteral(IDENT, "five"),
                new TokenLiteral(ASSIGN, "="),
                new TokenLiteral(INT, "5"),
                new TokenLiteral(SEMICOLON, ";"),

                new TokenLiteral(LET, "let"),
                new TokenLiteral(IDENT, "ten"),
                new TokenLiteral(ASSIGN, "="),
                new TokenLiteral(INT, "10"),
                new TokenLiteral(SEMICOLON, ";"),

                new TokenLiteral(LET, "let"),
                new TokenLiteral(IDENT, "add"),
                new TokenLiteral(ASSIGN, "="),
                new TokenLiteral(FUNCTION, "fn"),
                new TokenLiteral(LPAREN, "("),
                new TokenLiteral(IDENT, "x"),
                new TokenLiteral(COMMA, ","),
                new TokenLiteral(IDENT, "y"),
                new TokenLiteral(RPAREN, ")"),
                new TokenLiteral(LBRACE, "{"),
                new TokenLiteral(IDENT, "x"),
                new TokenLiteral(PLUS, "+"),
                new TokenLiteral(IDENT, "y"),
                new TokenLiteral(SEMICOLON, ";"),
                new TokenLiteral(RBRACE, "}"),
                new TokenLiteral(SEMICOLON, ";"),

                new TokenLiteral(LET, "let"),
                new TokenLiteral(IDENT, "result"),
                new TokenLiteral(ASSIGN, "="),
                new TokenLiteral(IDENT, "add"),
                new TokenLiteral(LPAREN, "("),
                new TokenLiteral(IDENT, "five"),
                new TokenLiteral(COMMA, ","),
                new TokenLiteral(IDENT, "ten"),
                new TokenLiteral(RPAREN, ")"),
                new TokenLiteral(SEMICOLON, ";"),

                new TokenLiteral(EOF, "")
        );
        var lexer = lexer(input);

        for (var expectedTokenLiteral : expectedTokens) {
            var token = lexer.nextToken();
            if (token.type() != expectedTokenLiteral.tokenType) {
                fail("token type wrong. expected=[%s], got=[%s]", expectedTokenLiteral.tokenType, token.type());
            }
            if (!token.literal().equals(expectedTokenLiteral.literal)) {
                fail("literal wrong. expected=[%s], got=[%s]", expectedTokenLiteral.literal, token.literal());
            }
        }
    }
}
