package LLVMIR.Value.Instruction;

import LLVMIR.Type.Type;
import LLVMIR.Value.Value;

public class ConversionInst extends Instruction{
    public ConversionInst(Value value, Type type, OP op) {
        super("%" + (++Value.valNumber), type, op);
        addOperand(value);
        this.setHasname(true);
    }

    public Value getValue(){
        return getOperand(0);
    }

    @Override
    public String getInstString(){
        String to = null;
        if(getOp() == OP.Ftoi){
            to = "to i32";
        }
        else if(getOp() == OP.Zext){
            to = "to i32";
        }
        else if(getOp() == OP.BitCast){
            to = "to i32*";
        }
        return getName() + " = " + getOp() + " " +
                getValue() + " " + to;
    }
}
