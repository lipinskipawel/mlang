package com.github.lipinskipawel.mlang.parser.ast.expression;

import com.github.lipinskipawel.mlang.parser.ast.statement.BlockStatement;
import com.github.lipinskipawel.mlang.lexer.token.Token;

import java.util.List;

import static java.util.stream.Collectors.joining;

public final class FunctionLiteral extends Expression {
    private Token token; // The 'fn' token
    private List<Identifier> parameters;
    private BlockStatement body;

    public FunctionLiteral(Token token) {
        this.token = token;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        return tokenLiteral() +
                "(" + params() + ")" +
                body.string();
    }

    private String params() {
        return parameters.stream()
                .map(Identifier::string)
                .collect(joining(", "));
    }

    @Override
    public void expressionNode() {

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
