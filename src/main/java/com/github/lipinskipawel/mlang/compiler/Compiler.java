package com.github.lipinskipawel.mlang.compiler;

import com.github.lipinskipawel.mlang.code.Instructions;
import com.github.lipinskipawel.mlang.code.OpCode;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.parser.ast.Node;
import com.github.lipinskipawel.mlang.parser.ast.Program;
import com.github.lipinskipawel.mlang.parser.ast.expression.BooleanExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.InfixExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.IntegerLiteral;
import com.github.lipinskipawel.mlang.parser.ast.expression.PrefixExpression;
import com.github.lipinskipawel.mlang.parser.ast.statement.ExpressionStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.lipinskipawel.mlang.code.Instructions.instructions;
import static com.github.lipinskipawel.mlang.code.Instructions.make;
import static com.github.lipinskipawel.mlang.code.Instructions.noInstructions;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_ADD;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_BANG;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_CONSTANT;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_DIV;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_EQUAL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_FALSE;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_GREATER_THAN;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_MINUS;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_MUL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_NOT_EQUAL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_POP;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_SUB;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_TRUE;
import static java.util.Optional.empty;

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

    public Optional<Object> compile(Node ast) {
        switch (ast) {
            case Program program -> {
                for (var statement : program.programStatements()) {
                    final var result = compile(statement);
                    if (result.isPresent()) {
                        return result;
                    }
                }
            }
            case ExpressionStatement statement -> {
                final var result = compile(statement.expression());
                if (result.isPresent()) {
                    return result;
                }
                emit(OP_POP);
            }
            case PrefixExpression prefix -> {
                final var error = compile(prefix.right());
                if (error.isPresent()) {
                    return error;
                }

                switch (prefix.operator()) {
                    case "!" -> emit(OP_BANG);
                    case "-" -> emit(OP_MINUS);
                    default -> throw new IllegalArgumentException("unknown operator [%s]".formatted(prefix.operator()));
                }
            }
            case InfixExpression infix -> {
                if (infix.operator().equals("<")) {
                    final var right = compile(infix.right());
                    if (right.isPresent()) {
                        return right;
                    }

                    final var left = compile(infix.left());
                    if (left.isPresent()) {
                        return left;
                    }
                    emit(OP_GREATER_THAN);
                    return empty();
                }
                final var left = compile(infix.left());
                if (left.isPresent()) {
                    return left;
                }

                final var right = compile(infix.right());
                if (right.isPresent()) {
                    return right;
                }

                switch (infix.operator()) {
                    case "+" -> emit(OP_ADD);
                    case "-" -> emit(OP_SUB);
                    case "*" -> emit(OP_MUL);
                    case "/" -> emit(OP_DIV);
                    case ">" -> emit(OP_GREATER_THAN);
                    case "==" -> emit(OP_EQUAL);
                    case "!=" -> emit(OP_NOT_EQUAL);
                    default -> throw new IllegalArgumentException("unknown operator [%s]".formatted(infix.operator()));
                }
            }
            case IntegerLiteral integer -> {
                final var monkeyInteger = new MonkeyInteger(integer.value());
                emit(OP_CONSTANT, addConstant(monkeyInteger));
            }
            case BooleanExpression booleanExpression -> {
                if (booleanExpression.value()) {
                    emit(OP_TRUE);
                } else {
                    emit(OP_FALSE);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + ast);
        }
        return empty();
    }

    // here we can write to file or to collections
    private int emit(OpCode op, int... operands) {
        final var instruction = make(op, operands);
        final var position = addInstructions(instruction);
        return position;
    }

    private int addInstructions(byte[] instruction) {
        final var newPositionInstruction = instructions.bytes().length;
        instructions.append(instructions(instruction));
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
