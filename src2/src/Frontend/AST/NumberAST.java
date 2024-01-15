package Frontend.AST;

import Frontend.AST.DeclAST.InitValAST;

public class NumberAST extends IASTNode{
    private int intConst;

    public NumberAST(int intConst) {
        this.intConst = intConst;
    }

    public int getIntConst() {
        return intConst;
    }
}
