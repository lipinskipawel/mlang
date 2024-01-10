package com.github.lipinskipawel.mlang.parser.ast.expression;

import com.github.lipinskipawel.mlang.lexer.token.Token;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public final class HashLiteral extends Expression {
    private final Token token; // the '{' token
    private final Map<Expression, Expression> pairs;

    public HashLiteral(Token token) {
        this.token = token;
        this.pairs = new HashMap<>();
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String string() {
        return "{"
                + pairs.entrySet()
                .stream()
                .map(it -> it.getKey().string() + ":" + it.getValue().string())
                .collect(joining(", "))
                + "}";
    }

    @Override
    public void expressionNode() {

    }

    public void addPair(Expression key, Expression value) {
        pairs.put(key, value);
    }

    public Map<Expression, Expression> pairs() {
        return pairs;
    }
}
