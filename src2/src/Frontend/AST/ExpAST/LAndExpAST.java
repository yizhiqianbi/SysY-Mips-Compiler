package Frontend.AST.ExpAST;

import Frontend.AST.IASTNode;
import Frontend.Token;

import java.beans.Expression;

public class LAndExpAST extends IASTNode {
    private LAndExpAST lAndExp;
    private EqExpAST eqExpAST;
    private String op;

    private int state;


    public LAndExpAST(EqExpAST eqExpAST){
        this.eqExpAST = eqExpAST;
        state =1;
    }

    public LAndExpAST( EqExpAST eqExpAST , String op, LAndExpAST lAndExp){
        this.lAndExp = lAndExp;
        this.op = op;
        this.eqExpAST = eqExpAST;
        state=2;
    }

    public String getOp() {
        return op;
    }

    public EqExpAST getEqExpAST() {
        return eqExpAST;
    }

    public LAndExpAST getlAndExp() {
        return lAndExp;
    }

    public int getState() {
        return state;
    }
}
