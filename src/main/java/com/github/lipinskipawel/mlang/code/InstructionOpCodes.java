package com.github.lipinskipawel.mlang.code;

public interface InstructionOpCodes {

    int OP_CONSTANT = 1;
    int OP_ADD = 2;
    int OP_POP = 3;
    int OP_SUB = 4;
    int OP_MUL = 5;
    int OP_DIV = 6;
    int OP_TRUE = 7;
    int OP_FALSE = 8;
    int OP_EQUAL = 9;
    int OP_NOT_EQUAL = 10;
    int OP_GREATER_THAN = 11;
    int OP_MINUS = 12;
    int OP_BANG = 13;
    int OP_JUMP_NOT_TRUTHY = 14;
    int OP_JUMP = 15;
    int OP_NULL = 16;
    int OP_GET_GLOBAL = 17;
    int OP_SET_GLOBAL = 18;
    int OP_ARRAY = 19;
    int OP_HASH = 20;
    int OP_INDEX = 21;
    int OP_CALL = 22;
    int OP_RETURN_VALUE = 23;
    int OP_RETURN = 24;
    int OP_GET_LOCAL = 25;
    int OP_SET_LOCAL = 26;
    int OP_GET_BUILTIN = 27;
    int OP_CLOSURE = 28;
    int OP_GET_FREE = 29;
    int OP_CURRENT_CLOSURE = 30;
}
