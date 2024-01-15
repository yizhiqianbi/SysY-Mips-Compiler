package LLVMIR.Type;

import java.util.ArrayList;
import java.util.List;

public class FunctionType extends Type{

    private ArrayList<Type> parameterTypes;
    private Type returnType;
    @Override
    public boolean isFunctionType() {
        return true;
    }

    public FunctionType(Type returnType){
        this.returnType = returnType;
        this.parameterTypes = new ArrayList<Type>();
        arrayTypeNoLength();
    }

    public FunctionType(Type returnType,ArrayList<Type> parameterTypes){
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        arrayTypeNoLength();
    }


    public ArrayList<Type> getParameterTypes() {
        return parameterTypes;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public void setParameterTypes(ArrayList<Type> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    //!!!! 不太明白捏
    private void arrayTypeNoLength(){
        ArrayList<Integer> target = new ArrayList<>();
        for (Type type : parameterTypes) {
            if (type instanceof ArrayType) {
//                if (((ArrayType) type).getNumOfElement()) {
//                    target.add(parametersType.indexOf(type));
//                }
            }
        }
//        for (int index : target) {
//            parametersType.set(index, new ir.types.PointerType(((ArrayType) parametersType.get(index)).getElementType()));
//        }
    }


}
