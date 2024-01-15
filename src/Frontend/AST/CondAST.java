package Frontend.AST;

import Frontend.AST.ExpAST.LOrExpAST;

public class CondAST extends IASTNode{

    private LOrExpAST lOrExpAST;

    public CondAST(LOrExpAST lOrExpAST) {
        this.lOrExpAST = lOrExpAST;
    }

    public LOrExpAST getlOrExpAST() {
        return lOrExpAST;
    }
}
