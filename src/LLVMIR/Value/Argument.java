package LLVMIR.Value;

import LLVMIR.Type.Type;

public class Argument extends Value{
    private final Function parentFunc;
    public Argument(String name, Type type,Function parentFunc){
        super(name,type);
        this.parentFunc = parentFunc;
    }

    @Override
    public String toString(){
        return getType() + " " + getName();
    }

}
