package com.github.lipinskipawel.mlang.repl;

import com.github.lipinskipawel.mlang.parser.Parser;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static com.github.lipinskipawel.mlang.compiler.Compiler.compiler;
import static com.github.lipinskipawel.mlang.lexer.Lexer.lexer;
import static com.github.lipinskipawel.mlang.vm.VirtualMachine.virtualMachine;

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

                final var compiler = compiler();
                var error = compiler.compile(program);
                if (error.isPresent()) {
                    output.printf("Compilation failed [%s]%n", error.get());
                    continue;
                }

                final var vm = virtualMachine(compiler.bytecode());
                error = vm.run();
                if (error.isPresent()) {
                    output.printf("Executing bytecode failed [%s]%n", error.get());
                    continue;
                }

                final var monkeyObject = vm.lastPoppedStackElement();
                output.println(monkeyObject.inspect());
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
