package com.fplus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import com.fplus.ast.Stmt;

public class Main {
    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        String bytes = new String(Files.readAllBytes(Paths.get(path)));
        run(bytes);
    }

    private static void runPrompt() {
        System.out.println("FPlus REPL");
        System.out.println("Usage: java com.fplus.Main [script]");
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        // Debug Tokens
        // for (Token token : tokens)
        // System.out.println(token);

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (statements == null)
            return;

        interpreter.interpret(statements);
    }
}
