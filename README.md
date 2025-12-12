# FPlus Programming Language

FPlus is a modern, interpreted programming language running on the JVM. It combines Python-like syntax with Rust-inspired memory management (ARC) and C++ style power features.

## Features

### 1. Memory Safety
- **Automatic Reference Counting (ARC)**: No garbage collector pauses. Memory is deterministic.
- **Value Semantics**: Variables (lists, structs) are copied on assignment by default, ensuring safety.
- **Reference Types**: `class` types are reference-counted. `struct` types are values.

### 2. Type Safety
- **Inferred Static Typing**: Variable types are locked at initialization.
- **Runtime Enforced**: Assignments are checked against the variable's type.

### 3. Object Oriented
- **Classes & Structs**: Define data structures.
- **Methods**: Define methods using `impl` blocks.
- **Member Access**: Dot syntax (`obj.field`).

### 4. Functional & Power Features
- **Lambdas**: C++ style anonymous functions with explicit capture lists `[captures](args): body`.
- **Lists**: Native list support `[1, 2, 3]`.
- **Loops**: `while` loops.
- **Multi-Threading**: Native `spawn` command to run lambdas in separate threads.

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

### Lambdas & Concurrency
```fplus
let factor = 2
let doubler = [factor](n):
    return n * factor

spawn(doubler)
```
