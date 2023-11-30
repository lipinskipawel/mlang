package com.github.lipinskipawel.mlang.parser;

import com.github.lipinskipawel.mlang.ast.Program;
import com.github.lipinskipawel.mlang.ast.expression.Expression;
import com.github.lipinskipawel.mlang.ast.expression.Identifier;
import com.github.lipinskipawel.mlang.ast.expression.InfixExpression;
import com.github.lipinskipawel.mlang.ast.expression.IntegerLiteral;
import com.github.lipinskipawel.mlang.ast.expression.PrefixExpression;
import com.github.lipinskipawel.mlang.ast.statement.ExpressionStatement;
import com.github.lipinskipawel.mlang.ast.statement.LetStatement;
import com.github.lipinskipawel.mlang.ast.statement.ReturnStatement;
import com.github.lipinskipawel.mlang.ast.statement.Statement;
import com.github.lipinskipawel.mlang.lexer.Lexer;
import com.github.lipinskipawel.mlang.token.Token;
import com.github.lipinskipawel.mlang.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.lipinskipawel.mlang.parser.Precedence.EQUALS;
import static com.github.lipinskipawel.mlang.parser.Precedence.LESSGREATER;
import static com.github.lipinskipawel.mlang.parser.Precedence.LOWEST;
import static com.github.lipinskipawel.mlang.parser.Precedence.PREFIX;
import static com.github.lipinskipawel.mlang.parser.Precedence.PRODUCT;
import static com.github.lipinskipawel.mlang.parser.Precedence.SUM;
import static com.github.lipinskipawel.mlang.token.TokenType.ASSIGN;
import static com.github.lipinskipawel.mlang.token.TokenType.ASTERISK;
import static com.github.lipinskipawel.mlang.token.TokenType.BANG;
import static com.github.lipinskipawel.mlang.token.TokenType.EOF;
import static com.github.lipinskipawel.mlang.token.TokenType.EQ;
import static com.github.lipinskipawel.mlang.token.TokenType.GT;
import static com.github.lipinskipawel.mlang.token.TokenType.IDENT;
import static com.github.lipinskipawel.mlang.token.TokenType.INT;
import static com.github.lipinskipawel.mlang.token.TokenType.LT;
import static com.github.lipinskipawel.mlang.token.TokenType.MINUS;
import static com.github.lipinskipawel.mlang.token.TokenType.NOT_EQ;
import static com.github.lipinskipawel.mlang.token.TokenType.PLUS;
import static com.github.lipinskipawel.mlang.token.TokenType.SEMICOLON;
import static com.github.lipinskipawel.mlang.token.TokenType.SLASH;
import static java.lang.Integer.parseInt;

public final class Parser {
    private final Lexer lexer;
    private final List<String> errors;
    private Token currentToken;
    private Token peekToken;

    private final Map<TokenType, Supplier<Expression>> prefixParseFns;
    private final Map<TokenType, Function<Expression, Expression>> infixParseFns;
    private final Map<TokenType, Precedence> precedences = Map.of(
            EQ, EQUALS,
            NOT_EQ, EQUALS,
            LT, LESSGREATER,
            GT, LESSGREATER,
            PLUS, SUM,
            MINUS, SUM,
            SLASH, PRODUCT,
            ASTERISK, PRODUCT
    );

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        // Read two tokens, so currentToken and peekToken are both set
        nextToken();
        nextToken();
        this.errors = new ArrayList<>();
        this.prefixParseFns = new HashMap<>();
        this.infixParseFns = new HashMap<>();
        registerPrefix(IDENT, this::parseIdentifier);
        registerPrefix(INT, this::parseIntegerLiteral);
        registerPrefix(BANG, this::parsePrefixExpression);
        registerPrefix(MINUS, this::parsePrefixExpression);
        registerInfix(PLUS, this::parseInfixExpression);
        registerInfix(MINUS, this::parseInfixExpression);
        registerInfix(SLASH, this::parseInfixExpression);
        registerInfix(ASTERISK, this::parseInfixExpression);
        registerInfix(EQ, this::parseInfixExpression);
        registerInfix(NOT_EQ, this::parseInfixExpression);
        registerInfix(LT, this::parseInfixExpression);
        registerInfix(GT, this::parseInfixExpression);
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
            default -> parseExpressionStatement();
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

    private ExpressionStatement parseExpressionStatement() {
        final var expressionStatement = new ExpressionStatement(currentToken);

        expressionStatement.expression(parseExpression(LOWEST));

        if (peekTokenIs(SEMICOLON)) {
            nextToken();
        }

        return expressionStatement;
    }

    private Expression parseExpression(Precedence precedence) {
        final var prefix = prefixParseFns.get(currentToken.type());
        if (prefix == null) {
            noPrefixParseFnError(currentToken.type());
            return null;
        }

        var leftExpression = prefix.get();

        while (!peekTokenIs(SEMICOLON) && precedence.hasLowerPrecedence(peekPrecedence())) {
            final var infix = infixParseFns.get(peekToken.type());
            if (infix == null) {
                return leftExpression;
            }
            nextToken();
            leftExpression = infix.apply(leftExpression);
        }

        return leftExpression;
    }

    private void noPrefixParseFnError(TokenType type) {
        errors.add("no prefix parse function for %s found".formatted(type));
    }

    private Expression parseIdentifier() {
        return new Identifier(currentToken, currentToken.literal());
    }

    private Expression parseIntegerLiteral() {
        final var integerLiteral = new IntegerLiteral(currentToken);
        try {
            integerLiteral.value(parseInt(currentToken.literal()));
            return integerLiteral;
        } catch (Exception e) {
            final var msg = "could not parse %s as integer".formatted(currentToken.literal());
            errors.add(msg);
            return null;
        }
    }

    private Expression parsePrefixExpression() {
        final var prefixExpression = new PrefixExpression(currentToken, currentToken.literal());

        nextToken();
        prefixExpression.right(parseExpression(PREFIX));

        return prefixExpression;
    }

    private Expression parseInfixExpression(Expression left) {
        final var infixExpression = new InfixExpression(currentToken, left, currentToken.literal());

        var precedence = curPrecedence();
        nextToken();
        infixExpression.right(parseExpression(precedence));

        return infixExpression;
    }

    private void registerPrefix(TokenType type, Supplier<Expression> prefixParseFn) {
        prefixParseFns.put(type, prefixParseFn);
    }

    private void registerInfix(TokenType type, Function<Expression, Expression> infixParseFn) {
        infixParseFns.put(type, infixParseFn);
    }

    private Precedence peekPrecedence() {
        return precedences.getOrDefault(peekToken.type(), LOWEST);
    }

    private Precedence curPrecedence() {
        return precedences.getOrDefault(currentToken.type(), LOWEST);
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
