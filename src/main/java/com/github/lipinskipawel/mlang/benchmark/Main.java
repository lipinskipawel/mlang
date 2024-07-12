package com.github.lipinskipawel.mlang.benchmark;

import com.github.lipinskipawel.mlang.evaluator.Environment;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.parser.Parser;

import java.time.Duration;

import static com.github.lipinskipawel.mlang.compiler.Compiler.compiler;
import static com.github.lipinskipawel.mlang.evaluator.Evaluator.evaluator;
import static com.github.lipinskipawel.mlang.lexer.Lexer.lexer;
import static com.github.lipinskipawel.mlang.vm.VirtualMachine.virtualMachine;
import static java.lang.System.nanoTime;
import static java.time.temporal.ChronoUnit.NANOS;
import static java.util.Optional.ofNullable;

public final class Main {
    private static final String BENCHMARK_PROGRAM = """
            let fibonacci = fn(x) {
              if (x == 0) {
                0
              } else {
                if (x == 1) {
                  return 1;
                } else {
                  fibonacci(x - 1) + fibonacci(x - 2);
                }
              }
            };
            fibonacci(40);
            """;

    public static void main(String[] args) {
        final var engine = parseArgument(args);
        MonkeyObject result;
        Duration duration;

        final var lexer = lexer(BENCHMARK_PROGRAM);
        final var parser = new Parser(lexer);
        final var program = parser.parseProgram();

        if (engine.equals("eval")) {
            final var environment = new Environment();
            final var evaluator = evaluator();
            final var start = nanoTime();

            result = evaluator.eval(program, environment);
            final var end = nanoTime();
            duration = Duration.of(end - start, NANOS);
        } else {
            final var compiler = compiler();
            var error = compiler.compile(program);
            if (error.isPresent()) {
                System.out.printf("compiler error [%s]%n", error.get());
                return;
            }

            final var vm = virtualMachine(compiler.bytecode());
            final var start = nanoTime();
            vm.run();
            final var end = nanoTime();

            result = vm.lastPoppedStackElement();
            duration = Duration.of(end - start, NANOS);
        }

        System.out.printf("engine=%s, result=%s, duration=%s", engine, result.inspect(), duration);
    }

    private static String parseArgument(String[] args) {
        final var chosenEngine = ofNullable(args.length == 0 ? null : args[0])
                .map(it -> it.split("="))
                .filter(it -> it[0].equals("--engine"))
                .map(it -> it[1])
                .filter(it -> it.equals("eval") || it.equals("vm"));
        if (chosenEngine.isEmpty()) {
            System.err.println("Engine has not been chosen. Fallback to eval");
            System.err.println("Usage: --engine=eval or --engine=vm");
            return "eval";
        }
        return chosenEngine.get();
    }
}
