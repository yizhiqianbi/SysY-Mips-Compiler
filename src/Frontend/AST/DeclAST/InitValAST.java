package Frontend.AST.DeclAST;

import Frontend.AST.ExpAST.ExpAST;
import Frontend.AST.IASTNode;

import java.util.ArrayList;

public class InitValAST extends IASTNode {
    //!

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
    //!
    private ExpAST expAST;
    private ArrayList<InitValAST> initValASTS;
    private int state;

    public InitValAST(ExpAST expAST) {
        this.initValASTS = new ArrayList<>();
        this.expAST = expAST;
        this.state = 1;
    }

    public InitValAST(ArrayList<InitValAST> initValASTS) {
        this.initValASTS = initValASTS;
        this.state = 2;
    }

    public InitValAST(){
        this.initValASTS = new ArrayList<>();
        this.state =3;
    }





    public ArrayList<InitValAST> getInitValASTS() {
        return initValASTS;
    }

    public ExpAST getExpAST() {
        return expAST;
    }

    public int getState() {
        return state;
    }
}
