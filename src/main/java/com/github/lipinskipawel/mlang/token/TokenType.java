package com.github.lipinskipawel.mlang.token;

import java.util.Map;

public enum TokenType {
    ILLEGAL("ILLEGAL"),
    EOF("EOF"),

    // Identifiers + literals
    IDENT("IDENT"), // add, foobar, x, y, ...
    INT("INT"), // 123531

    // Operators
    ASSIGN("="),
    PLUS("+"),
    MINUS("-"),
    BANG("!"),
    ASTERISK("*"),
    SLASH("/"),
    EQ("=="),
    NOT_EQ("!="),

    LT("<"),
    GT(">"),

    // Delimiters
    COMMA(","),
    SEMICOLON(";"),

    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),

    // Keywords
    FUNCTION("FUNCTION"),
    LET("LET"),
    TRUE("TRUE"),
    FALSE("FALSE"),
    IF("IF"),
    ELSE("ELSE"),
    RETURN("RETURN"),

    STRING("STRING");

    private final String name;
    private static Map<String, TokenType> keywords = Map.of(
            "fn", FUNCTION,
            "let", LET,
            "true", TRUE,
            "false", FALSE,
            "if", IF,
            "else", ELSE,
            "return", RETURN
    );

    TokenType(String name) {
        this.name = name;
    }

    static TokenType fromLiteral(String literal) {
        return keywords.getOrDefault(literal, IDENT);
    }
}
