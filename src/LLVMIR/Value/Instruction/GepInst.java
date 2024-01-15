package LLVMIR.Value.Instruction;

import LLVMIR.Type.Type;
import LLVMIR.Value.BasicBlock;
import LLVMIR.Value.Value;

import java.util.ArrayList;

public class GepInst extends Instruction{
    //!这事数组的
    public GepInst(ArrayList<Value> indexs, Value target, Type type, BasicBlock basicBlock) {
        super("%" + (++Value.valNumber), type, OP.GEP);
        this.addOperand(target);
        for(Value index : indexs){
            this.addOperand(index);
        }
        this.setHasname(true);
    }

    public Value getTarget() {
        return getOperand(0);
    }

    public ArrayList<Value> getIndexs() {
        ArrayList<Value> indexs = new ArrayList<>();
        for(int i = 1; i < getOperands().size(); i++){
            indexs.add(getOperand(i));
        }
        return indexs;
    }
    public Value getPointer() {
        return getOperands().get(0);
    }

}
