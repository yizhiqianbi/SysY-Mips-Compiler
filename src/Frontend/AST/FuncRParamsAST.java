package Frontend.AST;

import Frontend.AST.ExpAST.ExpAST;

import java.util.ArrayList;

public class FuncRParamsAST extends IASTNode{
    private ArrayList<ExpAST> expASTS;

    public FuncRParamsAST(){
        this.expASTS = new ArrayList<ExpAST>();
    }

    public FuncRParamsAST(ArrayList<ExpAST> expASTS){
        this.expASTS = expASTS;
    }

    public void  addExpAST(ExpAST exp){
        this.expASTS.add(exp);
    }

    public ArrayList<ExpAST> getExpASTS() {
        return expASTS;
    }


}
