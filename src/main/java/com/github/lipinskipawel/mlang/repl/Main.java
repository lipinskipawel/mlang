package com.github.lipinskipawel.mlang.repl;

import java.util.Optional;

import static com.github.lipinskipawel.mlang.repl.Repl.repl;
import static java.lang.System.getProperty;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public final class Main {
    public static void main(String[] args) {
        final var helloMessage = readUsername()
                .map("Hello %s!"::formatted)
                .map(it -> it + " This is the Monkey programming language!")
                .orElse("This is the Monkey programming language!");

        System.out.println(helloMessage);
        repl(System.out, System.in);
    }

    private static Optional<String> readUsername() {
        try {
            return ofNullable(getProperty("user.name"));
        } catch (SecurityException ignored) {
            return empty();
        }
    }
}
