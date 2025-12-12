# FPlus Programming Language

FPlus is a modern, interpreted programming language running on the JVM. It combines Python-like syntax with Rust-inspired memory management (ARC), C++ style power features, and Erlang-inspired data types.

## Features

### 1. Memory Safety
- **Automatic Reference Counting (ARC)**: No garbage collector pauses. Memory is deterministic.
- **Value Semantics**: Variables (lists, structs) are copied on assignment by default, ensuring safety.
- **Reference Types**: `class` types are reference-counted. `struct` types are values.
- **Pointers**: C-style pointer support with `*` (address-of) and `&` (dereference) operators.

### 2. Type Safety
- **Inferred Static Typing**: Variable types are locked at initialization.
- **Runtime Enforced**: Assignments are checked against the variable's type.

### 3. Object Oriented
- **Classes & Structs**: Define data structures.
- **Methods**: Define methods using `impl` blocks.
- **Member Access**: Dot syntax (`obj.field`).

### 4. Erlang-Inspired Data Types
- **Atoms**: Immutable symbolic constants `@ok`, `@error`.
- **Tuples**: Fixed-size collections `{1, 2, 3}`.
- **Maps**: Key-value associations `#{@name => "Alice", @age => 30}`.

### 5. Functional & Power Features
- **Lambdas**: C++ style anonymous functions with explicit capture lists `[captures](args): body`.
- **Lists**: Native list support `[1, 2, 3]`.
- **Loops**: `while` loops.
- **Multi-Threading**: Native `spawn` command to run lambdas in separate threads.
- **Module System**: Import and organize code with `import "file.fps" as Module`.

## Usage

### Prerequisites
- Java JDK 8+

### Compilation
```bash
javac -d bin src/main/java/com/fplus/*.java src/main/java/com/fplus/ast/*.java src/main/java/com/fplus/runtime/*.java
```

### Running Scripts
```bash
java -cp bin com.fplus.Main <script_file.fps>
```

## Syntax Guide

### Variables & Types
```fplus
let x = 10          // Immutable
let mutable y = 20  // Mutable
y = 30

type Point = struct { x: int, y: int }
type Box = class { val: int }  // Reference type
```

### Control Flow
```fplus
if x > 5:
    print "Big"
else:
    print "Small"

while x > 0:
    x = x - 1
```

### Methods
```fplus
impl Point:
    fun dist():
        return this.x + this.y // Simplified
```

### Erlang Data Types
```fplus
// Atoms - symbolic constants
let status = @ok
let result = @error

// Tuples - fixed collections
let point = {10, 20}
let person = {"Alice", 25, @active}

// Maps - key-value pairs
let user = #{@name => "Bob", @age => 30, @role => @admin}
let config = #{1 => @enabled, 2 => @disabled}

// Nested structures
let response = {@ok, {200, "Success"}, #{@data => [1, 2, 3]}}
```

### Pointers
```fplus
let mutable x = 42
let ptr = *x        // Get address
print &ptr          // Dereference (prints 42)
&ptr = 100          // Modify through pointer
print x             // prints 100
```

### Module System
```fplus
// math_lib.fps
fun add(a, b):
    return a + b

// main.fps
import "math_lib.fps" as Math
print Math.add(5, 3)  // prints 8
```

### Lambdas & Concurrency
```fplus
let factor = 2
let doubler = [factor](n):
    return n * factor

print doubler(5)  // prints 10

// Multi-threading
spawn(doubler)
```

## Examples

See the following demo files:
- `function_demo.fps` - User-defined functions
- `pointer_demo.fps` - Pointer operations
- `module_demo.fps` - Module system usage
- `erlang_types_demo.fps` - Erlang data types
- `arc_demo.fps` - Automatic reference counting
- `lambda_demo.fps` - Lambda expressions and closures
