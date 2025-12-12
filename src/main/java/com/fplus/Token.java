package com.fplus;

/**
 * Represents a single lexical token in the FPlus/Ponz source code.
 * <p>
 * A token is the smallest meaningful unit of source code, produced by the lexer
 * and consumed by the parser. Each token contains its type, the actual text
 * from
 * the source (lexeme), an optional literal value for constants, and location
 * information for error reporting.
 * 
 * @see TokenType
 * @see Lexer
 */
public class Token {
    /** The classification of this token (keyword, operator, literal, etc.) */
    public final TokenType type;

    /** The actual text string from the source code that this token represents */
    public final String lexeme;

    /**
     * The parsed literal value for NUMBER, STRING, and ATOM tokens; null otherwise
     */
    public final Object literal;

    /** The line number where this token appears in the source code (1-indexed) */
    public final int line;

    /**
     * Constructs a new token.
     * 
     * @param type    The type of token
     * @param lexeme  The raw text from source code
     * @param literal The parsed value for literal tokens (may be null)
     * @param line    The source line number for error reporting
     */
    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    /**
     * Returns a string representation of this token for debugging.
     * Format: "TYPE lexeme literal" (literal omitted if null)
     * 
     * @return A debug string showing token details
     */
    public String toString() {
        return type + " " + lexeme + " " + (literal != null ? literal : "");
    }
}
