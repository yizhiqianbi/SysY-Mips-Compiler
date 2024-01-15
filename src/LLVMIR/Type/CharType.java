package LLVMIR.Type;

public class CharType extends Type {
    //字符没有用过
    char ch;
    public CharType(char ch){
        this.ch = ch;
    }

    public char getCh() {
        return ch;
    }

    public void setCh(char ch) {
        this.ch = ch;
    }

    @Override
    public boolean isCharType() {
        return true;
    }
}
