package LLVMIR.Value.Instruction;

import LLVMIR.Type.Type;
import LLVMIR.Type.VoidType;
import LLVMIR.Value.Value;

public class StoreInst extends Instruction{
    private boolean isInitArrayInst = false;
    public StoreInst(Value value,Value pointer) {
        super("", VoidType.voidType, OP.Store);
        this.addOperand(value);
        this.addOperand(pointer);
    }

    public Value getValue(){
        return getOperand(0);
    }

    public Value getPointer(){
        return getOperand(1);
    }


    @Override
    public String getInstString() {
        return "store " + getValue() + ", " + getPointer();
    }
}
