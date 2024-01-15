package Frontend.AST;

import Frontend.AST.DeclAST.DeclAST;

public class BlockItemAST extends IASTNode{
    private DeclAST declAST;
    private StmtAST stmtAST;


    public BlockItemAST(StmtAST stmtAST){
        this.stmtAST = stmtAST;
    }

    public BlockItemAST(DeclAST declAST){
        this.declAST = declAST;
    }

    public DeclAST getDeclAST() {
        return declAST;
    }

    public StmtAST getStmtAST() {
        return stmtAST;
    }
}
