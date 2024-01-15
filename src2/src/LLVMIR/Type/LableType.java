package LLVMIR.Type;

public class LableType extends Type{

    // !!!! 不太清楚LableType干什么用的
    // !虽管中窥豹，但终将见全貌
    private final int handler;
    private static int HANDLER = 0;

    public LableType(int handler) {
        this.handler = HANDLER++;
    }


    @Override
    public boolean isLableType() {
        return true;
    }

    @Override
    public String toString() {
        return "label_" + handler;
    }
}
