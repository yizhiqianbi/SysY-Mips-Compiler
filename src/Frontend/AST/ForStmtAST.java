package Frontend.AST;

import Frontend.AST.ExpAST.ExpAST;
import Frontend.AST.ExpAST.LValAST;

public class ForStmtAST extends IASTNode{
    private LValAST lValAST;

    private ExpAST expAST;


    public ForStmtAST(LValAST   lValAST, ExpAST expAST) {
        this.lValAST = lValAST;
        this.expAST = expAST;
    }
    public LValAST getlValAST() {
        return lValAST;
    }

    public ExpAST getExpAST() {
        return expAST;
    }
}
