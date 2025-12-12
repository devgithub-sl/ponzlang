package com.fplus;

import com.fplus.ast.*;
import com.fplus.runtime.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * Tree-walking interpreter for FPlus/Ponz that executes the Abstract Syntax
 * Tree.
 * <p>
 * This interpreter implements the Visitor pattern to traverse and execute AST
 * nodes
 * produced by the {@link Parser}. It provides:
 * <ul>
 * <li><b>Dynamic typing</b> with type inference and enforcement
 * <li><b>Memory management</b> via Automatic Reference Counting (ARC)
 * <li><b>Lexical scoping</b> with nested environments
 * <li><b>First-class functions</b> including lambdas with closures
 * <li><b>Object-oriented features</b> - structs, classes, methods
 * <li><b>Concurrency</b> - thread spawning with shared heap
 * <li><b>Module system</b> - file imports with isolated scopes
 * <li><b>Erlang-inspired types</b> - atoms, tuples, maps
 * <li><b>Pointer operations</b> - address-of, dereference (experimental)
 * </ul>
 * <p>
 * Key implementation details:
 * <ul>
 * <li><b>Value semantics</b>: Most values are copied on assignment; classes use
 * reference semantics
 * <li><b>ARC memory model</b>: Values are retain/release counted for safe
 * memory management
 * <li><b>Shared resources</b>: Type definitions, methods, and heap are shared
 * across modules/threads
 * <li><b>Native functions</b>: Provides built-ins like {@code time()},
 * {@code len()}, {@code spawn()}, etc.
 * </ul>
 * <p>
 * Supported statements:
 * {@code let}, {@code mutable}, {@code type}, {@code struct}, {@code class},
 * {@code impl},
 * {@code fun}, {@code if/else}, {@code while}, {@code print}, {@code return},
 * {@code import}
 * <p>
 * Supported expressions:
 * Literals, variables, binary/unary operators, function calls, property access,
 * lambdas, lists, tuples, maps, atoms, pointers
 * 
 * @see Parser
 * @see Environment
 * @see Heap
 * @see Value
 */
public class Interpreter implements Expr.Visitor<Value>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();
    private final Heap heap; // Shared Heap
    private final Map<String, Stmt.Type> typeDefinitions; // Shared TypeDefs
    private final Map<String, Map<String, Stmt.Function>> methods; // Shared Methods

    public Interpreter() {
        this.heap = new Heap();
        this.typeDefinitions = new HashMap<>();
        this.methods = new HashMap<>();
        registerNativeFunctions();
    }

    // Private constructor for spawning threads/modules with shared resources
    private Interpreter(Heap heap, Map<String, Stmt.Type> typeDefinitions,
            Map<String, Map<String, Stmt.Function>> methods) {
        this.heap = heap;
        this.typeDefinitions = typeDefinitions;
        this.methods = methods;
        registerNativeFunctions();
    }

    // Spawn a new thread running the lambda
    private void spawnThread(FunctionValue lambda) {
        // Create a new Interpreter instance sharing the same Heap and Definitions
        Interpreter threadInterpreter = new Interpreter(this.heap, this.typeDefinitions, this.methods);

        Thread thread = new Thread(() -> {
            try {
                // Execute the lambda body in its closure
                threadInterpreter.executeBlock(lambda.body, lambda.closure);
            } catch (Exception e) {
                System.err.println("Thread Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void registerNativeFunctions() {
        environment.define("time", new NativeFunction() {
            @Override
            public Value call(List<Value> args, Heap heap) {
                return new PrimitiveValue((int) (System.currentTimeMillis() / 1000));
            }

            @Override
            public String toString() {
                return "<native fn time>";
            }
        }, false);

        environment.define("len", new NativeFunction() {
            @Override
            public Value call(List<Value> args, Heap heap) {
                if (args.size() != 1)
                    throw new RuntimeException("len() takes 1 argument.");
                Value arg = args.get(0);
                if (arg instanceof ListValue) {
                    return new PrimitiveValue(((ListValue) arg).size());
                }
                throw new RuntimeException("len() argument must be a list.");
            }

            @Override
            public String toString() {
                return "<native fn len>";
            }
        }, false);

        environment.define("push", new NativeFunction() {
            @Override
            public Value call(List<Value> args, Heap heap) {
                if (args.size() != 2)
                    throw new RuntimeException("push() takes 2 arguments (list, item).");
                Value list = args.get(0);
                Value item = args.get(1);

                if (list instanceof ListValue) {
                    Value itemCopy = item.copy();
                    itemCopy.retain(heap);
                    ((ListValue) list).add(itemCopy);
                    return null;
                }
                throw new RuntimeException("push() first argument must be a list.");
            }

            @Override
            public String toString() {
                return "<native fn push>";
            }
        }, false);

        environment.define("get", new NativeFunction() {
            @Override
            public Value call(List<Value> args, Heap heap) {
                if (args.size() != 2)
                    throw new RuntimeException("get() takes 2 arguments (list, index).");
                Value list = args.get(0);
                Value index = args.get(1);

                if (list instanceof ListValue && index.getRaw() instanceof Integer) {
                    ListValue lv = (ListValue) list;
                    int idx = (int) index.getRaw();
                    if (idx < 0 || idx >= lv.size())
                        throw new RuntimeException("Index out of bounds.");
                    return lv.get(idx);
                }
                throw new RuntimeException("get() expects (list, int).");
            }

            @Override
            public String toString() {
                return "<native fn get>";
            }
        }, false);

        environment.define("sleep", new NativeFunction() {
            @Override
            public Value call(List<Value> args, Heap heap) {
                if (args.size() != 1)
                    throw new RuntimeException("sleep() takes 1 argument (ms).");
                Value msObj = args.get(0);
                if (msObj.getRaw() instanceof Integer) {
                    long ms = ((Integer) msObj.getRaw()).longValue();
                    try {
                        Thread.sleep(ms);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                }
                throw new RuntimeException("sleep() argument must be an integer.");
            }

            @Override
            public String toString() {
                return "<native fn sleep>";
            }
        }, false);

        environment.define("spawn", new NativeFunction() {
            @Override
            public Value call(List<Value> args, Heap heap) {
                if (args.size() != 1)
                    throw new RuntimeException("spawn() takes 1 argument (lambda).");

                Value funcVal = args.get(0);
                if (!(funcVal instanceof FunctionValue)) {
                    throw new RuntimeException("spawn() expects a function/lambda.");
                }

                final FunctionValue lambda = (FunctionValue) funcVal;
                spawnThread(lambda);
                return null;
            }

            @Override
            public String toString() {
                return "<native fn spawn>";
            }
        }, false);
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeException error) {
            System.err.println(error.getMessage());
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Value evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitLetStmt(Stmt.Let stmt) {
        Value value = evaluate(stmt.initializer);
        Value valueToStore = value.copy();
        valueToStore.retain(heap);
        environment.define(stmt.name.lexeme, valueToStore, stmt.isMutable);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            for (Value val : this.environment.getLocalValues()) {
                val.release(heap);
            }
            this.environment = previous;
        }
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Value value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitTypeStmt(Stmt.Type stmt) {
        typeDefinitions.put(stmt.name.lexeme, stmt);
        return null;
    }

    @Override
    public Void visitImplStmt(Stmt.Impl stmt) {
        Map<String, Stmt.Function> typeMethods = methods.computeIfAbsent(stmt.name.lexeme, k -> new HashMap<>());
        for (Stmt.Function method : stmt.methods) {
            typeMethods.put(method.name.lexeme, method);
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        FunctionValue function = new FunctionValue(stmt.params, stmt.body, environment);
        environment.define(stmt.name.lexeme, function, false);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Value value = null;
        if (stmt.value != null)
            value = evaluate(stmt.value);
        throw new ReturnException(value);
    }

    @Override
    public Void visitDeleteStmt(Stmt.Delete stmt) {
        System.out.println("Warning: Manual delete command ignored in ARC mode.");
        return null;
    }

    @Override
    public Void visitImportStmt(Stmt.Import stmt) {
        String path = stmt.path.literal.toString();
        String alias = stmt.alias.lexeme;

        try {
            String source = Files.readString(Paths.get(path));
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.scanTokens();
            Parser parser = new Parser(tokens);
            List<Stmt> statements = parser.parse();

            // Run module in a new Interpreter that shares Heap/Defs but has its own Global
            // Scope
            Interpreter moduleInterpreter = new Interpreter(this.heap, this.typeDefinitions, this.methods);
            moduleInterpreter.interpret(statements);

            // Collect exports
            Map<String, Value> exports = moduleInterpreter.environment.getExports();
            StructValue moduleObj = new StructValue("Module");
            for (Map.Entry<String, Value> entry : exports.entrySet()) {
                moduleObj.set(entry.getKey(), entry.getValue());
            }

            // Define alias in current scope
            environment.define(alias, moduleObj, false);

        } catch (IOException e) {
            throw new RuntimeException("Could not import module '" + path + "': " + e.getMessage());
        }
        return null;
    }

    @Override
    public Value visitUnaryExpr(Expr.Unary expr) {
        Value right = evaluate(expr.right);
        switch (expr.operator.type) {
            case MINUS:
                if (right.getRaw() instanceof Integer)
                    return new PrimitiveValue(-(int) right.getRaw());
                throw new RuntimeException("Operand must be a number.");
            case BANG:
                return new PrimitiveValue(!isTruthy(right));
        }
        return null;
    }

    @Override
    public Value visitAddressOfExpr(Expr.AddressOf expr) {
        Environment env = environment.resolve(expr.name.lexeme);
        if (env != null) {
            return new PointerValue(env, expr.name.lexeme);
        }
        throw new RuntimeException("Undefined variable '" + expr.name.lexeme + "'.");
    }

    @Override
    public Value visitDereferenceExpr(Expr.Dereference expr) {
        Value ptr = evaluate(expr.expression);
        if (ptr instanceof PointerValue) {
            PointerValue p = (PointerValue) ptr;
            try {
                return p.environment.get(p.name);
            } catch (Exception e) {
                throw e;
            }
        }
        throw new RuntimeException("Can only dereference a pointer.");
    }

    @Override
    public Value visitPointerSetExpr(Expr.PointerSet expr) {
        Value ptr = evaluate(expr.pointer);
        if (ptr instanceof PointerValue) {
            PointerValue p = (PointerValue) ptr;
            Value val = evaluate(expr.value);
            Value valCopy = val.copy();
            valCopy.retain(heap);
            p.environment.assign(p.name, valCopy);
            return valCopy;
        }
        throw new RuntimeException("Can only assign to a dereferenced pointer.");
    }

    @Override
    public Value visitBinaryExpr(Expr.Binary expr) {
        Value left = evaluate(expr.left);
        Value right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return new PrimitiveValue((int) left.getRaw() - (int) right.getRaw());
            case PLUS:
                if (left.getRaw() instanceof Integer && right.getRaw() instanceof Integer) {
                    return new PrimitiveValue((int) left.getRaw() + (int) right.getRaw());
                }
                if (left.getRaw() instanceof String && right.getRaw() instanceof String) {
                    return new PrimitiveValue((String) left.getRaw() + (String) right.getRaw());
                }
                throw new RuntimeException("Operands must be two numbers or two strings.");
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return new PrimitiveValue((int) left.getRaw() * (int) right.getRaw());
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return new PrimitiveValue((int) left.getRaw() / (int) right.getRaw());
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return new PrimitiveValue((int) left.getRaw() > (int) right.getRaw());
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return new PrimitiveValue((int) left.getRaw() >= (int) right.getRaw());
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return new PrimitiveValue((int) left.getRaw() < (int) right.getRaw());
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return new PrimitiveValue((int) left.getRaw() <= (int) right.getRaw());
            case BANG_EQUAL:
                return new PrimitiveValue(!isEqual(left, right));
            case EQUAL_EQUAL:
                return new PrimitiveValue(isEqual(left, right));
        }

        return null;
    }

    @Override
    public Value visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Value visitLiteralExpr(Expr.Literal expr) {
        return new PrimitiveValue(expr.value);
    }

    @Override
    public Value visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name.lexeme);
    }

    @Override
    public Value visitAtomExpr(Expr.Atom expr) {
        return new AtomValue((String) expr.token.literal);
    }

    @Override
    public Value visitTupleExpr(Expr.Tuple expr) {
        List<Value> elements = new ArrayList<>();
        for (Expr e : expr.elements) {
            elements.add(evaluate(e));
        }
        return new TupleValue(elements);
    }

    @Override
    public Value visitMapLiteralExpr(Expr.MapLiteral expr) {
        Map<Value, Value> map = new HashMap<>();
        for (int i = 0; i < expr.keys.size(); i++) {
            Value key = evaluate(expr.keys.get(i)).copy();
            Value value = evaluate(expr.values.get(i)).copy();
            map.put(key, value);
        }
        return new MapValue(map);
    }

    @Override
    public Value visitAssignExpr(Expr.Assign expr) {
        Value newValue = evaluate(expr.value);
        Value valueToStore = newValue.copy();
        valueToStore.retain(heap);

        Value oldValue = null;
        try {
            oldValue = environment.get(expr.name.lexeme);
        } catch (RuntimeException e) {
        }

        if (oldValue != null)
            oldValue.release(heap);

        environment.assign(expr.name.lexeme, valueToStore);
        return valueToStore;
    }

    @Override
    public Value visitNewExpr(Expr.New expr) {
        String typeName = expr.className.lexeme;
        Stmt.Type typeDef = typeDefinitions.get(typeName);
        if (typeDef == null) {
            throw new RuntimeException("Undefined type '" + typeName + "'.");
        }

        StructValue instance = new StructValue(typeName);

        if (expr.arguments.size() != typeDef.fields.size()) {
            throw new RuntimeException(
                    "Expected " + typeDef.fields.size() + " arguments but got " + expr.arguments.size() + ".");
        }

        for (int i = 0; i < typeDef.fields.size(); i++) {
            instance.set(typeDef.fields.get(i), evaluate(expr.arguments.get(i)).copy());
        }

        if (typeDef.kind.equals("class")) {
            String address = heap.allocate(instance);
            return new ClassReference(address, typeName);
        } else {
            return instance;
        }
    }

    @Override
    public Value visitGetExpr(Expr.Get expr) {
        Value object = evaluate(expr.object);
        StructValue instance = resolveStruct(object);

        Value result = instance.get(expr.name.lexeme);
        if (result == null) {
            throw new RuntimeException("Undefined property '" + expr.name.lexeme + "'.");
        }
        return result;
    }

    @Override
    public Value visitSetExpr(Expr.Set expr) {
        Value object = evaluate(expr.object);
        StructValue instance = resolveStruct(object);

        Value newValue = evaluate(expr.value);
        Value valueToStore = newValue.copy();

        Value oldValue = instance.get(expr.name.lexeme);
        valueToStore.retain(heap);
        if (oldValue != null)
            oldValue.release(heap);

        instance.set(expr.name.lexeme, valueToStore);
        return valueToStore;
    }

    @Override
    public Value visitCallExpr(Expr.Call expr) {
        if (expr.callee instanceof Expr.Get) {
            Expr.Get get = (Expr.Get) expr.callee;
            Value object = evaluate(get.object);
            StructValue instance = resolveStruct(object);

            // 1. Try Field (Module function or Closure field)
            Value field = instance.get(get.name.lexeme);
            if (field != null) {
                if (field instanceof FunctionValue || field instanceof NativeFunction) {
                    return callValue(field, expr.arguments);
                }
                // Field exists but not callable? Fallthrough might be confusing, but let's
                // stick to field priority.
                throw new RuntimeException("Property '" + get.name.lexeme + "' is not a function.");
            }

            // 2. Try Method
            return callMethod(object, get.name.lexeme, expr.arguments);
        }

        Value callee = evaluate(expr.callee);
        return callValue(callee, expr.arguments);
    }

    private Value callValue(Value callee, List<Expr> argExprs) {
        // Native Function
        if (callee instanceof NativeFunction) {
            List<Value> args = new ArrayList<>();
            for (Expr argExpr : argExprs) {
                args.add(evaluate(argExpr));
            }
            return ((NativeFunction) callee).call(args, heap);
        }

        // Lambda Function (FunctionValue)
        if (callee instanceof FunctionValue) {
            return callLambda((FunctionValue) callee, argExprs);
        }

        throw new RuntimeException("Can only call functions, methods, or lambdas.");
    }

    private Value callLambda(FunctionValue func, List<Expr> arguments) {
        if (arguments.size() != func.params.size()) {
            throw new RuntimeException("Lambda expects " + func.params.size() + " args.");
        }

        // Create closure environment parented by the lambda's capture closure
        Environment env = new Environment(func.closure);

        for (int i = 0; i < func.params.size(); i++) {
            Value argVal = evaluate(arguments.get(i)).copy();
            argVal.retain(heap);
            env.define(func.params.get(i).lexeme, argVal, false);
        }

        try {
            executeBlock(func.body, env);
        } catch (ReturnException returnValue) {
            return returnValue.value;
        }

        return null;
    }

    private Value callMethod(Value object, String methodName, List<Expr> arguments) {
        String typeName = Environment.inferType(object);

        Map<String, Stmt.Function> typeMethods = methods.get(typeName);
        if (typeMethods == null || !typeMethods.containsKey(methodName)) {
            throw new RuntimeException("Method '" + methodName + "' not defined for type '" + typeName + "'.");
        }

        Stmt.Function method = typeMethods.get(methodName);
        Environment methodEnv = new Environment(environment);
        Value thisValue = object.copy();
        thisValue.retain(heap);
        methodEnv.define("this", thisValue, false);

        if (arguments.size() != method.params.size()) {
            throw new RuntimeException("Method " + methodName + " expects " + method.params.size() + " args.");
        }

        for (int i = 0; i < method.params.size(); i++) {
            Value argVal = evaluate(arguments.get(i)).copy();
            argVal.retain(heap);
            methodEnv.define(method.params.get(i).lexeme, argVal, false);
        }

        try {
            executeBlock(method.body, methodEnv);
        } catch (ReturnException returnValue) {
            return returnValue.value;
        }

        return null;
    }

    @Override
    public Value visitThisExpr(Expr.This expr) {
        return environment.get("this");
    }

    @Override
    public Value visitListExpr(Expr.ListLiteral expr) {
        List<Value> elements = new ArrayList<>();
        for (Expr el : expr.elements) {
            Value val = evaluate(el);
            elements.add(val.copy());
        }
        return new ListValue(elements);
    }

    @Override
    public Value visitLambdaExpr(Expr.Lambda expr) {
        Environment lambdaClosure = new Environment(environment);
        for (Expr captureExpr : expr.captures) {
            Value val = evaluate(captureExpr);
            String name = "";
            if (captureExpr instanceof Expr.Variable) {
                name = ((Expr.Variable) captureExpr).name.lexeme;
            } else if (captureExpr instanceof Expr.AddressOf) {
                name = ((Expr.AddressOf) captureExpr).name.lexeme;
            } else {
                continue;
            }

            Value capturedCopy = val.copy();
            capturedCopy.retain(heap);
            lambdaClosure.define(name, capturedCopy, false);
        }

        return new FunctionValue(expr.params, expr.body, lambdaClosure);
    }

    private StructValue resolveStruct(Value value) {
        if (value instanceof StructValue) {
            return (StructValue) value;
        } else if (value instanceof ClassReference) {
            return heap.dereference(((ClassReference) value).address);
        }
        throw new RuntimeException("Only instances have properties.");
    }

    private boolean isTruthy(Value object) {
        if (object == null)
            return false;
        if (object.getRaw() instanceof Boolean)
            return (boolean) object.getRaw();
        if (object.getRaw() instanceof Integer)
            return (int) object.getRaw() != 0;
        return true;
    }

    private boolean isEqual(Value a, Value b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;
        if (a.equals(b))
            return true; // Forward to Value.equals() which PointerValue implements
        return a.getRaw().equals(b.getRaw());
    }

    private void checkNumberOperands(Token operator, Value left, Value right) {
        if (left.getRaw() instanceof Integer && right.getRaw() instanceof Integer)
            return;
        throw new RuntimeException(operator.type + " operands must be numbers.");
    }

    private String stringify(Value value) {
        if (value == null)
            return "nil";

        // Handle special value types with custom formatting
        if (value instanceof AtomValue) {
            return "@" + ((AtomValue) value).name;
        }

        if (value instanceof TupleValue) {
            List<Value> elements = ((TupleValue) value).elements;
            return "{" + elements.stream().map(this::stringify).collect(Collectors.joining(", ")) + "}";
        }

        if (value instanceof MapValue) {
            Map<Value, Value> entries = ((MapValue) value).entries;
            return "#{" + entries.entrySet().stream()
                    .map(e -> stringify(e.getKey()) + " => " + stringify(e.getValue()))
                    .collect(Collectors.joining(", ")) + "}";
        }

        // Handle ClassReference
        if (value instanceof ClassReference) {
            ClassReference ref = (ClassReference) value;
            try {
                StructValue actual = heap.dereference(ref.address);
                return "ref<" + ref.address + ":" + ref.typeName + ">";
            } catch (Exception e) {
                return "ref<" + ref.address + "> (Inaccessible)";
            }
        }

        // Handle strings with escape sequences
        if (value instanceof PrimitiveValue && value.getRaw() instanceof String) {
            return ((String) value.getRaw()).replace("\\n", "\n").replace("\\t", "\t");
        }

        // Default
        Object raw = value.getRaw();
        if (raw == null)
            return "nil";
        return raw.toString();
    }
}
