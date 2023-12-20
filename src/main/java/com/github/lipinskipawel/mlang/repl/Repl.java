package com.github.lipinskipawel.mlang.repl;

import com.github.lipinskipawel.mlang.evaluator.Evaluator;
import com.github.lipinskipawel.mlang.parser.Parser;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static com.github.lipinskipawel.mlang.lexer.Lexer.lexer;

final class Repl {
    private static final String PROMPT = ">> ";
    private static final String MONKEY_FACE = """
                       __,__
              .--.  .-"     "-.  .--.
             / .. \\/  .-. .-.  \\/ .. \\
            | |  '|  /   Y   \\  |'  | |
            | \\   \\  \\ 0 | 0 /  /   / |
             \\ '- ,\\.-\"\"\"\"\"\"\"-./, -' /
              ''-' /_   ^ ^   _\\ '-''
                  |  \\._   _./  |
                  \\   \\ '~' /   /
                   '._ '-=-' _.'
                      '-----'
            """;

    static void repl(OutputStream outputStream, InputStream inputStream) {
        final var output = new PrintStream(outputStream);
        try (var scanner = new Scanner(inputStream)) {
            while (true) {
                output.print(PROMPT);

                final var line = scanner.nextLine();
                final var lexer = lexer(line);
                final var parser = new Parser(lexer);
                final var program = parser.parseProgram();

                if (!parser.errors().isEmpty()) {
                    printParseError(output, parser);
                    continue;
                }

                final var evaluated = Evaluator.eval(program);
                if (evaluated != null) {
                    output.println(evaluated.inspect());
                }
            }
        }
    }

    static void printParseError(PrintStream output, Parser parser) {
        output.println(MONKEY_FACE);
        output.println("Woops! We ran into some monkey business here!");
        output.println(" parser errors:");
        output.println(parser.errors());
    }
}
