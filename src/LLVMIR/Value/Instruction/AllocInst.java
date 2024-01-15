package LLVMIR.Value.Instruction;

import LLVMIR.Type.PointerType;
import LLVMIR.Type.Type;
import LLVMIR.Value.BasicBlock;
import LLVMIR.Value.Value;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AllocInst extends Instruction{

    ArrayList<Value> initValues;
    Value initValue;
    Type allocType;
    private boolean isConst;
    public AllocInst(Type type){
        super("%"+ (++Value.valNumber),type,OP.Alloca);
        this.setHasname(true);
    }

    public AllocInst(String name, Type type, BasicBlock basicBlock, boolean isConst){
        super(name, type, OP.Alloca);
        this.isConst = isConst;
        PointerType pointerType = (PointerType) type;
        allocType = pointerType.getElementType();
        this.setHasname(true);
    }


    public void setInitValue(Value initValue) {
        this.initValue = initValue;
    }
    public Value getInitValue() {
        return initValue;
    }

    public void setInitValues(ArrayList<Value> initValues) {
        this.initValues = initValues;
    }

    public ArrayList<Value> getInitValues() {
        return initValues;
    }

    public String getInstString(){
        Type EleType = ((PointerType) getType());
        return getName() + " = " + "alloca " + EleType.toString();
    }

    public Type getAllocType() {
        return allocType;
    }


    public boolean isConst() {
        return isConst;
    }
}
