package Frontend.AST;

import Frontend.AST.ExpAST.ConstExpAST;
import Frontend.Token;

import java.util.ArrayList;

public class FuncFParamAST extends IASTNode{
    private String bType;
    private Token ident;

    private ArrayList<Token> leftBrack;
    private ArrayList<ConstExpAST> constExpASTS;
    private int state;

    public FuncFParamAST( String bType,Token ident){
        this.bType = bType;
        this.ident = ident;
        this.state = 1;
        this.constExpASTS = new ArrayList<>();
        this.leftBrack =new ArrayList<>();
    }

    public FuncFParamAST( String bType ,Token ident,ArrayList<ConstExpAST> constExpASTS){
        this.ident = ident;
        this.state = 2;
        this.constExpASTS = constExpASTS;
        this.bType = bType;
        this.leftBrack =new ArrayList<>();
    }


    public ArrayList<ConstExpAST> getConstExpASTS() {
        return constExpASTS;
    }

    public ArrayList<Token> getLeftBrack() {
        return leftBrack;
    }

    public int getState() {
        return state;
    }
    public Token getIdent() {
        return ident;
    }

    public String getbType() {
        return bType;
    }

    public void setLeftBrack(ArrayList<Token> leftBrack) {
        this.leftBrack = leftBrack;
    }


}
