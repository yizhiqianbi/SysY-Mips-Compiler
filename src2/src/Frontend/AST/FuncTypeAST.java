package Frontend.AST;

import Frontend.Token;

public class FuncTypeAST extends IASTNode{
    private String functype;
    private Token funcTypeToken;
    public FuncTypeAST(String functype) {
        this.functype = functype;
    }

    public FuncTypeAST(Token funcTypeToken) {
        this.funcTypeToken = new Token(funcTypeToken.getType(),funcTypeToken.getVal(),funcTypeToken.getTokenLine());
    }

    public Token getFuncTypeToken() {
        return funcTypeToken;
    }

//    public String getFunctype() {
//        return functype;
//    }
}
