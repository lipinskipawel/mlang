package com.github.lipinskipawel.mlang.compiler;

import com.github.lipinskipawel.mlang.code.Instructions;
import com.github.lipinskipawel.mlang.code.OpCode;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.parser.ast.Node;
import com.github.lipinskipawel.mlang.parser.ast.Program;
import com.github.lipinskipawel.mlang.parser.ast.expression.InfixExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.IntegerLiteral;
import com.github.lipinskipawel.mlang.parser.ast.statement.ExpressionStatement;

import java.util.ArrayList;
import java.util.List;

import static com.github.lipinskipawel.mlang.code.Instructions.make;
import static com.github.lipinskipawel.mlang.code.Instructions.noInstructions;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_CONSTANT;

public final class Compiler {
    private final Instructions instructions;
    private final List<MonkeyObject> constants;

    private Compiler(Instructions instructions, List<MonkeyObject> constants) {
        this.instructions = instructions;
        this.constants = constants;
    }

    public static Compiler compiler() {
        return new Compiler(noInstructions(), new ArrayList<>());
    }

    public Compiler compile(Node ast) {
        switch (ast) {
            case Program program -> {
                for (var statement : program.programStatements()) {
                    final var result = compile(statement);
                    if (result != null) {
                        throw new RuntimeException("Compile error");
                    }
                }
            }
            case ExpressionStatement statement -> {
                final var result = compile(statement.expression());
                if (result != null) {
                    throw new RuntimeException("Compile error");
                }
            }
            case InfixExpression infix -> {
                final var left = compile(infix.left());
                if (left != null) {
                    throw new RuntimeException("Compile error");
                }

                final var right = compile(infix.right());
                if (right != null) {
                    throw new RuntimeException("Compile error");
                }
            }
            case IntegerLiteral integer -> {
                final var monkeyInteger = new MonkeyInteger(integer.value());
                emit(OP_CONSTANT, addConstant(monkeyInteger));
            }
            default -> throw new IllegalStateException("Unexpected value: " + ast);
        }
        return null;
    }

    // here we can write to file or to collections
    private int emit(OpCode op, int... operands) {
        final var instruction = make(op, operands);
        final var position = addInstructions(instruction);
        return position;
    }

    private int addInstructions(byte[] instruction) {
        final var newPositionInstruction = instructions.bytes().length;
        instructions.append(Instructions.instructions(instruction));
        return newPositionInstruction;
    }

    private int addConstant(MonkeyObject constant) {
        constants.add(constant);
        return constants.size() - 1;
    }

    public Bytecode bytecode() {
        return new Bytecode(instructions, constants);
    }
}
