package com.fplus;

public enum TokenType {
    // Keywords
    LET, MUTABLE, TYPE, STRUCT, CLASS, NEW, DELETE, IF, THEN, ELSE, PRINT,
    IMPL, FUN, RETURN, THIS, WHILE, IMPORT, AS,

    // Identifiers and Literals
    IDENTIFIER, NUMBER, STRING,

    // Symbols
    ASSIGN, COLON, SEMICOLON, DOT, COMMA,
    LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET,
    PLUS, MINUS, STAR, SLASH, BANG,

    // Logic & Relation
    EQUAL_EQUAL, BANG_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,

    // Erlang Types
    ATOM, MAP_START, ARROW,

    // Formatting
    NEWLINE, INDENT, DEDENT, EOF
}
