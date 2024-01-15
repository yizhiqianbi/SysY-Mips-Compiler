package Frontend.AST.ExpAST;

import Frontend.AST.IASTNode;
import Frontend.Token;

public class MulExpAST extends IASTNode {

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


    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }



    private MulExpAST mulExpAST;
    private UnaryExpAST unaryExpAST;
    private String op;

    private int state;

    public MulExpAST(UnaryExpAST unaryExpAST){
        this.unaryExpAST = unaryExpAST;
        this.state=1;
    }

    public MulExpAST(UnaryExpAST unaryExpAST, String op, MulExpAST mulExpAST){
        this.mulExpAST = mulExpAST;
        this.op = op;
        this.unaryExpAST = unaryExpAST;
        this.state =2;
    }

    public String getOp() {
        return op;
    }

    public MulExpAST getMulExpAST() {
        return mulExpAST;
    }

    public UnaryExpAST getUnaryExpAST() {
        return unaryExpAST;
    }

    public int getState() {
        return state;
    }

    public void setMulExpAST(MulExpAST mulExpAST) {
        this.mulExpAST = mulExpAST;
    }

}
