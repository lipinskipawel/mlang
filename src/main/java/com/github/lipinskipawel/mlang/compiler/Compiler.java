package com.github.lipinskipawel.mlang.compiler;

import com.github.lipinskipawel.mlang.code.Instructions;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.parser.ast.Node;

import static com.github.lipinskipawel.mlang.code.Instructions.noInstructions;

public final class Compiler {
    private final Instructions instructions;
    private final MonkeyObject[] constants;

    private Compiler(Instructions instructions, MonkeyObject[] constants) {
        this.instructions = instructions;
        this.constants = constants;
    }

    public static Compiler compiler() {
        return new Compiler(noInstructions(), new MonkeyObject[16]);
    }

    public Compiler compile(Node ast) {
        return null;
    }

    public Bytecode bytecode() {
        return new Bytecode(instructions, constants);
    }
}
