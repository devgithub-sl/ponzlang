package com.fplus.runtime;

import java.util.List;
import com.fplus.ast.Stmt;
import com.fplus.Token;

public class FunctionValue implements Value {
    public final List<Token> params;
    public final List<Stmt> body;
    public final Environment closure;

    public FunctionValue(List<Token> params, List<Stmt> body, Environment closure) {
        this.params = params;
        this.body = body;
        this.closure = closure;
    }

    @Override
    public Object getRaw() {
        return this;
    }

    @Override
    public Value copy() {
        // Functions are reference types (closures), but in FPlus context do we copy
        // them?
        // Closure environment is shared reference.
        // We can just return 'this' effectively sharing the underlying closure.
        // Or create new FunctionValue wrapping same closure.
        return new FunctionValue(params, body, closure);
    }

    @Override
    public void retain(Heap heap) {
        // Closure captures might need retaining?
        // But closure is Env, Env holds Values.
        // When FunctionValue is retained, it keeps the Closure alive.
        // We don't have RefCount on Environment itself (it's Java GC managed).
        // But the VALUES inside the closure must be retained.

        // This is tricky.
        // Lambda [x] captures 'x' by COPY (as per plan).
        // So the 'closure' Env contains COPIES of captured vars.
        // Those copies are "owned" by the FunctionValue.
        // So if we retain FunctionValue, we ensure we don't release its owned captures?
        // Actually, the closure owns them.
        // Since we are in Java, if FunctionValue is alive, Closure is alive.

        // We just need to make sure we don't accidentally release the captured values
        // until the FunctionValue is destroyed (GC'd).
        // Since we don't have destructors on FunctionValue (Java),
        // and our Heap is manual-ish ARC...

        // If the captured value is a ClassReference, we have incremented the RefCount
        // on capture.
        // When FunctionValue dies (Java GC), we should technically decrement.
        // But we can't hook into Java GC easily.

        // Simplification for Prototype:
        // We will LEAK captured references if we don't have specific "destroy" hook.
        // BUT, since we "copy" values into closure, they are distinct references.
        // If we "retain" them on capture, their count goes up.
        // We never call "release" on the closure values?
        // That's a leak.

        // Correct approach for managed runtime in Java:
        // Rely on Java GC for the `FunctionValue` object itself.
        // But for the `ClassReference`s inside it, we need manual management?
        // Ideally, `FunctionValue` would implement `finalize()` or similar, but that's
        // bad practice.

        // Alternative: FPlus ARC only manages "Let" bindings and "Fields".
        // FunctionValue is a value.
        // When `let f = lambda` -> `f` retains `FunctionValue`.
        // `FunctionValue.retain` -> could increment a counter on itself?
        // If `FunctionValue` had a `refCount`.
        // But `FunctionValue` is not in our `Heap`. It's a `Value` on Java Heap.

        // Let's assume for this prototype: Captures are COPIED.
        // If they are primitives/structs, no issue.
        // If they are references, we retain them.
        // We accept that we might leak the captured references because we don't have a
        // destructor for FunctionValue.
        // (Unless we add FunctionValue to our Heap, but that changes the model
        // significantly).
    }

    @Override
    public void release(Heap heap) {
        // If we could determine when this FunctionValue is 0-ref...
    }

    @Override
    public String toString() {
        return "<fn>";
    }
}
