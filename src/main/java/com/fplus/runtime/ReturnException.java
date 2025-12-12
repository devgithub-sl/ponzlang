package com.fplus.runtime;

/**
 * Exception used to implement non-local return from functions.
 * <p>
 * When a {@code return} statement is executed, this exception is thrown to
 * unwind
 * the call stack back to the function call site. The exception carries the
 * return
 * value and is caught by the interpreter's function call handler.
 * <p>
 * This is a "fast exception" - it disables stack trace generation for
 * performance
 * since it's used for normal control flow rather than error handling.
 * 
 * @see FunctionValue
 * @see com.fplus.Interpreter#visitReturnStmt
 */
public class ReturnException extends RuntimeException {
    public final Value value;

    public ReturnException(Value value) {
        super(null, null, false, false); // Fast exception (no stack trace)
        this.value = value;
    }
}
