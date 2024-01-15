package LLVMIR.Value;

import LLVMIR.Type.IntegerType;
import LLVMIR.Type.Type;

public class ConstInteger extends Const{
    private final int val;

    public static ConstInteger constZero = new ConstInteger(0);

    public ConstInteger(int val){
        super(String.valueOf(val), new IntegerType(32));
        this.val = val;
    }

    public int getVal() {
        return val;
    }
}
