package LLVMIR.Value.Instruction;

import LLVMIR.Type.IntegerType;
import LLVMIR.Type.Type;
import LLVMIR.Value.BasicBlock;
import LLVMIR.Value.Value;

public class BinaryInst extends Instruction{
    public BinaryInst(OP op, Value left, Value right, Type type){
        super("%" + (++Value.valNumber), type, op);
        this.addOperand(left);
        this.addOperand(right);
        this.setHasname(true);
    }

    public Value getLeftVal(){
        return getOperand(0);
    }

    public Value getRightVal(){
        return getOperand(1);
    }



    @Override
    public String getInstString(){
        return getName() + " = " +
                getLeftVal().getName() + " " + getOp() + " " +
                getRightVal().getName();
    }
}
