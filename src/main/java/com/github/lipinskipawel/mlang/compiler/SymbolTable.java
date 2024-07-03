package com.github.lipinskipawel.mlang.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.github.lipinskipawel.mlang.compiler.SymbolTable.SymbolScope.BUILTIN_SCOPE;
import static com.github.lipinskipawel.mlang.compiler.SymbolTable.SymbolScope.FREE_SCOPE;
import static com.github.lipinskipawel.mlang.compiler.SymbolTable.SymbolScope.FUNCTION_SCOPE;
import static com.github.lipinskipawel.mlang.compiler.SymbolTable.SymbolScope.GLOBAL_SCOPE;
import static com.github.lipinskipawel.mlang.compiler.SymbolTable.SymbolScope.LOCAL_SCOPE;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public class SymbolTable {
    public enum SymbolScope {
        GLOBAL_SCOPE,
        LOCAL_SCOPE,
        BUILTIN_SCOPE,
        FREE_SCOPE,
        FUNCTION_SCOPE;
    }

    public record Symbol(String name, SymbolScope scope, int index) {
    }

    private final Map<String, Symbol> store;
    private int numDefinitions;
    SymbolTable outer;
    List<Symbol> freeSymbols;

    private SymbolTable(Map<String, Symbol> store, int numDefinitions, List<Symbol> freeSymbols) {
        this.store = requireNonNull(store);
        this.numDefinitions = numDefinitions;
        this.freeSymbols = freeSymbols;
    }

    public static SymbolTable symbolTable() {
        return new SymbolTable(new HashMap<>(), 0, new ArrayList<>());
    }

    public static SymbolTable enclosedSymbolTable(SymbolTable symbolTable) {
        final var table = symbolTable();
        table.outer = symbolTable;
        return table;
    }

    public Symbol defineFree(Symbol original) {
        freeSymbols.add(original);

        final var symbol = new Symbol(original.name, FREE_SCOPE, freeSymbols.size() - 1);

        store.put(original.name, symbol);
        return symbol;
    }

    public Symbol defineFunctionName(String name) {
        final var symbol = new Symbol(name, FUNCTION_SCOPE, 0);
        store.put(name, symbol);
        return symbol;
    }

    public Symbol defineBuiltin(int index, String name) {
        final var symbol = new Symbol(name, BUILTIN_SCOPE, index);
        store.put(name, symbol);
        return symbol;
    }

    public Symbol define(String name) {
        final var scope = outer == null ? GLOBAL_SCOPE : LOCAL_SCOPE;
        final var symbol = new Symbol(name, scope, numDefinitions);
        store.put(name, symbol);
        numDefinitions++;
        return symbol;
    }

    public Optional<Symbol> resolve(String name) {
        final var symbol = store.get(name);
        if (symbol == null && outer != null) {
            final var maybeOuterSymbol = outer.resolve(name);
            if (maybeOuterSymbol.isEmpty()) {
                return empty();
            }

            final var outerSymbol = maybeOuterSymbol.get();
            if (outerSymbol.scope == GLOBAL_SCOPE || outerSymbol.scope == BUILTIN_SCOPE) {
                return maybeOuterSymbol;
            }

            final var free = defineFree(outerSymbol);
            return of(free);
        }
        return ofNullable(symbol);
    }

    public int numDefinitions() {
        return numDefinitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolTable table = (SymbolTable) o;
        return numDefinitions == table.numDefinitions && Objects.equals(store, table.store) && Objects.equals(outer, table.outer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(store, numDefinitions, outer);
    }
}
