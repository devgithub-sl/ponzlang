package com.fplus.ast;

import java.util.List;
import com.fplus.Token;

public abstract class Stmt {
    public interface Visitor<R> {
        R visitExpressionStmt(Expression stmt);

        R visitLetStmt(Let stmt);

        R visitBlockStmt(Block stmt);

        R visitIfStmt(If stmt);

        R visitWhileStmt(While stmt); // New support for loops

        R visitPrintStmt(Print stmt);

        R visitTypeStmt(Type stmt);

        R visitImplStmt(Impl stmt);

        R visitFunctionStmt(Function stmt);

        R visitReturnStmt(Return stmt);

        R visitDeleteStmt(Delete stmt);

        R visitImportStmt(Import stmt);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static class Expression extends Stmt {
        public final Expr expression;

        public Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    public static class Let extends Stmt {
        public final Token name;
        public final Expr initializer;
        public final boolean isMutable;

        public Let(Token name, Expr initializer, boolean isMutable) {
            this.name = name;
            this.initializer = initializer;
            this.isMutable = isMutable;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLetStmt(this);
        }
    }

    public static class Block extends Stmt {
        public final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    public static class While extends Stmt {
        public final Expr condition;
        public final Stmt body;

        public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    public static class Print extends Stmt {
        public final Expr expression;

        public Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    public static class Type extends Stmt {
        public final Token name;
        public final String kind;
        public final List<String> fields;

        public Type(Token name, String kind, List<String> fields) {
            this.name = name;
            this.kind = kind;
            this.fields = fields;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTypeStmt(this);
        }
    }

    public static class Impl extends Stmt {
        public final Token name;
        public final List<Stmt.Function> methods;

        public Impl(Token name, List<Stmt.Function> methods) {
            this.name = name;
            this.methods = methods;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitImplStmt(this);
        }
    }

    public static class Function extends Stmt {
        public final Token name;
        public final List<Token> params;
        public final List<Stmt> body;

        public Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    public static class Return extends Stmt {
        public final Token keyword;
        public final Expr value;

        public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    public static class Delete extends Stmt {
        public final Token name;

        public Delete(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitDeleteStmt(this);
        }
    }

    public static class Import extends Stmt {
        public final Token path;
        public final Token alias;

        public Import(Token path, Token alias) {
            this.path = path;
            this.alias = alias;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitImportStmt(this);
        }
    }
}
