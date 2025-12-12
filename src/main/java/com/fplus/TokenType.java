package com.fplus;

/**
 * Defines all token types recognized by the FPlus/Ponz lexer.
 * <p>
 * Tokens are categorized into keywords, identifiers, literals, symbols,
 * logical/relational operators, Erlang-specific types, and formatting tokens.
 * These token types are used during lexical analysis to break down source code
 * into meaningful units for the parser.
 * 
 * @see Lexer
 * @see Token
 */
public enum TokenType {
    // Keywords - Language reserved words for control flow, declarations, and
    // operations
    /** Variable binding with immutable default: {@code let x = 5} */
    LET,
    /** Variable modifier for mutable bindings: {@code mutable y = 10} */
    MUTABLE,
    /** Type alias declaration: {@code type MyType = ...} */
    TYPE,
    /** Structure type definition: {@code struct Point { x, y }} */
    STRUCT,
    /** Class declaration for object-oriented programming */
    CLASS,
    /** Object instantiation: {@code new MyClass()} */
    NEW,
    /** Memory deallocation: {@code delete obj} */
    DELETE,
    /** Conditional statement start: {@code if condition} */
    IF,
    /** Conditional true branch: {@code then expr} */
    THEN,
    /** Conditional false branch: {@code else expr} */
    ELSE,
    /** Print statement for output: {@code print value} */
    PRINT,
    /**
     * Implementation block for traits/protocols: {@code impl MyTrait for MyType}
     */
    IMPL,
    /** Function declaration: {@code fun name(params) { ... }} */
    FUN,
    /** Early return from function: {@code return value} */
    RETURN,
    /** Self-reference in class/struct methods: {@code this.field} */
    THIS,
    /** Loop construct: {@code while condition { ... }} */
    WHILE,
    /** Module import: {@code import module} */
    IMPORT,
    /** Import aliasing: {@code import module as alias} */
    AS,

    // Identifiers and Literals - User-defined names and constant values
    /** User-defined name for variables, functions, types, etc. */
    IDENTIFIER,
    /** Numeric literal: {@code 42}, {@code 3.14} */
    NUMBER,
    /** String literal: {@code "hello world"} */
    STRING,

    // Symbols - Punctuation and delimiters
    /** Assignment operator: {@code =} */
    ASSIGN,
    /** Type annotation separator: {@code x: Int} */
    COLON,
    /** Statement terminator: {@code ;} */
    SEMICOLON,
    /** Member access: {@code obj.field} */
    DOT,
    /** Argument/element separator: {@code ,} */
    COMMA,
    /** Left parenthesis: {@code (} */
    LPAREN,
    /** Right parenthesis: {@code )} */
    RPAREN,
    /** Left brace/block start: <code>{</code> */
    LBRACE,
    /** Right brace/block end: <code>}</code> */
    RBRACE,
    /** Left bracket/array start: {@code [} */
    LBRACKET,
    /** Right bracket/array end: {@code ]} */
    RBRACKET,
    /** Addition operator: {@code +} */
    PLUS,
    /** Subtraction or negation operator: {@code -} */
    MINUS,
    /** Multiplication operator: {@code *} */
    STAR,
    /** Division operator: {@code /} */
    SLASH,
    /** Logical NOT operator: {@code !} */
    BANG,

    // Logic & Relation - Comparison and equality operators
    /** Equality comparison: {@code ==} */
    EQUAL_EQUAL,
    /** Inequality comparison: {@code !=} */
    BANG_EQUAL,
    /** Less than comparison: {@code <} */
    LESS,
    /** Less than or equal comparison: {@code <=} */
    LESS_EQUAL,
    /** Greater than comparison: {@code >} */
    GREATER,
    /** Greater than or equal comparison: {@code >=} */
    GREATER_EQUAL,

    // Erlang Types - Tokens specific to Erlang-inspired data structures
    /** Atom literal (symbol): {@code :atom_name} */
    ATOM,
    /** Map literal start: {@code #{} */
    MAP_START,
    /** Map key-value separator: {@code =>} */
    ARROW,

    // Formatting - Whitespace and indentation-based structure
    /** Newline character marking end of line */
    NEWLINE,
    /** Indentation increase (for significant whitespace) */
    INDENT,
    /** Indentation decrease (for significant whitespace) */
    DEDENT,
    /** End of file marker */
    EOF
}
