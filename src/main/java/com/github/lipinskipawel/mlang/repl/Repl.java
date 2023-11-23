package com.github.lipinskipawel.mlang.repl;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static com.github.lipinskipawel.mlang.lexer.Lexer.lexer;
import static com.github.lipinskipawel.mlang.token.TokenType.EOF;

final class Repl {
    private static final String PROMPT = ">> ";

    static void repl(OutputStream outputStream, InputStream inputStream) {
        final var output = new PrintStream(outputStream);
        try (var scanner = new Scanner(inputStream)) {
            while (true) {
                output.print(PROMPT);

                final var line = scanner.nextLine();
                final var lexer = lexer(line);

                var token = lexer.nextToken();
                while (token.type() != EOF) {
                    output.println(token);
                    token = lexer.nextToken();
                }
            }
        }
    }
}
