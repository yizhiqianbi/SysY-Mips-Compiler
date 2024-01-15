package Frontend.AST;

import Frontend.AST.ExpAST.UnaryExpAST;

public class UnaryOpAST extends IASTNode{
    private String operator;
    public UnaryOpAST(String operator){
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}
