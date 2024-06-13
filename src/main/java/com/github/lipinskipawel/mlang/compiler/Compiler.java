package com.github.lipinskipawel.mlang.compiler;

import com.github.lipinskipawel.mlang.code.Instructions;
import com.github.lipinskipawel.mlang.code.OpCode;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyString;
import com.github.lipinskipawel.mlang.parser.ast.Node;
import com.github.lipinskipawel.mlang.parser.ast.Program;
import com.github.lipinskipawel.mlang.parser.ast.expression.ArrayLiteral;
import com.github.lipinskipawel.mlang.parser.ast.expression.BooleanExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.Identifier;
import com.github.lipinskipawel.mlang.parser.ast.expression.IfExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.InfixExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.IntegerLiteral;
import com.github.lipinskipawel.mlang.parser.ast.expression.PrefixExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.StringLiteral;
import com.github.lipinskipawel.mlang.parser.ast.statement.BlockStatement;
import com.github.lipinskipawel.mlang.parser.ast.statement.ExpressionStatement;
import com.github.lipinskipawel.mlang.parser.ast.statement.LetStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.lipinskipawel.mlang.code.Instructions.instructions;
import static com.github.lipinskipawel.mlang.code.Instructions.make;
import static com.github.lipinskipawel.mlang.code.Instructions.noInstructions;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_ADD;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_ARRAY;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_BANG;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_CONSTANT;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_DIV;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_EQUAL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_FALSE;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_GET_GLOBAL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_GREATER_THAN;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_JUMP;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_JUMP_NOT_TRUTHY;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_MINUS;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_MUL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_NOT_EQUAL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_NULL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_POP;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_SET_GLOBAL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_SUB;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_TRUE;
import static com.github.lipinskipawel.mlang.code.OpCode.opCode;
import static com.github.lipinskipawel.mlang.compiler.SymbolTable.symbolTable;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public final class Compiler {
    private final Instructions instructions;
    private final List<MonkeyObject> constants;
    private final SymbolTable symbolTable;
    private EmittedInstructions lastInstruction;
    private EmittedInstructions previousInstruction;

    private Compiler(Instructions instructions, List<MonkeyObject> constants, SymbolTable symbolTable) {
        this.instructions = requireNonNull(instructions);
        this.constants = requireNonNull(constants);
        this.symbolTable = requireNonNull(symbolTable);
    }

    public static Compiler compiler() {
        return compiler(new ArrayList<>(), symbolTable());
    }

    public static Compiler compiler(List<MonkeyObject> constants, SymbolTable symbolTable) {
        return new Compiler(noInstructions(), constants, symbolTable);
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
            case LetStatement letStatement -> {
                final var error = compile(letStatement.value());
                if (error.isPresent()) {
                    return error;
                }

                final var symbol = symbolTable.define(letStatement.name().value());
                emit(OP_SET_GLOBAL, symbol.index());
            }
            case Identifier identifier -> {
                final var resolve = symbolTable.resolve(identifier.value());
                if (resolve.isEmpty()) {
                    return of("undefined variable [%s]".formatted(identifier.value()));
                }
                emit(OP_GET_GLOBAL, resolve.get().index());
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
            case StringLiteral string -> {
                final var monkeyString = new MonkeyString(string.value());
                emit(OP_CONSTANT, addConstant(monkeyString));
            }
            case BooleanExpression booleanExpression -> {
                if (booleanExpression.value()) {
                    emit(OP_TRUE);
                } else {
                    emit(OP_FALSE);
                }
            }
            case IfExpression ifExpression -> {
                var error = compile(ifExpression.condition());
                if (error.isPresent()) {
                    return error;
                }

                final var jumpNotTruthyPos = emit(OP_JUMP_NOT_TRUTHY, 9999);

                error = compile(ifExpression.consequence());
                if (error.isPresent()) {
                    return error;
                }

                if (lastInstructionIsPop()) {
                    removeLastPop();
                }

                final var jumpPos = emit(OP_JUMP, 3333);

                final var afterConsequencePos = instructions.length();
                changeOperand(jumpNotTruthyPos, afterConsequencePos);

                if (ifExpression.alternative() == null) {
                    emit(OP_NULL);
                } else {
                    error = compile(ifExpression.alternative());
                    if (error.isPresent()) {
                        return error;
                    }

                    if (lastInstructionIsPop()) {
                        removeLastPop();
                    }
                }

                final var afterAlternativePos = instructions.length();
                changeOperand(jumpPos, afterAlternativePos);
            }
            case BlockStatement blockStatement -> {
                for (var statement : blockStatement.statements()) {
                    final var error = compile(statement);
                    if (error.isPresent()) {
                        return error;
                    }
                }
            }
            case ArrayLiteral arrayLiteral -> {
                for (var expression : arrayLiteral.elements()) {
                    final var error = compile(expression);
                    if (error.isPresent()) {
                        return error;
                    }
                }
                emit(OP_ARRAY, arrayLiteral.elements().size());
            }
            default -> throw new IllegalStateException("Unexpected value: " + ast);
        }
        return empty();
    }

    private boolean lastInstructionIsPop() {
        return lastInstruction.opCode() == OP_POP;
    }

    private void removeLastPop() {
        instructions.remove(lastInstruction.position());
        lastInstruction = previousInstruction;
    }

    private void changeOperand(int opPosition, int... operand) {
        final var opCode = opCode(instructions.instructionAt(opPosition));
        final var newInstruction = make(opCode, operand);

        instructions.replaceInstructions(opPosition, newInstruction);
    }

    // here we can write to file or to collections
    private int emit(OpCode op, int... operands) {
        final var instruction = make(op, operands);
        final var position = addInstructions(instruction);

        setLastInstruction(op, position);

        return position;
    }

    private int addInstructions(byte[] instruction) {
        final var newPositionInstruction = instructions.bytes().length;
        instructions.append(instructions(instruction));
        return newPositionInstruction;
    }

    private void setLastInstruction(OpCode op, int pos) {
        previousInstruction = lastInstruction;
        lastInstruction = new EmittedInstructions(op, pos);
    }

    private int addConstant(MonkeyObject constant) {
        constants.add(constant);
        return constants.size() - 1;
    }

    public Bytecode bytecode() {
        return new Bytecode(instructions, constants);
    }
}
