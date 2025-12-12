package com.fplus.runtime;

public class ReturnException extends RuntimeException {
    public final Value value;

    public ReturnException(Value value) {
        super(null, null, false, false); // Fast exception (no stack trace)
        this.value = value;
    }
}
