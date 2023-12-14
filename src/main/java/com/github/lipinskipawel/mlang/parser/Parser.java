package com.github.lipinskipawel.mlang.parser;

import com.github.lipinskipawel.mlang.ast.Program;
import com.github.lipinskipawel.mlang.ast.expression.BooleanExpression;
import com.github.lipinskipawel.mlang.ast.expression.CallExpression;
import com.github.lipinskipawel.mlang.ast.expression.Expression;
import com.github.lipinskipawel.mlang.ast.expression.FunctionLiteral;
import com.github.lipinskipawel.mlang.ast.expression.Identifier;
import com.github.lipinskipawel.mlang.ast.expression.IfExpression;
import com.github.lipinskipawel.mlang.ast.expression.InfixExpression;
import com.github.lipinskipawel.mlang.ast.expression.IntegerLiteral;
import com.github.lipinskipawel.mlang.ast.expression.PrefixExpression;
import com.github.lipinskipawel.mlang.ast.statement.BlockStatement;
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

import static com.github.lipinskipawel.mlang.parser.Precedence.CALL;
import static com.github.lipinskipawel.mlang.parser.Precedence.EQUALS;
import static com.github.lipinskipawel.mlang.parser.Precedence.LESSGREATER;
import static com.github.lipinskipawel.mlang.parser.Precedence.LOWEST;
import static com.github.lipinskipawel.mlang.parser.Precedence.PREFIX;
import static com.github.lipinskipawel.mlang.parser.Precedence.PRODUCT;
import static com.github.lipinskipawel.mlang.parser.Precedence.SUM;
import static com.github.lipinskipawel.mlang.token.TokenType.ASSIGN;
import static com.github.lipinskipawel.mlang.token.TokenType.ASTERISK;
import static com.github.lipinskipawel.mlang.token.TokenType.BANG;
import static com.github.lipinskipawel.mlang.token.TokenType.COMMA;
import static com.github.lipinskipawel.mlang.token.TokenType.ELSE;
import static com.github.lipinskipawel.mlang.token.TokenType.EOF;
import static com.github.lipinskipawel.mlang.token.TokenType.EQ;
import static com.github.lipinskipawel.mlang.token.TokenType.FALSE;
import static com.github.lipinskipawel.mlang.token.TokenType.FUNCTION;
import static com.github.lipinskipawel.mlang.token.TokenType.GT;
import static com.github.lipinskipawel.mlang.token.TokenType.IDENT;
import static com.github.lipinskipawel.mlang.token.TokenType.IF;
import static com.github.lipinskipawel.mlang.token.TokenType.INT;
import static com.github.lipinskipawel.mlang.token.TokenType.LBRACE;
import static com.github.lipinskipawel.mlang.token.TokenType.LPAREN;
import static com.github.lipinskipawel.mlang.token.TokenType.LT;
import static com.github.lipinskipawel.mlang.token.TokenType.MINUS;
import static com.github.lipinskipawel.mlang.token.TokenType.NOT_EQ;
import static com.github.lipinskipawel.mlang.token.TokenType.PLUS;
import static com.github.lipinskipawel.mlang.token.TokenType.RBRACE;
import static com.github.lipinskipawel.mlang.token.TokenType.RPAREN;
import static com.github.lipinskipawel.mlang.token.TokenType.SEMICOLON;
import static com.github.lipinskipawel.mlang.token.TokenType.SLASH;
import static com.github.lipinskipawel.mlang.token.TokenType.TRUE;
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
            ASTERISK, PRODUCT,
            LPAREN, CALL
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
        registerPrefix(TRUE, this::parseBoolean);
        registerPrefix(FALSE, this::parseBoolean);
        registerPrefix(LPAREN, this::parseGroupedExpression);
        registerPrefix(IF, this::parseIfExpression);
        registerPrefix(FUNCTION, this::parseFunctionLiteral);

        registerInfix(PLUS, this::parseInfixExpression);
        registerInfix(MINUS, this::parseInfixExpression);
        registerInfix(SLASH, this::parseInfixExpression);
        registerInfix(ASTERISK, this::parseInfixExpression);
        registerInfix(EQ, this::parseInfixExpression);
        registerInfix(NOT_EQ, this::parseInfixExpression);
        registerInfix(LT, this::parseInfixExpression);
        registerInfix(GT, this::parseInfixExpression);
        registerInfix(LPAREN, this::parseCallExpression);
    }

    private void nextToken() {
        this.currentToken = this.peekToken;
        this.peekToken = lexer.nextToken();
    }

    public Program parseProgram() {
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
        nextToken();

        final var value = parseExpression(LOWEST);
        letStatement.value(value);

        if (peekTokenIs(SEMICOLON)) {
            nextToken();
        }

        return letStatement;
    }

    private ReturnStatement parseReturnStatement() {
        var returnStatement = new ReturnStatement(currentToken);

        nextToken();

        final var value = parseExpression(LOWEST);
        returnStatement.returnValue(value);

        if (peekTokenIs(SEMICOLON)) {
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

    private Expression parseBoolean() {
        return new BooleanExpression(currentToken, curTokenIs(TRUE));
    }

    private Expression parseGroupedExpression() {
        nextToken();

        var expression = parseExpression(LOWEST);
        if (!expectPeek(RPAREN)) {
            return null;
        }

        return expression;
    }

    private Expression parseIfExpression() {
        final var ifExpression = new IfExpression(currentToken);

        if (!expectPeek(LPAREN)) {
            return null;
        }

        nextToken();
        final var condition = parseExpression(LOWEST);
        ifExpression.condition(condition);

        if (!expectPeek(RPAREN)) {
            return null;
        }

        if (!expectPeek(LBRACE)) {
            return null;
        }

        final var consequence = parseBlockStatement();
        ifExpression.consequence(consequence);

        if (peekTokenIs(ELSE)) {
            nextToken();

            if (!expectPeek(LBRACE)) {
                return null;
            }

            final var alternative = parseBlockStatement();
            ifExpression.alternative(alternative);
        }

        return ifExpression;
    }

    private BlockStatement parseBlockStatement() {
        final var blockStatement = new BlockStatement(currentToken);
        final var statements = new ArrayList<Statement>();
        nextToken();

        while (!curTokenIs(RBRACE) && !curTokenIs(EOF)) {
            final var statement = parseStatement();
            if (statement != null) {
                statements.add(statement);
            }
            nextToken();
        }
        blockStatement.statements(statements);
        return blockStatement;
    }

    private Expression parseFunctionLiteral() {
        final var functionLiteral = new FunctionLiteral(currentToken);

        if (!expectPeek(LPAREN)) {
            return null;
        }

        final var parameters = parseFunctionParameters();
        functionLiteral.parameters(parameters);

        if (!expectPeek(LBRACE)) {
            return null;
        }

        final var body = parseBlockStatement();
        functionLiteral.body(body);

        return functionLiteral;
    }

    private List<Identifier> parseFunctionParameters() {
        final var identifiers = new ArrayList<Identifier>();

        if (peekTokenIs(RPAREN)) {
            nextToken();
            return identifiers;
        }

        nextToken();
        final var identifier = new Identifier(currentToken, currentToken.literal());
        identifiers.add(identifier);

        while (peekTokenIs(COMMA)) {
            nextToken();
            nextToken();
            final var anotherIdentifier = new Identifier(currentToken, currentToken.literal());
            identifiers.add(anotherIdentifier);
        }

        if (!expectPeek(RPAREN)) {
            return null;
        }

        return identifiers;
    }

    private Expression parseInfixExpression(Expression left) {
        final var infixExpression = new InfixExpression(currentToken, left, currentToken.literal());

        var precedence = curPrecedence();
        nextToken();
        infixExpression.right(parseExpression(precedence));

        return infixExpression;
    }

    private Expression parseCallExpression(Expression function) {
        final var callExpression = new CallExpression(currentToken, function);

        final var arguments = parseCallArguments();
        callExpression.arguments(arguments);

        return callExpression;
    }

    private List<Expression> parseCallArguments() {
        final var arguments = new ArrayList<Expression>();

        if (peekTokenIs(RPAREN)) {
            nextToken();
            return arguments;
        }

        nextToken();
        arguments.add(parseExpression(LOWEST));

        while (peekTokenIs(COMMA)) {
            nextToken();
            nextToken();
            arguments.add(parseExpression(LOWEST));
        }

        if (!expectPeek(RPAREN)) {
            return null;
        }

        return arguments;
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
