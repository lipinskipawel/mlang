package com.github.lipinskipawel.mlang.parser;

import com.github.lipinskipawel.mlang.ast.Program;
import com.github.lipinskipawel.mlang.ast.expression.Identifier;
import com.github.lipinskipawel.mlang.ast.statement.LetStatement;
import com.github.lipinskipawel.mlang.ast.statement.ReturnStatement;
import com.github.lipinskipawel.mlang.ast.statement.Statement;
import com.github.lipinskipawel.mlang.lexer.Lexer;
import com.github.lipinskipawel.mlang.token.Token;
import com.github.lipinskipawel.mlang.token.TokenType;

import java.util.ArrayList;
import java.util.List;

import static com.github.lipinskipawel.mlang.token.TokenType.ASSIGN;
import static com.github.lipinskipawel.mlang.token.TokenType.EOF;
import static com.github.lipinskipawel.mlang.token.TokenType.IDENT;
import static com.github.lipinskipawel.mlang.token.TokenType.SEMICOLON;

public final class Parser {
    private Lexer lexer;
    private Token currentToken;
    private Token peekToken;
    private List<String> errors;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        // Read two tokens, so currentToken and peekToken are both set
        nextToken();
        nextToken();
        this.errors = new ArrayList<>();
    }

    private void nextToken() {
        this.currentToken = this.peekToken;
        this.peekToken = lexer.nextToken();
    }

    Program parseProgram() {
        final var program = new Program();

        while (!curTokenIs(EOF)) {
            final var statement = parseStatement();
            if (statement != null) {
                program.add(statement);
            }
            nextToken();
        }
        return program;
    }

    private Statement parseStatement() {
        return switch (currentToken.type()) {
            case LET -> parseLetStatement();
            case RETURN -> parseReturnStatement();
            default -> null;
        };
    }

    private LetStatement parseLetStatement() {
        final var letStatement = new LetStatement(currentToken);

        if (!expectPeek(IDENT)) {
            return null;
        }

        letStatement.name = new Identifier(currentToken, currentToken.literal());

        if (!expectPeek(ASSIGN)) {
            return null;
        }

        while (!curTokenIs(SEMICOLON)) {
            nextToken();
        }

        return letStatement;
    }

    private ReturnStatement parseReturnStatement() {
        var returnStatement = new ReturnStatement(currentToken);

        nextToken();
        while (!curTokenIs(SEMICOLON)) {
            nextToken();
        }

        return returnStatement;
    }

    private boolean curTokenIs(TokenType tokenType) {
        return currentToken.type() == tokenType;
    }

    private boolean expectPeek(TokenType tokenType) {
        if (peekTokenIs(tokenType)) {
            nextToken();
            return true;
        }
        peekError(tokenType);
        return false;
    }

    private boolean peekTokenIs(TokenType tokenType) {
        return peekToken.type() == tokenType;
    }

    public List<String> errors() {
        return List.copyOf(errors);
    }

    private void peekError(TokenType tokenType) {
        var message = "expected next token to be %s, got %s instead".formatted(tokenType, peekToken);
        errors.add(message);
    }
}
