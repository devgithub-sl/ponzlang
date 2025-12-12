package com.fplus.ast;

import java.util.List;
import com.fplus.Token;

public abstract class Expr {
    public interface Visitor<R> {
        R visitBinaryExpr(Binary expr);

        R visitGroupingExpr(Grouping expr);

        R visitUnaryExpr(Unary expr); // Added

        R visitLiteralExpr(Literal expr);

        R visitVariableExpr(Variable expr);

        R visitAssignExpr(Assign expr);

        R visitNewExpr(New expr);

        R visitGetExpr(Get expr);

        R visitSetExpr(Set expr);

        R visitCallExpr(Call expr);

        R visitThisExpr(This expr);

        R visitListExpr(ListLiteral expr);

        R visitLambdaExpr(Lambda expr);

        R visitAddressOfExpr(AddressOf expr); // Added

        R visitDereferenceExpr(Dereference expr); // Added

        R visitPointerSetExpr(PointerSet expr);

        R visitAtomExpr(Atom expr);

        R visitTupleExpr(Tuple expr);

        R visitMapLiteralExpr(MapLiteral expr);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static class Atom extends Expr {
        public final Token token;

        public Atom(Token token) {
            this.token = token;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAtomExpr(this);
        }
    }

    public static class Tuple extends Expr {
        public final List<Expr> elements;

        public Tuple(List<Expr> elements) {
            this.elements = elements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTupleExpr(this);
        }
    }

    public static class MapLiteral extends Expr {
        public final List<Expr> keys;
        public final List<Expr> values;

        public MapLiteral(List<Expr> keys, List<Expr> values) {
            this.keys = keys;
            this.values = values;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitMapLiteralExpr(this);
        }
    }

    public static class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class Grouping extends Expr {
        public final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    public static class Unary extends Expr { // Added
        public final Token operator;
        public final Expr right;

        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class Literal extends Expr {
        public final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Variable extends Expr {
        public final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    public static class New extends Expr {
        public final Token className;
        public final List<Expr> arguments;

        public New(Token className, List<Expr> arguments) {
            this.className = className;
            this.arguments = arguments;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitNewExpr(this);
        }
    }

    public static class Get extends Expr {
        public final Expr object;
        public final Token name;

        public Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    public static class Set extends Expr {
        public final Expr object;
        public final Token name;
        public final Expr value;

        public Set(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    public static class Call extends Expr {
        public final Expr callee;
        public final Token paren;
        public final List<Expr> arguments;

        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    public static class This extends Expr {
        public final Token keyword;

        public This(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpr(this);
        }
    }

    public static class ListLiteral extends Expr {
        public final List<Expr> elements;

        public ListLiteral(List<Expr> elements) {
            this.elements = elements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitListExpr(this);
        }
    }

    public static class Lambda extends Expr {
        public final List<Expr> captures;
        public final List<Token> params;
        public final List<Stmt> body;

        public Lambda(List<Expr> captures, List<Token> params, List<Stmt> body) {
            this.captures = captures;
            this.params = params;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLambdaExpr(this);
        }
    }

    public static class AddressOf extends Expr {
        public final Token name;

        public AddressOf(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAddressOfExpr(this);
        }
    }

    public static class Dereference extends Expr {
        public final Expr expression;

        public Dereference(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitDereferenceExpr(this);
        }
    }

    public static class PointerSet extends Expr {
        public final Expr pointer;
        public final Expr value;

        public PointerSet(Expr pointer, Expr value) {
            this.pointer = pointer;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPointerSetExpr(this);
        }
    }

}
