package Frontend.AST.ExpAST;

import Frontend.AST.IASTNode;
import Frontend.Token;

public class EqExpAST extends IASTNode {

    private EqExpAST eqExpAST;

    private RelExpAST relExpAST;

    private String op;

    private int state;

    public EqExpAST (RelExpAST relExpAST) {
        this.relExpAST = relExpAST;
        this.state = 1;
    }

    public EqExpAST ( RelExpAST relExpAST , String op , EqExpAST eqExpAST) {
        this.eqExpAST = eqExpAST;
        this.op = op;
        this.relExpAST = relExpAST;
        this.state = 2;
    }

    public EqExpAST getEqExpAST() {
        return eqExpAST;
    }

    public String getOp() {
        return op;
    }

    public RelExpAST getRelExpAST() {
        return relExpAST;
    }

    public int getState() {
        return state;
    }
}
