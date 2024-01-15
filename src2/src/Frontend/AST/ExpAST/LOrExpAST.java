package Frontend.AST.ExpAST;

import Frontend.AST.IASTNode;
import Frontend.Token;

public class LOrExpAST extends IASTNode {

    private LAndExpAST lAndExpAST;
    private LOrExpAST lOrExpAST;
    private String op;
    private int state;

    public LOrExpAST(LAndExpAST lAndExpAST){
        this.lAndExpAST = lAndExpAST;
        this.state =1;
    }

    public LOrExpAST( LAndExpAST lAndExpAST,String op , LOrExpAST lOrExpAST){
        this.op = op;
        this.lAndExpAST = lAndExpAST;
        this.lOrExpAST = lOrExpAST;
        this.state =2;
    }


    public String getOp() {
        return op;
    }

    public LAndExpAST getlAndExpAST() {
        return lAndExpAST;
    }

    public LOrExpAST getlOrExpAST() {
        return lOrExpAST;
    }

    public int getState() {
        return state;
    }
}
