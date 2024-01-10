package com.github.lipinskipawel.mlang.parser.ast.expression;

import com.github.lipinskipawel.mlang.parser.ast.Node;
import com.github.lipinskipawel.mlang.lexer.token.Token;

import java.util.List;

import static java.util.stream.Collectors.joining;

public final class ArrayLiteral extends Expression {
    private final Token token; // the '[' token
    private List<Expression> elements;

    public ArrayLiteral(Token token) {
        this.token = token;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        return "[" + elements.stream()
                .map(Node::string)
                .collect(joining(", "))
                + "]";
    }

    @Override
    public void expressionNode() {

    }

    public void elements(List<Expression> elements) {
        this.elements = elements;
    }

    public List<Expression> elements() {
        return elements;
    }
}
