package com.github.lipinskipawel.mlang.lexer;

import com.github.lipinskipawel.mlang.token.Token;
import com.github.lipinskipawel.mlang.token.TokenType;

import static com.github.lipinskipawel.mlang.token.TokenType.ASSIGN;
import static com.github.lipinskipawel.mlang.token.TokenType.COMMA;
import static com.github.lipinskipawel.mlang.token.TokenType.EOF;
import static com.github.lipinskipawel.mlang.token.TokenType.ILLEGAL;
import static com.github.lipinskipawel.mlang.token.TokenType.INT;
import static com.github.lipinskipawel.mlang.token.TokenType.LBRACE;
import static com.github.lipinskipawel.mlang.token.TokenType.LPAREN;
import static com.github.lipinskipawel.mlang.token.TokenType.PLUS;
import static com.github.lipinskipawel.mlang.token.TokenType.RBRACE;
import static com.github.lipinskipawel.mlang.token.TokenType.RPAREN;
import static com.github.lipinskipawel.mlang.token.TokenType.SEMICOLON;
import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;
import static java.lang.String.valueOf;

final class Lexer {
    private final String input;
    private int position; // current position in input (points to current char)
    private int readPosition; // current reading position in input (after current char)
    private char character; // current char under examination

    private Lexer(String input) {
        this.input = input;
        position = 0;
        readPosition = 0;
        character = 0;
    }

    public static Lexer lexer(String input) {
        final var lexer = new Lexer(input);
        lexer.readChar();
        return lexer;
    }

    Token nextToken() {
        skipWhitespaces();
        return switch (character) {
            case '=' -> toToken(ASSIGN);
            case '+' -> toToken(PLUS);
            case '(' -> toToken(LPAREN);
            case ')' -> toToken(RPAREN);
            case '{' -> toToken(LBRACE);
            case '}' -> toToken(RBRACE);
            case ',' -> toToken(COMMA);
            case ';' -> toToken(SEMICOLON);
            case 0 -> new Token(EOF, "");
            default -> {
                if (isLetter(character) || character == '_') {
                    yield new Token(readIdentifier());
                } else if (isDigit(character)) {
                    yield new Token(INT, readNumber());
                }
                yield toToken(ILLEGAL);
            }
        };
    }

    private void skipWhitespaces() {
        while (character == ' ' || character == '\t' || character == '\n' || character == '\r') {
            readChar();
        }
    }

    private Token toToken(TokenType tokenType) {
        final var token = new Token(tokenType, valueOf(character));
        readChar();
        return token;
    }

    String readIdentifier() {
        var pos = position;
        while (isLetter(character)) {
            readChar();
        }
        return input.substring(pos, position);
    }

    String readNumber() {
        var pos = position;
        while (isDigit(character)) {
            readChar();
        }
        return input.substring(pos, position);
    }

    void readChar() {
        if (readPosition >= input.length()) {
            character = 0;
        } else {
            character = input.charAt(readPosition);
        }
        position = readPosition;
        readPosition += 1;
    }
}
