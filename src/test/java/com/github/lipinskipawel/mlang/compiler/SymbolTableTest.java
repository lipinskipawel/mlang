package com.github.lipinskipawel.mlang.compiler;

import com.github.lipinskipawel.mlang.compiler.SymbolTable.Symbol;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.lipinskipawel.mlang.compiler.SymbolTable.SymbolScope.GLOBAL_SCOPE;
import static com.github.lipinskipawel.mlang.compiler.SymbolTable.SymbolScope.LOCAL_SCOPE;
import static com.github.lipinskipawel.mlang.compiler.SymbolTable.enclosedSymbolTable;
import static com.github.lipinskipawel.mlang.compiler.SymbolTable.symbolTable;

class SymbolTableTest implements WithAssertions {

    private record SymbolTestCase(SymbolTable table, List<Symbol> expectedSymbols) {
    }

    @Test
    void define_symbol() {
        var global = symbolTable();
        var a = global.define("a");
        var b = global.define("b");
        assertThat(a).isEqualTo(new Symbol("a", GLOBAL_SCOPE, 0));
        assertThat(b).isEqualTo(new Symbol("b", GLOBAL_SCOPE, 1));

        var firstLocal = enclosedSymbolTable(global);
        var c = firstLocal.define("c");
        var d = firstLocal.define("d");
        assertThat(c).isEqualTo(new Symbol("c", LOCAL_SCOPE, 0));
        assertThat(d).isEqualTo(new Symbol("d", LOCAL_SCOPE, 1));

        var secondLocal = enclosedSymbolTable(global);
        var e = secondLocal.define("e");
        var f = secondLocal.define("f");
        assertThat(e).isEqualTo(new Symbol("e", LOCAL_SCOPE, 0));
        assertThat(f).isEqualTo(new Symbol("f", LOCAL_SCOPE, 1));
    }

    @Test
    void resolve_symbol() {
        var symbolTable = symbolTable();
        symbolTable.define("a");
        symbolTable.define("b");

        var expectedList = List.of(
                new Symbol("a", GLOBAL_SCOPE, 0),
                new Symbol("b", GLOBAL_SCOPE, 1)
        );

        for (var expected : expectedList) {
            var actual = symbolTable.resolve(expected.name());
            assertThat(actual).isPresent();
            assertThat(actual.get()).isEqualTo(expected);
        }
    }

    @Test
    void resolve_local() {
        var global = symbolTable();
        global.define("a");
        global.define("b");

        var local = enclosedSymbolTable(global);
        local.define("c");
        local.define("d");

        var expectedList = List.of(
                new Symbol("a", GLOBAL_SCOPE, 0),
                new Symbol("b", GLOBAL_SCOPE, 1),
                new Symbol("c", LOCAL_SCOPE, 0),
                new Symbol("d", LOCAL_SCOPE, 1)
        );

        for (var expected : expectedList) {
            var actual = local.resolve(expected.name());
            assertThat(actual).isPresent();
            assertThat(actual.get()).isEqualTo(expected);
        }
    }

    @Test
    void resolve_nested_local() {
        var global = symbolTable();
        global.define("a");
        global.define("b");

        var firstLocal = enclosedSymbolTable(global);
        firstLocal.define("c");
        firstLocal.define("d");

        var secondLocal = enclosedSymbolTable(firstLocal);
        secondLocal.define("e");
        secondLocal.define("f");

        var tests = List.of(
                new SymbolTestCase(
                        firstLocal,
                        List.of(
                                new Symbol("a", GLOBAL_SCOPE, 0),
                                new Symbol("b", GLOBAL_SCOPE, 1),
                                new Symbol("c", LOCAL_SCOPE, 0),
                                new Symbol("d", LOCAL_SCOPE, 1)
                        )),
                new SymbolTestCase(
                        secondLocal,
                        List.of(
                                new Symbol("a", GLOBAL_SCOPE, 0),
                                new Symbol("b", GLOBAL_SCOPE, 1),
                                new Symbol("e", LOCAL_SCOPE, 0),
                                new Symbol("f", LOCAL_SCOPE, 1)
                        ))
        );

        for (var test : tests) {
            for (var symbol : test.expectedSymbols) {
                var actual = test.table.resolve(symbol.name());
                assertThat(actual).isPresent();
                assertThat(actual.get()).isEqualTo(symbol);
            }
        }
    }
}