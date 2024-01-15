package Frontend.AST;

import java.util.ArrayList;

public class FuncFParamsAST extends IASTNode{
    private ArrayList<FuncFParamAST> funcFParamASTS;

    public FuncFParamsAST(ArrayList<FuncFParamAST> funcFParamsASTS){
        this.funcFParamASTS = funcFParamsASTS;
    }

    public FuncFParamsAST(){
        this.funcFParamASTS = new ArrayList<FuncFParamAST>();
    }

    public void AddFuncFParamAST(FuncFParamAST funcFParamAST){
        this.funcFParamASTS.add(funcFParamAST);
    }

    public ArrayList<FuncFParamAST> getFuncFParamASTS() {
        return funcFParamASTS;
    }
}
