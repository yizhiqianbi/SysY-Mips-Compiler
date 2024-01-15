package Frontend.AST;

import Frontend.AST.ExpAST.ExpAST;
import Frontend.AST.ExpAST.LValAST;

public class FormatStringAST extends IASTNode{
    private String fstr;

    public FormatStringAST(String formatString){
        this.fstr = formatString;
    }

    public String getFstr() {
        return fstr;
    }
}
