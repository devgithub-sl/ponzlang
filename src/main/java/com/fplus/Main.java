package com.fplus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import com.fplus.ast.Stmt;

/**
 * Main entry point for the FPlus/Ponz interpreter.
 * <p>
 * This class provides the command-line interface for running FPlus/Ponz
 * programs.
 * It coordinates the three-stage execution pipeline:
 * <ol>
 * <li><b>Lexical Analysis</b> - {@link Lexer} converts source code to tokens
 * <li><b>Parsing</b> - {@link Parser} builds an Abstract Syntax Tree (AST)
 * <li><b>Interpretation</b> - {@link Interpreter} executes the AST
 * </ol>
 * <p>
 * Usage: {@code java com.fplus.Main [script.fplus]}
 * <ul>
 * <li>With arguments: Executes the specified source file
 * <li>Without arguments: Shows usage information (REPL not yet implemented)
 * </ul>
 * 
 * @see Lexer
 * @see Parser
 * @see Interpreter
 */
public class Main {
    /** Shared interpreter instance for executing FPlus/Ponz code */
    private static final Interpreter interpreter = new Interpreter();

    /**
     * Main entry point for the interpreter.
     * 
     * @param args Command-line arguments; first argument is the script file path
     * @throws IOException if the source file cannot be read
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /**
     * Reads and executes a FPlus/Ponz source file.
     * 
     * @param path Path to the source file
     * @throws IOException if the file cannot be read
     */
    private static void runFile(String path) throws IOException {
        String bytes = new String(Files.readAllBytes(Paths.get(path)));
        run(bytes);
    }

    /**
     * Displays usage information.
     * <p>
     * Note: Interactive REPL is not yet implemented.
     */
    private static void runPrompt() {
        System.out.println("FPlus REPL");
        System.out.println("Usage: java com.fplus.Main [script]");
    }

    /**
     * Executes FPlus/Ponz source code through the lexer-parser-interpreter
     * pipeline.
     * <p>
     * Steps:
     * <ol>
     * <li>Tokenize source with {@link Lexer}
     * <li>Parse tokens into AST with {@link Parser}
     * <li>Execute AST with {@link Interpreter}
     * </ol>
     * 
     * @param source The complete source code to execute
     */
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
