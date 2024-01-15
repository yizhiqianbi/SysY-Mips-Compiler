package LLVMIR.Value.Instruction;

import LLVMIR.Type.PointerType;
import LLVMIR.Type.Type;
import LLVMIR.Value.Value;

public class LoadInst extends Instruction{
    public LoadInst(Value pointer,Type type){
        super("%"+(++Value.valNumber),type,OP.Load);
        this.addOperand(pointer);
        this.setHasname(true);
    }

    public Value getPointer(){
        return getOperand(0);
    }
    @Override
    public String getInstString(){
        Value pointer = getPointer();

        try{
            Type type = ((PointerType) pointer.getType()).getElementType();
            return getName() + " = " + "load " + type + ", "
                    + pointer.getType() + " " + pointer.getName();
        }
        catch (Exception e )
        {
            return getName() + " = " + "load " + ", "
                    + pointer.getType() + " " + pointer.getName();
        }
    }
}
