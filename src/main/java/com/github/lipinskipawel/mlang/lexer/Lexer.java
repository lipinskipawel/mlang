package com.github.lipinskipawel.mlang.lexer;

import com.github.lipinskipawel.mlang.lexer.token.Token;
import com.github.lipinskipawel.mlang.lexer.token.TokenType;

import static com.github.lipinskipawel.mlang.lexer.token.TokenType.ASSIGN;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.ASTERISK;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.BANG;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.COLON;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.COMMA;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.EOF;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.EQ;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.GT;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.ILLEGAL;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.INT;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.LBRACE;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.LBRACKET;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.LPAREN;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.LT;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.MINUS;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.NOT_EQ;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.PLUS;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.RBRACE;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.RBRACKET;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.RPAREN;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.SEMICOLON;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.SLASH;
import static com.github.lipinskipawel.mlang.lexer.token.TokenType.STRING;
import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;
import static java.lang.String.valueOf;

public final class Lexer {
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

    public Token nextToken() {
        skipWhitespaces();
        return switch (character) {
            case '=' -> {
                var nextChar = peekChar();
                if (nextChar == '=') {
                    readChar();
                    yield toToken(EQ, "==");
                }
                yield toToken(ASSIGN);
            }
            case '+' -> toToken(PLUS);
            case '-' -> toToken(MINUS);
            case '!' -> {
                var nextChar = peekChar();
                if (nextChar == '=') {
                    readChar();
                    yield toToken(NOT_EQ, "!=");
                }
                yield toToken(BANG);
            }
            case '*' -> toToken(ASTERISK);
            case '/' -> toToken(SLASH);
            case '<' -> toToken(LT);
            case '>' -> toToken(GT);
            case '(' -> toToken(LPAREN);
            case ')' -> toToken(RPAREN);
            case '[' -> toToken(LBRACKET);
            case ']' -> toToken(RBRACKET);
            case '"' -> {
                final var literal = readString();
                yield new Token(STRING, literal);
            }
            case '{' -> toToken(LBRACE);
            case '}' -> toToken(RBRACE);
            case ',' -> toToken(COMMA);
            case ';' -> toToken(SEMICOLON);
            case ':' -> toToken(COLON);
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

    private Token toToken(TokenType tokenType, String literal) {
        final var token = new Token(tokenType, literal);
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

    String readString() {
        var pos = position + 1;
        do {
            readChar();
        } while (character != '"' && character != 0);
        final var result = input.substring(pos, position);
        readChar();
        return result;
    }

    char peekChar() {
        if (readPosition >= input.length()) {
            return 0;
        }
        return input.charAt(readPosition);
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
