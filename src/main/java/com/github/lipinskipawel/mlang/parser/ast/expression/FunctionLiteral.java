package com.github.lipinskipawel.mlang.parser.ast.expression;

import com.github.lipinskipawel.mlang.lexer.token.Token;
import com.github.lipinskipawel.mlang.parser.ast.statement.BlockStatement;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.joining;

public final class FunctionLiteral extends Expression {
    private Token token; // The 'fn' token
    private List<Identifier> parameters;
    private BlockStatement body;
    private Optional<String> name;

    public FunctionLiteral(Token token) {
        this.token = token;
        this.name = empty();
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        return tokenLiteral() +
                nameOfFunction() +
                "(" + params() + ")" +
                body.string();
    }

    private String nameOfFunction() {
        return name
                .map("<%s>"::formatted)
                .orElse("");
    }

    private String params() {
        return parameters.stream()
                .map(Identifier::string)
                .collect(joining(", "));
    }

    @Override
    public void expressionNode() {

    }

    public void name(String name) {
        requireNonNull(name);
        this.name = Optional.of(name);
    }

    public Optional<String> name() {
        return name;
    }

    public void parameters(List<Identifier> identifiers) {
        this.parameters = identifiers;
    }

    public List<Identifier> parameters() {
        return parameters;
    }

    public void body(BlockStatement blockStatement) {
        this.body = blockStatement;
    }

    public BlockStatement body() {
        return body;
    }
}
