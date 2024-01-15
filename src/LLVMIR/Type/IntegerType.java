package LLVMIR.Type;

public class IntegerType extends Type{
    private final int bitNum;
    public IntegerType(int bitNum){
        this.bitNum = bitNum;
    }

    public static final IntegerType I32 = new IntegerType(32);
    public static final IntegerType I1 = new IntegerType(1);

    public int getBitNum() {
        return bitNum;
    }

    @Override
    public boolean isIntegerType() {
        return true;
    }

    @Override
    public String toString() {
        return "i"+bitNum;
    }
}
