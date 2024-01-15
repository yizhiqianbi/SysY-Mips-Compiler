package Frontend.AST.ExpAST;

import Frontend.AST.FuncRParamsAST;
import Frontend.AST.IASTNode;
import Frontend.AST.UnaryOpAST;
import Frontend.Token;

public class UnaryExpAST extends IASTNode {

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
    private PrimaryExpAST primaryExpAST;
    private FuncRParamsAST funcRParamsAST;
    private UnaryExpAST unaryExpAST;
    private UnaryOpAST unaryOpAST;
    private int state;

    public UnaryExpAST(PrimaryExpAST primaryExpAST){
        this.state =1 ;
        this.primaryExpAST  = primaryExpAST;
    }

    public UnaryExpAST(Token ident){
        this.state =2;
        this.ident = ident;
    }

    public UnaryExpAST(Token ident, FuncRParamsAST funcRParamsAST){
        this.state =2;
        this.ident  = ident;
        this.funcRParamsAST = funcRParamsAST;
    }

    public UnaryExpAST(UnaryOpAST unaryOpAST,UnaryExpAST unaryExpAST){
        this.state =3;
        this.unaryOpAST = unaryOpAST;
        this.unaryExpAST = unaryExpAST;


    }


    public int getState() {
        return state;
    }

    public FuncRParamsAST getFuncRParamsAST() {
        return funcRParamsAST;
    }

    public UnaryExpAST getUnaryExpAST() {
        return unaryExpAST;
    }

    public PrimaryExpAST getPrimaryExpAST() {
        return primaryExpAST;
    }

    public UnaryOpAST getUnaryOpAST() {
        return unaryOpAST;
    }

    public Token getIdent() {
        return ident;
    }
}
