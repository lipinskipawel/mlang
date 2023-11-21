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

    // Delimiters
    COMMA(","),
    SEMICOLON(";"),

    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),

    // Keywords
    FUNCTION("FUNCTION"),
    LET("LET");

    private final String name;
    private static Map<String, TokenType> keywords = Map.of(
            "fn", FUNCTION,
            "let", LET
    );

    TokenType(String name) {
        this.name = name;
    }

    static TokenType fromLiteral(String literal) {
        return keywords.getOrDefault(literal, IDENT);
    }
}
