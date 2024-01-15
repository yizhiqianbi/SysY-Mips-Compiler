package Frontend.AST.DeclAST;

import Frontend.AST.IASTNode;

public class DeclAST extends IASTNode {

    private ConstDeclAST constDeclAST;
    private VarDeclAST varDeclAST;
    private int state;

    public DeclAST(ConstDeclAST constDeclAST){
        this.constDeclAST = constDeclAST;
        this.state = 1;
    }

    public DeclAST (VarDeclAST varDeclAST){
        this.varDeclAST = varDeclAST;
        this.state = 2;
    }

    public int getState() {
        return state;
    }

    public ConstDeclAST getConstDeclAST() {
        return constDeclAST;
    }

    public VarDeclAST getVarDeclAST() {
        return varDeclAST;
    }
}
