package LLVMIR.Type;

public abstract class Type {

    public boolean isIntegerType() {return false;}
    public boolean isFunctionType(){return false;}
    public boolean isVoidType(){return false;}
    public boolean isArrayType(){return false;}
    public boolean isPointerType(){return false;}

    public boolean isLableType() {return false;}

}
