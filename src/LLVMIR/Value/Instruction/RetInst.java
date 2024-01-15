package LLVMIR.Value.Instruction;

import LLVMIR.Type.Type;
import LLVMIR.Type.VoidType;
import LLVMIR.Value.ConstFloat;
import LLVMIR.Value.ConstInteger;
import LLVMIR.Value.Value;

public class RetInst extends Instruction{
    private boolean isVoid;

    public RetInst(String name, Type type, OP op) {
        super(name, type, op);
    }

    public RetInst(Value value){
        super("", VoidType.voidType,OP.Ret);
        this.addOperand(value);
        this.isVoid = false;
    }
    public RetInst(){
        super("", VoidType.voidType,OP.Ret);
        this.isVoid = true;
    }

    public boolean isVoid(){
        return getOperand(0).getName().equals("void");
    }

    public Value getValue(){
        return this.getOperand(0);
    }

    @Override
    public String getInstString(){
        StringBuilder sb = new StringBuilder();
        sb.append("ret ");

        Value value = getValue();
        if(value.getType().isIntegerType()) sb.append("i32 ");

        if(value instanceof ConstInteger constInt) {
            sb.append(constInt.getVal());
        }
        else sb.append(value.getName());
        return sb.toString();
    }


    @Override
    public boolean hasName() {
        return false;
    }



}
