package Frontend.AST.DeclAST;

import Frontend.AST.IASTNode;

import java.util.ArrayList;

public class ConstDeclAST extends IASTNode {
//     ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // 1.花括号内重复0
//次 2.花括号内重复多次

    private ArrayList<ConstDefAST> constDefASTS;

    private int state ;

    public ConstDeclAST(){
        this.state =1;
        this.constDefASTS = new ArrayList<>();
    }

    public ConstDeclAST(ArrayList<ConstDefAST> constDefASTS) {
        this.state = 2;
        this.constDefASTS = constDefASTS;
    }

    //! 没有用过
    public void addConstDefAST(ConstDefAST constDefAST){
        this.constDefASTS.add(constDefAST);
    }

    public ArrayList<ConstDefAST> getConstDefASTS() {
        return constDefASTS;
    }


}
