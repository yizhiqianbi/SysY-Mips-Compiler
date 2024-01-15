package LLVMIR.Type;

import java.util.ArrayList;

public class ArrayType extends Type{
    private int numOfElement;
    private Type elementType;
    public ArrayType(int numOfElement, Type elementType){
        this.numOfElement = numOfElement;
        this.elementType  = elementType;
    }

    @Override
    public boolean isArrayType() {
        return true;
    }

    // 如果不是arraytype，则是1
    public int getDimension() {
        if(!elementType.isArrayType()) return 1;
        return ((ArrayType) elementType).getDimension()+1;
    }

    // ! 这里跟文法似乎有紧密关联数组有多维
    public ArrayList<Integer> getNumList(){
        ArrayList<Integer> numList = new ArrayList<>();
        numList.add(numOfElement);
        if(elementType.isArrayType()){
            numList.addAll(((ArrayType) elementType).getNumList());
        }
        return numList;
    }

    public boolean isIntegerArr(){
        return elementType.isArrayType();
    }

    public Type getElementType() {
        return elementType;
    }

    public int getNumOfElement() {
        return numOfElement;
    }
}
