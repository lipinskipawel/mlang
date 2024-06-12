package com.github.lipinskipawel.mlang.compiler;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.lipinskipawel.mlang.compiler.SymbolTable.SymbolScope.GLOBAL_SCOPE;
import static com.github.lipinskipawel.mlang.compiler.SymbolTable.symbolTable;

class SymbolTableTest implements WithAssertions {

    @Test
    void define_symbol() {
        var symbolTable = symbolTable();

        var a = symbolTable.define("a");
        var b = symbolTable.define("b");

        assertThat(a).isEqualTo(new SymbolTable.Symbol("a", GLOBAL_SCOPE, 0));
        assertThat(b).isEqualTo(new SymbolTable.Symbol("b", GLOBAL_SCOPE, 1));
    }

    @Test
    void resolve_symbol() {
        var symbolTable = symbolTable();
        symbolTable.define("a");
        symbolTable.define("b");

        var expectedList = List.of(
                new SymbolTable.Symbol("a", GLOBAL_SCOPE, 0),
                new SymbolTable.Symbol("b", GLOBAL_SCOPE, 1)
        );

        for (var i = 0; i < expectedList.size(); i++) {
            var expected = expectedList.get(i);
            var actual = symbolTable.resolve(expected.name());
            assertThat(actual).isPresent();
            assertThat(actual.get()).isEqualTo(expected);
        }
    }
}