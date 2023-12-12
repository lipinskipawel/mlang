package com.github.lipinskipawel.mlang.ast.expression;

import com.github.lipinskipawel.mlang.ast.Node;
import com.github.lipinskipawel.mlang.token.Token;

import java.util.List;

import static java.util.stream.Collectors.joining;

public final class CallExpression extends Expression {
    private Token token; // The '(' token
    private Expression function; // Identifier or FunctionLiteral
    private List<Expression> arguments;

    public CallExpression(Token token, Expression function) {
        this.token = token;
        this.function = function;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        return function.string() +
                "(" + args() + ")";
    }

    private String args() {
        return arguments.stream()
                .map(Node::string)
                .collect(joining(", "));
    }

    @Override
    public void expressionNode() {

    }

    public Expression function() {
        return function;
    }

    public void arguments(List<Expression> expressions) {
        this.arguments = expressions;
    }

    public List<Expression> arguments() {
        return arguments;
    }
}
