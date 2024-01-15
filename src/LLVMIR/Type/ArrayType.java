package LLVMIR.Type;

import java.util.ArrayList;

public class ArrayType extends PointerType{
    //纯纯的数组
    int eleDim;
    int totDim = 1;
    ArrayList<Integer> dimList = new ArrayList<>();

    public ArrayType(Type eleType, int eleDim) {
        super(eleType);
        this.eleDim = eleDim;
        Type iter = this;
        while (iter instanceof ArrayType){
            totDim *= ((ArrayType) iter).getEleDim();
            dimList.add(((ArrayType) iter).eleDim);
            iter = ((ArrayType) iter).getElementType();
        }
    }

    @Override
    public boolean isArrayType(){
        return true;
    }

    public int getEleDim() {
        return eleDim;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        if(!ElementType.isArrayType()){
            stringBuilder.append("[");
            stringBuilder.append(eleDim);
            stringBuilder.append(" x i32]");
        }
        else{
            stringBuilder.append("[");
            stringBuilder.append(eleDim);
            stringBuilder.append(" x ");
            stringBuilder.append(ElementType.toString());
            stringBuilder.append("]");
        }
        //  这里的StringBuilder中有eleType产生的*号，需要我们处理
        String rawString = stringBuilder.toString();
        String string =  rawString.replace("*", "");
        return string + "*";
    }

    private int getArrSize(){
        int res = 1;
        PointerType it = this;
        while (it instanceof ArrayType){
            res *= ((ArrayType) it).getEleDim();
            it = (PointerType) it.getElementType();
        }
        return res * 4;
    }

    public int calOffset(ArrayList<Integer> indexs){
        int ans = 0;
        ArrayList<Integer> gapDims = new ArrayList<>();
        ArrayType arrType = this;
        int size = getArrSize();
        while (true){
            gapDims.add(size);
            size /= arrType.getEleDim();
            Type tmpType = arrType.getElementType();
            if(!(tmpType instanceof ArrayType)) break;
            else arrType = (ArrayType) arrType.getElementType();
        }
        gapDims.add(4);

        for(int i = 0; i < indexs.size(); i++){
            ans += indexs.get(i) * gapDims.get(i);
        }

        return ans;
    }

    public ArrayList<Integer> getDimList(){
        return dimList;
    }

    public int getTotDim(){
        return totDim;
    }
}
