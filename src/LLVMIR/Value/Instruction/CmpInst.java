package LLVMIR.Value.Instruction;

import LLVMIR.Type.IntegerType;
import LLVMIR.Value.BasicBlock;
import LLVMIR.Value.ConstInteger;
import LLVMIR.Value.Value;

public class CmpInst extends Instruction{
    public CmpInst(Value left, Value right, OP op) {
        super("%" + (++Value.valNumber), new IntegerType(1), op);
        this.addOperand(left);
        this.addOperand(right);
        this.setHasname( true);
    }

    public Value getLeftVal(){
        return getOperand(0);
    }

    public Value getRightVal(){
        return getOperand(1);
    }

    public boolean isBothConst(){
        Value left = getOperand(0);
        Value right = getOperand(1);
        return left instanceof ConstInteger && right instanceof ConstInteger;
    }

    @Override
    public String getInstString(){
        return getName() + " = " +
                getLeftVal().getName() + " " + getOp() + " " +
                getRightVal().getName();
    }
}
