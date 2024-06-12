package com.github.lipinskipawel.mlang.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.lipinskipawel.mlang.compiler.SymbolTable.SymbolScope.GLOBAL_SCOPE;
import static java.util.Objects.requireNonNull;

public class SymbolTable {
    public enum SymbolScope {
        GLOBAL_SCOPE("GLOBAL");
        private final String scope;

        SymbolScope(String scope) {
            this.scope = scope;
        }
    }

    public record Symbol(String name, SymbolScope scope, int index) {
    }

    private final Map<String, Symbol> store;
    private int numDefinitions;

    private SymbolTable(Map<String, Symbol> store, int numDefinitions) {
        this.store = requireNonNull(store);
        this.numDefinitions = numDefinitions;
    }

    public static SymbolTable symbolTable() {
        return new SymbolTable(new HashMap<>(), 0);
    }

    public Symbol define(String identifier) {
        final var symbol = new Symbol(identifier, GLOBAL_SCOPE, numDefinitions);
        store.put(identifier, symbol);
        numDefinitions++;
        return symbol;
    }

    public Optional<Symbol> resolve(String identifier) {
        return Optional.of(store.get(identifier));
    }
}
