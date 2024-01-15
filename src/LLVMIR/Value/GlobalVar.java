package LLVMIR.Value;

import LLVMIR.Type.Type;

import java.util.ArrayList;

public class GlobalVar extends Value{
    private boolean isConst;
    private boolean isConstDecl;
    private boolean isArray;
    private Value value;
    //  代表全局数组的初始值
    private ArrayList<Value> values;

    public GlobalVar(String name, Type type, boolean isConst, Value value){
        super(name, type);
        this.isConst = isConst;
        //  这个Value是他的初始值
        this.value = value;
        this.isArray = false;
    }

    public GlobalVar(String name, Type type, boolean isConst, ArrayList<Value> values){
        super(name, type);
        this.isConst = isConst;
        //  这个Value是他的初始值
        this.values = values;
        this.isArray = true;
    }



    public boolean isConst() {
        return isConst;
    }



    public Value getValue() {
        return value;
    }

    public ArrayList<Value> getValues() {
        return values;
    }


    public boolean isConstDecl() {
        return isConstDecl;
    }
    public void setIsConstDecl(boolean isConstDecl) {
        this.isConstDecl = isConstDecl;
    }
}
