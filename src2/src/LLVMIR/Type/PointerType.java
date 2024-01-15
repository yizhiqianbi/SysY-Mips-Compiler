package LLVMIR.Type;

public class PointerType extends Type{
    Type ElementType;
    public PointerType(Type ElementType) {
        this.ElementType = ElementType;
    }

    @Override
    public boolean isPointerType() {
        return true;
    }

    public Type getElementType() {
        return ElementType;
    }

    @Override
    public String toString() {
        return ElementType.toString()+'*';
    }
}
