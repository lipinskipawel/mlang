package com.github.lipinskipawel.mlang.evaluator.objects;

import com.github.lipinskipawel.mlang.evaluator.Environment;
import com.github.lipinskipawel.mlang.parser.ast.expression.Identifier;
import com.github.lipinskipawel.mlang.parser.ast.statement.BlockStatement;

import java.util.List;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.FUNCTION_OBJ;
import static java.util.stream.Collectors.joining;

public final class MonkeyFunction extends MonkeyObject {
    private final List<Identifier> parameters;
    private final BlockStatement block;
    private final Environment environment;

    public MonkeyFunction(List<Identifier> parameters, BlockStatement block, Environment environment) {
        this.parameters = parameters;
        this.block = block;
        this.environment = environment;
    }

    @Override
    public ObjectType type() {
        return FUNCTION_OBJ;
    }

    @Override
    public String inspect() {
        return "fn" + "(" + params() + ") {\n" +
                block.string() +
                "\n}";
    }

    private String params() {
        return parameters
                .stream()
                .map(Identifier::value)
                .collect(joining(", "));
    }

    public List<Identifier> parameters() {
        return parameters;
    }

    public BlockStatement block() {
        return block;
    }

    public Environment environment() {
        return environment;
    }
}
