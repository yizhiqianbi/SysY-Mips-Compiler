package LLVMIR.Value;

import LLVMIR.Type.Type;
import LLVMIR.Use;

import java.util.ArrayList;

public class User extends Value{
    protected ArrayList<Value> operands=new ArrayList<>();
    public User(String name, Type type){
        super(name, type);
    }

    //!------------ Operand--------------------
    public void addOperand(Value operand){
        if(operands!=null){operands.add(operand);}
    }

    public ArrayList<Value> getOperands() {
        return operands;
    }


    public void setOperands(ArrayList<Value> operands) {
        this.operands = operands;
    }
    public Value getOperand(int index){
        if(index>=operands.size()) return null;
        else return operands.get(index);
    }
    public void setOperand(int index, Value value){
        if(index>=operands.size())return;
        this.operands.set(index,value);
        updateUse(value);
    }

    public void resetOperand(Value oldValue,Value newValue){
        for(Value value : operands){
            if(value.equals(oldValue)){
                // ??? index是从0开始吧
                operands.set(operands.indexOf(value),newValue);
                break;
            }
        }
    }

    public void resetOperand(int index,Value newValue){
        if(index>=operands.size())return;
        operands.set(index,newValue);
    }

    public void removeUseFromOperands(){
        if(operands!=null){
            for(Value operand: operands){
                operand.removeUseByUser(this);
            }
        }
    }


    public void removeOperand(Value operand){
        if(operands.contains(operand)) operands.remove(operand);
    }

    //!------------ Operand--------------------


    //!------------User Operate--------------------

    private void updateUse(Value operand) {
        if (operand != null) {
            operand.addUse(new Use(operand, this));
        }
    }
    public void removeOperand(int index) {
        Value operand = operands.get(index);
        operands.remove(index);
        if (operand != null) {
            operand.removeOneUseByUser(this);
        }
    }

    public void replaceOperand(int index,Value value){
        Value operand = operands.get(index);
        setOperand(index, value);
        if (operand != null) {
            operand.removeOneUseByUser(this);
        }
    }

    public void replaceOperand(Value oldValue,Value newValue){
        for (int i = 0; i < operands.size(); i++) {
            if (operands.get(i) == oldValue) {
                replaceOperand(i, newValue);
            }
        }
    }






}
