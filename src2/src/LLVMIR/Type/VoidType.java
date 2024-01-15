package LLVMIR.Type;
public class VoidType extends Type {

    public static final VoidType voidType = new VoidType();

    @Override
    public boolean isVoidType() {
        return true;
    }

    @Override
    public String toString() {
        return "void";
    }
}
