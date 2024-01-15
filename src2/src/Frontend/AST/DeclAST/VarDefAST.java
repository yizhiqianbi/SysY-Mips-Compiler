package Frontend.AST.DeclAST;

import Frontend.AST.ExpAST.ConstExpAST;
import Frontend.AST.IASTNode;
import Frontend.Token;

import java.util.ArrayList;

public class VarDefAST extends IASTNode {

    public String getPrintLLVM(){
        if(regID!=-1){
            Integer integer = regID;
            return "%" + integer.toString();
        }else if(isGlobal){
            return "@" + globalIdent;
        }
        else if(returnValue!=null){
            return returnValue;
        }else {
            return "Error: Unknown return value";
        }
    }

    private Token ident;
    private InitValAST initValAST;
    private ArrayList<ConstExpAST> constExpASTS;
    // state = 1: 1
    // state = 2: {1,1}
    // state = 3: {{2,2},{2,2}}
    private int state;

    // int a
    public VarDefAST(Token ident){
        this.ident = ident;
        this.constExpASTS = new ArrayList<>();
        this.state =1;
    }

    // int a = 1
    public VarDefAST(Token ident, InitValAST initVal){
        this.ident = ident;
        this.initValAST = initVal;
        this.constExpASTS = new ArrayList<>();
        this.state =1;
    }

    // int a[] a[][]
    public VarDefAST(Token ident, ArrayList<ConstExpAST> constExpASTS){
        this.ident = ident;
        this.constExpASTS = constExpASTS;
        this.state = this.constExpASTS.size()+1;
    }


    //int a[]={}  a[][]={{}{}}
    public VarDefAST(Token ident, ArrayList<ConstExpAST> constExpASTS ,InitValAST initValAST) {
        this.ident = ident;
        this.constExpASTS = constExpASTS;
        this.initValAST = initValAST;
        this.state = this.constExpASTS.size()+1;
    }


    public int getState() {
        return state;
    }

    public Token getIdent() {
        return ident;
    }

    public ArrayList<ConstExpAST> getConstExpASTS() {
        return constExpASTS;
    }

    public InitValAST getInitValAST() {
        return initValAST;
    }


}
