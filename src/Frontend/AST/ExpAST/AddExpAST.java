package Frontend.AST.ExpAST;

import Frontend.AST.IASTNode;

//加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp // 1.MulExp 2.+ 需覆盖 3.- 需
//覆盖
public class AddExpAST extends IASTNode {

    public String getPrintLLVM(){
        if(regID!=-1){
            Integer integer = regID;
            return "%" + integer.toString();
        }else if(isGlobal){
            return "@" + globalIdent;
        }
        else if(returnValue!=null){
            return returnValue;
        }else {
            return "Error: Unknown return value";
        }
    }

    private MulExpAST mulExp;
    private AddExpAST addExp;
    private String op;

    private int state;

    public AddExpAST(MulExpAST mulExpAST){
        this.mulExp = mulExpAST;
        this.state=1;
//        System.out.println("<AddExp>");
    }
    public AddExpAST(MulExpAST mulExpAST, String op , AddExpAST addExp){
        this.op = op;
        this.addExp = addExp;
        this.mulExp = mulExpAST;
        this.state = 2;
//        System.out.println("<AddExp>");
    }

    public String getOp(){
        return op;
    }


    public MulExpAST getMulExpAST(){
        return mulExp;
    }

    public AddExpAST getAddExpAST(){
        return addExp;
    }

    public int getState(){
        return state;
    }













}
