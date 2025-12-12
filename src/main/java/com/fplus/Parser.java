package com.fplus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fplus.ast.*;
import static com.fplus.TokenType.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            while (match(NEWLINE))
                ;
            if (isAtEnd())
                break;

            Stmt stmt = declaration();
            if (stmt != null)
                statements.add(stmt);
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(FUN))
                return function("function");
            if (match(LET))
                return letDeclaration();
            if (match(TYPE))
                return typeDeclaration();
            if (match(IMPL))
                return implDeclaration();
            if (match(IMPORT))
                return importDeclaration();
            if (match(DELETE))
                return deleteStatement();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt letDeclaration() {
        boolean isMutable = false;
        if (match(MUTABLE)) {
            isMutable = true;
        }

        Token name = consume(IDENTIFIER, "Expect variable name.");
        consume(ASSIGN, "Expect '=' after variable name.");
        Expr initializer = expression();
        consume(NEWLINE, "Expect newline after let declaration.");
        return new Stmt.Let(name, initializer, isMutable);
    }

    private Stmt typeDeclaration() {
        Token name = consume(IDENTIFIER, "Expect type name.");
        consume(ASSIGN, "Expect '=' after type name.");

        String kind = "struct";
        if (match(CLASS))
            kind = "class";
        else if (match(STRUCT))
            kind = "struct";
        else
            throw error(peek(), "Expect 'class' or 'struct'.");

        consume(LBRACE, "Expect '{' before type body.");

        List<String> fields = new ArrayList<>();
        if (!check(RBRACE)) {
            do {
                if (check(RBRACE))
                    break;
                while (match(NEWLINE))
                    ;
                if (check(RBRACE))
                    break;

                Token fieldName = consume(IDENTIFIER, "Expect field name.");
                consume(COLON, "Expect ':' after field name.");
                consume(IDENTIFIER, "Expect field type.");
                fields.add(fieldName.lexeme);

                if (match(COMMA, SEMICOLON))
                    ;
                match(NEWLINE);
            } while (!check(RBRACE) && !isAtEnd());
        }

        consume(RBRACE, "Expect '}' after type body.");
        match(NEWLINE);

        return new Stmt.Type(name, kind, fields);
    }

    private Stmt implDeclaration() {
        Token name = consume(IDENTIFIER, "Expect type name to implement.");
        consume(COLON, "Expect ':' after type name.");
        consume(NEWLINE, "Expect newline before 'impl' block.");
        consume(INDENT, "Expect indentation start for 'impl' block.");

        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(DEDENT) && !isAtEnd()) {
            while (match(NEWLINE))
                ;
            if (check(DEDENT))
                break;

            if (match(FUN)) {
                methods.add(function("method"));
            } else {
                throw error(peek(), "Expect 'fun' inside 'impl'.");
            }
        }

        consume(DEDENT, "Expect DEDENT after 'impl' block.");
        return new Stmt.Impl(name, methods);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        consume(LPAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RPAREN)) {
            do {
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RPAREN, "Expect ')' after parameters.");
        consume(COLON, "Expect ':' before " + kind + " body.");
        consume(NEWLINE, "Expect newline before " + kind + " body.");

        consume(INDENT, "Expect indentation start for " + kind + " body.");
        List<Stmt> body = blockBody();

        return new Stmt.Function(name, parameters, body);
    }

    private Stmt importDeclaration() {
        Token path = consume(STRING, "Expect module path.");
        consume(AS, "Expect 'as' after import path.");
        Token alias = consume(IDENTIFIER, "Expect module alias.");
        consume(NEWLINE, "Expect newline after import.");
        return new Stmt.Import(path, alias);
    }

    private Stmt deleteStatement() {
        Token name = consume(IDENTIFIER, "Expect variable name to delete.");
        consume(NEWLINE, "Expect newline after delete.");
        return new Stmt.Delete(name);
    }

    private Stmt statement() {
        if (match(IF))
            return ifStatement();
        if (match(WHILE))
            return whileStatement();
        if (match(PRINT))
            return printStatement();
        if (match(RETURN))
            return returnStatement();
        if (match(INDENT))
            return new Stmt.Block(blockBody());

        return expressionStatement();
    }

    private List<Stmt> blockBody() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(DEDENT) && !isAtEnd()) {
            stmtsInto(statements);
        }
        consume(DEDENT, "Expect DEDENT after block.");
        return statements;
    }

    private void stmtsInto(List<Stmt> statements) {
        while (match(NEWLINE))
            ; // Skip blank lines
        if (check(DEDENT))
            return;

        Stmt stmt = declaration();
        if (stmt != null)
            statements.add(stmt);
    }

    private Stmt ifStatement() {
        Expr condition = expression();
        consume(COLON, "Expect ':' after if condition.");
        consume(NEWLINE, "Expect newline after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;

        while (match(NEWLINE))
            ; // Skip blank lines before else (including DEDENT terminators)

        if (match(ELSE)) {
            consume(COLON, "Expect ':' after else.");
            consume(NEWLINE, "Expect newline after else.");
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        Expr condition = expression();
        consume(COLON, "Expect ':' after while condition.");
        consume(NEWLINE, "Expect newline after while condition.");

        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(NEWLINE, "Expect newline after value.");
        return new Stmt.Print(value);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(NEWLINE)) {
            value = expression();
        }
        consume(NEWLINE, "Expect newline after return.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        if (!isAtEnd()) {
            consume(NEWLINE, "Expect newline after expression.");
        }
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = equality();

        if (match(ASSIGN)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            } else if (expr instanceof Expr.Dereference) {
                Expr.Dereference deref = (Expr.Dereference) expr;
                // ptr.* = value
                // Logic: PointerSet(ptr, value)
                return new Expr.PointerSet(deref.expression, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(EQUAL_EQUAL, BANG_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS, STAR)) {
            Token operator = previous();
            Expr right = unary();

            if (operator.type == STAR) {
                if (right instanceof Expr.Variable) {
                    return new Expr.AddressOf(((Expr.Variable) right).name);
                }
                if (!(right instanceof Expr.Variable)) {
                    throw error(operator, "Can only take address of a variable.");
                }
            }

            return new Expr.Unary(operator, right);
        }
        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LPAREN)) {
                List<Expr> arguments = new ArrayList<>();
                if (!check(RPAREN)) {
                    do {
                        arguments.add(expression());
                    } while (match(COMMA));
                }
                Token paren = consume(RPAREN, "Expect ')' after arguments.");

                expr = new Expr.Call(expr, paren, arguments);
            } else if (match(DOT)) {
                if (match(STAR)) {
                    expr = new Expr.Dereference(expr);
                } else {
                    Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                    expr = new Expr.Get(expr, name);
                }
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr primary() {
        if (match(THIS))
            return new Expr.This(previous());

        if (match(NEW)) {
            Token type = consume(IDENTIFIER, "Expect type name after new.");
            consume(LPAREN, "Expect '(' after type name.");
            List<Expr> arguments = new ArrayList<>();
            if (!check(RPAREN)) {
                do {
                    arguments.add(expression());
                } while (match(COMMA));
            }
            consume(RPAREN, "Expect ')' after arguments.");
            return new Expr.New(type, arguments);
        }

        if (match(LBRACKET)) {
            // Disambiguation: List Literal vs Lambda Capture
            // Lambda Capture: [ ident1, ident2 ] (
            // List Literal: [ expr1, expr2 ]

            // Checking if it's a lambda:
            // 1. All elements inside [] must be identifiers.
            // 2. Must be followed by (

            boolean isLambda = looksLikeLambda();

            if (isLambda) {
                // Parse Lambda
                // We already consumed LBRACKET.
                List<Expr> captures = new ArrayList<>();
                if (!check(RBRACKET)) {
                    do {
                        if (match(STAR)) {
                            Token name = consume(IDENTIFIER, "Expect identifier after '*'.");
                            captures.add(new Expr.AddressOf(name));
                        } else {
                            Token name = consume(IDENTIFIER, "Expect capture identifier.");
                            captures.add(new Expr.Variable(name));
                        }
                    } while (match(COMMA));
                }
                consume(RBRACKET, "Expect ']' after captures.");

                consume(LPAREN, "Expect '(' after captures.");
                List<Token> params = new ArrayList<>();
                if (!check(RPAREN)) {
                    do {
                        params.add(consume(IDENTIFIER, "Expect parameter name."));
                    } while (match(COMMA));
                }
                consume(RPAREN, "Expect ')' after parameters.");
                consume(COLON, "Expect ':' before lambda body.");

                // Allow inline body or block
                List<Stmt> body = new ArrayList<>();
                if (match(NEWLINE)) {
                    consume(INDENT, "Expect indentation string for lambda block.");
                    body = blockBody();
                } else {
                    throw error(peek(), "Expect newline and indent after lambda header.");
                }

                return new Expr.Lambda(captures, params, body);

            } else {
                // Parse List Literal
                List<Expr> elements = new ArrayList<>();
                if (!check(RBRACKET)) {
                    do {
                        elements.add(expression());
                    } while (match(COMMA));
                }
                consume(RBRACKET, "Expect ']' after list elements.");
                return new Expr.ListLiteral(elements);
            }
        }

        if (match(NUMBER)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LPAREN)) {
            Expr expr = expression();
            consume(RPAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        if (match(ATOM)) {
            return new Expr.Atom(previous());
        }

        if (match(MAP_START)) {
            List<Expr> keys = new ArrayList<>();
            List<Expr> values = new ArrayList<>();
            if (!check(RBRACE)) {
                do {
                    keys.add(expression());
                    consume(ARROW, "Expect '=>' after map key.");
                    values.add(expression());
                } while (match(COMMA));
            }
            consume(RBRACE, "Expect '}' after map entries.");
            return new Expr.MapLiteral(keys, values);
        }

        if (match(LBRACE)) {
            List<Expr> elements = new ArrayList<>();
            if (!check(RBRACE)) {
                do {
                    elements.add(expression());
                } while (match(COMMA));
            }
            consume(RBRACE, "Expect '}' after tuple elements.");
            return new Expr.Tuple(elements);
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean looksLikeLambda() {
        // Current is just after LBRACKET.
        // Scan ahead.
        // Expect Identifier (Comma Identifier)* RBracket LParen
        // OR [ *Ident ... ]
        int i = 0;

        // Handle empty capture []
        if (peek(i).type == RBRACKET && peek(i + 1).type == LPAREN)
            return true;

        // Handle Identifier/Comma sequence
        while (true) {
            if (peek(i).type == STAR) {
                i++;
                if (peek(i).type == IDENTIFIER) {
                    // *Ident treated same as Ident for lookahead purposes
                } else {
                    return false; // Error if * not followed by Ident
                }
            }

            if (peek(i).type == IDENTIFIER) {
                i++;
                if (peek(i).type == COMMA) {
                    i++;
                    continue;
                } else if (peek(i).type == RBRACKET) {
                    i++;
                    if (peek(i).type == LPAREN)
                        return true;
                    return false;
                } else {
                    return false; // Invalid lambda capture syntax
                }
            } else if (peek(i).type == RBRACKET) {
                return false;
            } else {
                return false; // Not an identifier
            }
        }
    }

    // Helper to peek ahead arbitrarily
    private Token peek(int offset) {
        if (current + offset >= tokens.size())
            return tokens.get(tokens.size() - 1);
        return tokens.get(current + offset);
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        System.err.println("Error at " + token.type + " line " + token.line + ": " + message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == NEWLINE)
                return;
            switch (peek().type) {
                case CLASS:
                case STRUCT:
                case LET:
                case IF:
                case PRINT:
                case TYPE:
                case DELETE:
                case IMPL:
                case FUN:
                case RETURN:
                case WHILE:
                    return;
            }
            advance();
        }
    }

    private static class ParseError extends RuntimeException {
    }
}
