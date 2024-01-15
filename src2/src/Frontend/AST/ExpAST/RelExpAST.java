package Frontend.AST.ExpAST;

import Frontend.AST.IASTNode;
import Frontend.Token;

public class RelExpAST extends IASTNode {
    private AddExpAST  addExp;
    private RelExpAST relExp;
    private String op;
    private int state;

    public RelExpAST (AddExpAST addExp){
        this.addExp = addExp;
        this.state = 1;
    }

    public RelExpAST (AddExpAST addExp, String op ,RelExpAST relExp){
        this.addExp = addExp;
        this.op = op;
        this.relExp = relExp;
        this.state = 2;
    }


    public AddExpAST getAddExp() {
        return addExp;
    }

    public RelExpAST getRelExp() {
        return relExp;
    }

    public String getOp() {
        return op;
    }

    public int getState() {
        return state;
    }
}
