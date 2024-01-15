package Frontend.AST.DeclAST;

import Frontend.AST.ExpAST.ConstExpAST;
import Frontend.AST.IASTNode;
import Frontend.Token;

import java.util.ArrayList;

public class ConstDefAST extends IASTNode {

//    private int regID = -1;


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


//    常数定义 ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal // 包含普通变量、一维
//数组、二维数组共三种情况
//    常数定义其实初始就赋值了

    private Token ident;
    private ArrayList<ConstExpAST> constExpASTS;
    private ConstExpAST constExpAST;

    private ConstInitValAST constInitValAST;

    private int state;
//    如果是二位数组
    private int dimension;

    public ConstDefAST (Token ident, ConstInitValAST constInitValAST){
        this.ident = ident;
        this.constInitValAST = constInitValAST;
        constExpASTS = new ArrayList<>();
        this.state =1;
    }

//    state = 2 means it only contains a single const value
//    public ConstDefAST(String ident,ConstExpAST constExp,ConstInitValAST constInitValAST ){
//        this.ident =ident;
//        this.constExpAST = constExp;
//        this.constInitValAST = constInitValAST;
//        this.state = 2;
//    }

    //    state = 2
    public ConstDefAST(Token ident, ArrayList<ConstExpAST> constExpASTS ,ConstInitValAST constInitValAST){
        this.ident = ident;
        this.constExpASTS = constExpASTS;
//        this.dimension = dimention;
        this.state = 2;
        this.constInitValAST = constInitValAST;
    }


    public Token getIdent() {
        return ident;
    }

    public int getState() {
        return state;
    }

    public ArrayList<ConstExpAST> getConstExpASTS() {
        return constExpASTS;
    }

    public ConstInitValAST getConstInitValAST() {
        return constInitValAST;
    }

    public ConstExpAST getConstExpAST() {
        return constExpAST;
    }

    public int getDimension() {
        return  dimension;
    }

}
