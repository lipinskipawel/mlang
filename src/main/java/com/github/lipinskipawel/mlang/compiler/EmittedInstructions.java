package com.github.lipinskipawel.mlang.compiler;

import com.github.lipinskipawel.mlang.code.OpCode;

public record EmittedInstructions(OpCode opCode, int position) {

    public EmittedInstructions withOpCode(OpCode opCode) {
        return new EmittedInstructions(opCode, position);
    }
}
