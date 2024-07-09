# mlang

Based on the book "[Writing An Interpreter In Go](https://interpreterbook.com)" and "[Writing A Compiler In Go](https://compilerbook.com/)"
by Thorsten Ball.

## Writing An Interpreter In Go

### Chapter 1

Write a lexer that turns 'random' characters into tokens. Transformation from text to tokens is called lexing.
Often here is the place to add line number and column to token information so later any compilation errors can be
enriched by those information.

The simplest lexer and iterate through text character by character to turn it into token.

### Chapter 2

Write a parser that turns tokens into abstract syntax tree. Abstract syntax tree is just a tree with different types of
nodes (mlang has expressions and statements). The implementation is similar to lexer. It just iterates though tokens to
build node. Expressions can build other expressions so this is why we need a tree structure. The algorithm works using
recursion. We also need functions that will handle prefix and infix expressions. Precedence is done by more nested
structure of the tree.

Operand -> << expression >>

Operator -> '=', '!'


prefix expression -> operator operand

infix expression -> operand operator operand

### Chapter 3

Evaluator just like previous forms of interpreter walks through previous form. This time it uses AST to turn it into
object system. Object system is needed to keep track of values. Interpreter lacks garbage collector, so the host must do
that.

We keep all the identifiers in the Environment so they are accessible thought out the script.

Host programming language - is the language that the interpreter is written in.
For this interpreter the host programming language is Java.

Booleans in this interpreter are compared using pointers. There is no sense of creating new instance of boolean since we
know there are only 2 valid instance of boolean.

### Chapter 4

Just like any other scripting language we can add builtin functions. Those are not part of standard library nor part of
a language specification (like syntax, memory model and so on). Those builtin functions serves a gap between interpreter
and system calls. Without those (and without std) we couldn't ever even print something to standard output.

Apart for booleans the interpreter also uses pointer comparison in map for keys. We use pointers because using lookups
(searching for value in keys) will defeat purpose of map turing O(1) into O(n).

## Writing A Compiler In Go

### General notes

Compiler on a high level overview is just a program that is capable of converting AST into bytecode. The example
language bytecode was series of instructions with constants. Symbol table is a construct that holds information about
identifiers in the program. Identifier here can mean functions, constants, procedure and also plain identifier.

Virtual machine is just a simplified machine with limited set of OpCode's that it understands. Just like a normal
machine it follows fetch-decode-execute cycle. Stack based virtual machine has been implemented.
Instruction pointer (also known as program counter) is just a pointer to
the address of the current instruction to fetch-decode-execute. When the instruction (opcode) is to load constant (in
our case the OP_CONSTANT has width of 2 bytes) then we the VM is going to update ip by 2 to point to the next
instruction.
Stack pointer is just a pointer to the head of the stack. On the stack all the values are living. Function calls are
living on the stack too. So to have a logical order, the frame must be implemented. Frame is just a compiled function
with instruction pointer and base pointer. Base pointer is where we "left" the stack before entering new frame.
