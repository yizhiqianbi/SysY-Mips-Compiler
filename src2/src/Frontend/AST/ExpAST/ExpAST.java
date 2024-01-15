package Frontend.AST.ExpAST;

import Frontend.AST.IASTNode;

public class ExpAST extends IASTNode {


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

    private AddExpAST addExpAST;

    public ExpAST (AddExpAST addExpAST){
        this.addExpAST = addExpAST;
    }

    public AddExpAST getAddExpAST() {
        return addExpAST;
    }
}
