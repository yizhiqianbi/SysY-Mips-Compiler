package Frontend.AST.ExpAST;

import Frontend.AST.IASTNode;
import Frontend.AST.NumberAST;

public class PrimaryExpAST extends IASTNode {


    public String getPrintLLVM(){
        if(regID!=-1){
            Integer integer = regID;
            return "%" + integer.toString();
        }
        else if(returnValue!=null){
            return returnValue;
        }else {
            return "Error: Unknown return value";
        }
    }


    private ExpAST expAST;

    private LValAST lValAST;

    private NumberAST numberAST;

    private int state;

    public PrimaryExpAST(ExpAST expAST){
        this.expAST = expAST;
        this.state = 1;

    }

    public PrimaryExpAST(LValAST lValAST){
        this.lValAST = lValAST;
        this.state = 2;
    }

    public PrimaryExpAST(NumberAST numberAST){
        this.numberAST = numberAST;
        this.state = 3;
    }

    public ExpAST getExpAST() {
        return expAST;
    }

    public LValAST getlValAST() {
        return lValAST;
    }

    public int getState() {
        return state;
    }

    public NumberAST getNumberAST() {
        return numberAST;
    }
}
