package com.github.lipinskipawel.mlang.compiler;

import com.github.lipinskipawel.mlang.code.Instructions;
import com.github.lipinskipawel.mlang.code.OpCode;
import com.github.lipinskipawel.mlang.evaluator.objects.CompilerFunction;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyString;
import com.github.lipinskipawel.mlang.parser.ast.Node;
import com.github.lipinskipawel.mlang.parser.ast.Program;
import com.github.lipinskipawel.mlang.parser.ast.expression.ArrayLiteral;
import com.github.lipinskipawel.mlang.parser.ast.expression.BooleanExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.FunctionLiteral;
import com.github.lipinskipawel.mlang.parser.ast.expression.HashLiteral;
import com.github.lipinskipawel.mlang.parser.ast.expression.Identifier;
import com.github.lipinskipawel.mlang.parser.ast.expression.IfExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.IndexExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.InfixExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.IntegerLiteral;
import com.github.lipinskipawel.mlang.parser.ast.expression.PrefixExpression;
import com.github.lipinskipawel.mlang.parser.ast.expression.StringLiteral;
import com.github.lipinskipawel.mlang.parser.ast.statement.BlockStatement;
import com.github.lipinskipawel.mlang.parser.ast.statement.ExpressionStatement;
import com.github.lipinskipawel.mlang.parser.ast.statement.LetStatement;
import com.github.lipinskipawel.mlang.parser.ast.statement.ReturnStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.lipinskipawel.mlang.code.Instructions.instructions;
import static com.github.lipinskipawel.mlang.code.Instructions.make;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_ADD;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_ARRAY;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_BANG;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_CONSTANT;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_DIV;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_EQUAL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_FALSE;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_GET_GLOBAL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_GREATER_THAN;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_HASH;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_INDEX;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_JUMP;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_JUMP_NOT_TRUTHY;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_MINUS;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_MUL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_NOT_EQUAL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_NULL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_POP;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_RETURN;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_RETURN_VALUE;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_SET_GLOBAL;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_SUB;
import static com.github.lipinskipawel.mlang.code.OpCode.OP_TRUE;
import static com.github.lipinskipawel.mlang.code.OpCode.opCode;
import static com.github.lipinskipawel.mlang.compiler.SymbolTable.symbolTable;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public final class Compiler {
    private final List<MonkeyObject> constants;
    private final SymbolTable symbolTable;
    final List<CompilationScope> compilationScopes;
    int scopeIndex;

    private Compiler(List<MonkeyObject> constants, SymbolTable symbolTable) {
        this.constants = requireNonNull(constants);
        this.symbolTable = requireNonNull(symbolTable);
        this.compilationScopes = new ArrayList<>();
        this.compilationScopes.add(new CompilationScope());
        this.scopeIndex = 0;
    }

    public static Compiler compiler() {
        return compiler(new ArrayList<>(), symbolTable());
    }

    public static Compiler compiler(List<MonkeyObject> constants, SymbolTable symbolTable) {
        return new Compiler(constants, symbolTable);
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

                if (lastInstructionIs(OP_POP)) {
                    removeLastPop();
                }

                final var jumpPos = emit(OP_JUMP, 3333);

                final var afterConsequencePos = currentInstructions().length();
                changeOperand(jumpNotTruthyPos, afterConsequencePos);

                if (ifExpression.alternative() == null) {
                    emit(OP_NULL);
                } else {
                    error = compile(ifExpression.alternative());
                    if (error.isPresent()) {
                        return error;
                    }

                    if (lastInstructionIs(OP_POP)) {
                        removeLastPop();
                    }
                }

                final var afterAlternativePos = currentInstructions().length();
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
            case HashLiteral hashLiteral -> {
                final var error = hashLiteral.pairs().entrySet()
                        .stream()
                        .sorted(comparing(it -> it.getKey().string()))
                        .map(it -> {
                            final var key = compile(it.getKey());
                            final var value = compile(it.getValue());
                            return key.or(() -> value).or(Optional::empty);
                        })
                        .flatMap(Optional::stream)
                        .findFirst();
                if (error.isPresent()) {
                    return error;
                }

                emit(OP_HASH, hashLiteral.pairs().size() * 2);
            }
            case IndexExpression indexExpression -> {
                var error = compile(indexExpression.left());
                if (error.isPresent()) {
                    return error;
                }

                error = compile(indexExpression.index());
                if (error.isPresent()) {
                    return error;
                }

                emit(OP_INDEX);
            }
            case FunctionLiteral functionLiteral -> {
                enterScope();

                final var error = compile(functionLiteral.body());
                if (error.isPresent()) {
                    return error;
                }

                if (lastInstructionIs(OP_POP)) {
                    replaceLastPopWithReturn();
                }
                if (!lastInstructionIs(OP_RETURN_VALUE)) {
                    emit(OP_RETURN);
                }

                final var instructions = leaveScope();
                final var compilerFunction = new CompilerFunction(instructions);
                emit(OP_CONSTANT, addConstant(compilerFunction));
            }
            case ReturnStatement returnStatement -> {
                final var error = compile(returnStatement.returnValue());
                if (error.isPresent()) {
                    return error;
                }
                emit(OP_RETURN_VALUE);
            }
            default -> throw new IllegalStateException("Unexpected value: " + ast);
        }
        return empty();
    }

    private Instructions currentInstructions() {
        return compilationScopes.get(scopeIndex).instructions();
    }

    private boolean lastInstructionIs(OpCode opCode) {
        if (currentInstructions().length() == 0) {
            return false;
        }
        return compilationScopes.get(scopeIndex).lastInstruction().opCode() == opCode;
    }

    private void replaceLastPopWithReturn() {
        final var lastPosition = compilationScopes.get(scopeIndex).lastInstruction().position();
        currentInstructions().replaceInstructions(lastPosition, make(OP_RETURN_VALUE, new int[0]));

        final var onTop = compilationScopes.get(scopeIndex);
        putCompilationScopeAt(onTop.withLastInstruction(onTop.lastInstruction().withOpCode(OP_RETURN_VALUE)), scopeIndex);
    }

    private void removeLastPop() {
        final var onTop = compilationScopes.get(scopeIndex);

        final var last = onTop.lastInstruction();
        final var previous = onTop.previousInstruction();

        final var newInstructions = instructions(currentInstructions().slice(0, last.position()));

        putCompilationScopeAt(
                onTop.withInstructions(newInstructions).withLastInstruction(previous),
                scopeIndex
        );
    }

    private void changeOperand(int opPosition, int... operand) {
        final var opCode = opCode(currentInstructions().instructionAt(opPosition));
        final var newInstruction = make(opCode, operand);

        currentInstructions().replaceInstructions(opPosition, newInstruction);
    }

    void enterScope() {
        final var scope = new CompilationScope();

        compilationScopes.add(scope);
        scopeIndex++;
    }

    Instructions leaveScope() {
        final var instructions = currentInstructions();

        compilationScopes.removeLast();
        scopeIndex--;

        return instructions;
    }

    // here we can write to file or to collections
    int emit(OpCode op, int... operands) {
        final var instruction = make(op, operands);
        final var position = addInstructions(instruction);

        setLastInstruction(op, position);

        return position;
    }

    private int addInstructions(byte[] instruction) {
        final var newPositionInstruction = currentInstructions().length();
        currentInstructions().append(instructions(instruction));

        final var newCompilationScope = compilationScopes.get(scopeIndex).withInstructions(currentInstructions());
        putCompilationScopeAt(newCompilationScope, scopeIndex);
        return newPositionInstruction;
    }

    private void setLastInstruction(OpCode op, int pos) {
        final var previous = compilationScopes.get(scopeIndex).lastInstruction();
        final var last = new EmittedInstructions(op, pos);

        putCompilationScopeAt(compilationScopes.get(scopeIndex)
                .withPreviousInstruction(previous)
                .withLastInstruction(last), scopeIndex
        );
    }

    private void putCompilationScopeAt(CompilationScope compilationScope, int position) {
        compilationScopes.add(position, compilationScope);
        compilationScopes.remove(++position);
    }

    private int addConstant(MonkeyObject constant) {
        constants.add(constant);
        return constants.size() - 1;
    }

    public Bytecode bytecode() {
        return new Bytecode(currentInstructions(), constants);
    }
}
