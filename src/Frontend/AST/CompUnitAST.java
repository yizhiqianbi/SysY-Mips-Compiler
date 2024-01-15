package Frontend.AST;

import Frontend.AST.DeclAST.DeclAST;

import java.util.ArrayList;

public class CompUnitAST extends IASTNode{

    private ArrayList<FuncDefAST> funcDefASTS;
    private ArrayList<DeclAST> declASTS;
    private MainFuncDefAST mainFuncDefAST;


    public CompUnitAST(ArrayList<DeclAST> declASTS,ArrayList<FuncDefAST> funcDefASTS,MainFuncDefAST mainFuncDefAST) {
        this.funcDefASTS = funcDefASTS;
        this.declASTS = declASTS;
        this.mainFuncDefAST = mainFuncDefAST;
    }

    public ArrayList<DeclAST> getDeclASTS() {
        return declASTS;
    }

    public ArrayList<FuncDefAST> getFuncDefASTS() {
        return funcDefASTS;
    }

    public MainFuncDefAST getMainFuncDefAST() {
        return mainFuncDefAST;
    }
}
