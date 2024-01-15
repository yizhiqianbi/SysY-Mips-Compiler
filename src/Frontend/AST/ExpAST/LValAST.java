package Frontend.AST.ExpAST;

import Frontend.AST.IASTNode;
import Frontend.Token;

import java.util.ArrayList;

public class LValAST extends IASTNode {
    private Token ident;
    private ArrayList<ExpAST> expASTS;
    private int state;

    public LValAST(Token ident){
        this.ident = ident;
        this.expASTS = new ArrayList<>();
        this.state=1;
    }

    public LValAST(Token idnet, ArrayList<ExpAST> expASTS){
        this.ident = idnet;
        this.expASTS = expASTS;
        this.state=2;
    }

    public ArrayList<ExpAST> getExpASTS() {
        return expASTS;
    }

    public int getState() {
        return state;
    }

    public Token getIdent() {
        return ident;
    }

    public int getDimension() {
        return this.expASTS.size();
    }
}
