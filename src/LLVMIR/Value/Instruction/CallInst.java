package LLVMIR.Value.Instruction;

import LLVMIR.Type.Type;
import LLVMIR.Value.Function;
import LLVMIR.Value.Value;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class CallInst extends Instruction{
    private boolean isVoid ;
    private Function function;
    public CallInst(Function function, ArrayList<Value> values) {
        super("%" + (++Value.valNumber), function.getType(),OP.Call);
        this.function =function;
        this.isVoid = !function.getType().isVoidType();

        if(function.getType().isIntegerType()){
            this.setName("%" + (++Value.valNumber));
            this.setHasname(true);
        }
        this.addOperand(function);
        for (Value value : values) {
            this.addOperand(value);
        }
    }
    public CallInst(Function function) {
        super("%" + (++Value.valNumber), function.getType(),OP.Call);
        this.function =function;
        this.isVoid = !function.getType().isVoidType();

        if(function.getType().isIntegerType()){
            this.setName("%" + (++Value.valNumber));
            this.setHasname(true);
//            this.setHasName(true);
        }
        this.addOperand(function);
    }

    public Function getFunction() {
        return function;
    }

    public ArrayList<Value> getParams(){
        return getUseValues();
    }


    @Override
    public boolean hasName() {
        return isVoid;
    }

    public boolean isVoid(){
        return isVoid;
    }


    public Function getCallFunc(){
        return (Function) getOperand(0);
    }


    public ArrayList<Value> getValues(){
        ArrayList<Value> values = new ArrayList<>();
        for(int i = 1; i < getOperands().size(); i++){
            values.add(getOperand(i));
        }
        return values;
    }

    @Override
    public String getInstString() {
        StringBuilder sb = new StringBuilder();
        if(!this.getType().isVoidType()){
            sb.append(getName()).append(" = ");
        }
        sb.append("call ").append(getFunction().getType()).append(' ').append(getFunction().getName()).append("(");
        ArrayList<Value> operands = getOperands();
        for(int i = 1; i < operands.size(); i++){
            Value value = operands.get(i);
            sb.append(value.getType()).append(" ").append(value.getName());
            if(i != operands.size() - 1){
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
