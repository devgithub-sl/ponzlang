package com.fplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // Indentation handling
    private final Stack<Integer> indentStack = new Stack<>();
    private int currentIndent = 0;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("let", TokenType.LET);
        keywords.put("mutable", TokenType.MUTABLE);
        keywords.put("type", TokenType.TYPE);
        keywords.put("struct", TokenType.STRUCT);
        keywords.put("class", TokenType.CLASS);
        keywords.put("new", TokenType.NEW);
        keywords.put("delete", TokenType.DELETE);
        keywords.put("if", TokenType.IF);
        keywords.put("then", TokenType.THEN);
        keywords.put("else", TokenType.ELSE);
        keywords.put("print", TokenType.PRINT);
        keywords.put("impl", TokenType.IMPL);
        keywords.put("fun", TokenType.FUN);
        keywords.put("return", TokenType.RETURN);
        keywords.put("this", TokenType.THIS);
        keywords.put("while", TokenType.WHILE);
        keywords.put("import", TokenType.IMPORT);
        keywords.put("as", TokenType.AS);
    }

    public Lexer(String source) {
        this.source = source;
        this.indentStack.push(0);
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        // Dedent back to 0 at EOF
        while (indentStack.peek() > 0) {
            indentStack.pop();
            tokens.add(new Token(TokenType.DEDENT, "", null, line));
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        if (current == 0 || (current > 0 && source.charAt(current - 1) == '\n')) {
            handleIndentation();
            if (isAtEnd())
                return; // check again after indentation handling
            start = current;
        }

        char c = advance();
        switch (c) {
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace that isn't at the start of a line (handled by
                // handleIndentation)
                break;
            case '\n':
                line++;
                tokens.add(new Token(TokenType.NEWLINE, "\n", null, line));
                break;
            case '(':
                addToken(TokenType.LPAREN);
                break;
            case ')':
                addToken(TokenType.RPAREN);
                break;
            case '{':
                addToken(TokenType.LBRACE);
                break;
            case '}':
                addToken(TokenType.RBRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            case ':':
                addToken(TokenType.COLON);
                break;
            case '"':
                string();
                break;
            case '[':
                addToken(TokenType.LBRACKET);
                break;
            case ']':
                addToken(TokenType.RBRACKET);
                break;

            case '!':
                if (match('='))
                    addToken(TokenType.BANG_EQUAL);
                else
                    System.err.println("Unexpected character '!'.");
                break;
            case '@':
                if (isAlpha(peek())) {
                    atom();
                } else {
                    System.err.println("Unexpected character '@'.");
                }
                break;
            case '#':
                if (match('{')) {
                    addToken(TokenType.MAP_START);
                } else {
                    System.err.println("Unexpected character '#'.");
                }
                break;
            case '=':
                if (match('='))
                    addToken(TokenType.EQUAL_EQUAL);
                else if (match('>'))
                    addToken(TokenType.ARROW);
                else
                    addToken(TokenType.ASSIGN);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    // Error: Unexpected character.
                    System.err.println("Unexpected character at line " + line + ": " + c);
                }
                break;
        }
    }

    private void handleIndentation() {
        int spaces = 0;
        while (peek() == ' ' || peek() == '\t') {
            if (peek() == '\t')
                spaces += 4; // Assume tab is 4 spaces
            else
                spaces++;
            advance();
        }

        // If it's a blank line or comment line, don't generate indent tokens
        if (peek() == '\n' || peek() == '\r' || peek() == '/' && peekNext() == '/') {
            return;
        }

        // This is a bit simplistic for a real F# lexer but works for a prototype
        // Actually, scanToken is called in a loop.
        // We need to emit NEWLINE before INDENT/DEDENT ideally, which we handled in the
        // previous char scan.

        int currentLevel = indentStack.peek();

        if (spaces > currentLevel) {
            indentStack.push(spaces);
            tokens.add(new Token(TokenType.INDENT, "", null, line));
        } else if (spaces < currentLevel) {
            while (spaces < indentStack.peek()) {
                indentStack.pop();
                tokens.add(new Token(TokenType.DEDENT, "", null, line));
            }
            // Emit NEWLINE after dedent to act as statement separator
            tokens.add(new Token(TokenType.NEWLINE, "\n", null, line));

            if (indentStack.peek() != spaces) {
                System.err.println("Error: Inconsistent indentation at line " + line);
            }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null)
            type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek()))
            advance();
        addToken(TokenType.NUMBER, Integer.parseInt(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }

        if (isAtEnd()) {
            System.err.println("Unterminated string at line " + line);
            return;
        }

        advance(); // The closing ".

        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private void atom() {
        while (isAlphaNumeric(peek()))
            advance();
        // substring(start + 1) to skip the '@'
        String text = source.substring(start + 1, current);
        addToken(TokenType.ATOM, text);
    }

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
