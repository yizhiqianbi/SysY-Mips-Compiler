package Frontend.AST;

import Frontend.Token;
import Symbol.FuncType;

public class FuncDefAST extends IASTNode{
    private FuncTypeAST funcTypeAST;
    private Token ident;
    private BlockAST blockAST;
//    private FuncRParamsAST funcRParamsAST;
    private FuncFParamsAST funcFParamsAST;
    private int state;

    public FuncDefAST(FuncTypeAST funcTypeAST, Token ident,BlockAST blockAST){
        this.funcTypeAST = funcTypeAST;
        this.ident = ident;
        this.blockAST = blockAST;
        this.state = 1;
    }

    public FuncDefAST(FuncTypeAST funcTypeAST, Token ident, FuncFParamsAST funcFParamsAST,BlockAST blockAST){
        this.funcTypeAST = funcTypeAST;
        this.ident = ident;
        this.funcFParamsAST = funcFParamsAST;
        this.blockAST =blockAST;
        this.state = 2;
    }


    public Token getIdent() {
        return ident;
    }

    public BlockAST getBlockAST() {
        return blockAST;
    }

    public FuncFParamsAST getFuncFParamsAST() {
        return funcFParamsAST;
    }

    public FuncTypeAST getFuncTypeAST() {
        return funcTypeAST;
    }

    public int getState() {
        return state;
    }
}
