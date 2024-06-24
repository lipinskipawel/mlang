package com.github.lipinskipawel.mlang.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.github.lipinskipawel.mlang.compiler.SymbolTable.SymbolScope.GLOBAL_SCOPE;
import static com.github.lipinskipawel.mlang.compiler.SymbolTable.SymbolScope.LOCAL_SCOPE;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class SymbolTable {
    public enum SymbolScope {
        GLOBAL_SCOPE("GLOBAL"),
        LOCAL_SCOPE("LOCAL");
        private final String scope;

        SymbolScope(String scope) {
            this.scope = scope;
        }
    }

    public record Symbol(String name, SymbolScope scope, int index) {
    }

    private final Map<String, Symbol> store;
    private int numDefinitions;
    SymbolTable outer;

    private SymbolTable(Map<String, Symbol> store, int numDefinitions) {
        this.store = requireNonNull(store);
        this.numDefinitions = numDefinitions;
    }

    public static SymbolTable symbolTable() {
        return new SymbolTable(new HashMap<>(), 0);
    }

    public static SymbolTable enclosedSymbolTable(SymbolTable symbolTable) {
        final var table = symbolTable();
        table.outer = symbolTable;
        return table;
    }

    public Symbol define(String identifier) {
        final var scope = outer == null ? GLOBAL_SCOPE : LOCAL_SCOPE;
        final var symbol = new Symbol(identifier, scope, numDefinitions);
        store.put(identifier, symbol);
        numDefinitions++;
        return symbol;
    }

    public Optional<Symbol> resolve(String identifier) {
        final var symbol = store.get(identifier);
        if (symbol == null && outer != null) {
            return outer.resolve(identifier);
        }
        return ofNullable(symbol);
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
