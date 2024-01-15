package Frontend.AST.ExpAST;

import Frontend.AST.IASTNode;

public class ConstExpAST extends IASTNode {

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



    private  AddExpAST addExp;

    private int state;
    public ConstExpAST (AddExpAST addExp) {
        this.addExp = addExp;
    }

    public int getState() {
        return state;
    }

    public  AddExpAST getAddExp() {
        return addExp;
    }

}
