package LLVMIR.Value.Instruction;

import LLVMIR.Type.Type;

public class Move extends Instruction{
    public Move(String name, Type type, OP op) {
        super(name, type, op);
    }
}
