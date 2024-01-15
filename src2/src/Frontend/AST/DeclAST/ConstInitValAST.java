package Frontend.AST.DeclAST;

import Frontend.AST.ExpAST.ConstExpAST;
import Frontend.AST.IASTNode;

import java.util.ArrayList;

public class ConstInitValAST extends IASTNode {
//    private int regID = -1;

    public String getPrintLLVM(){
        if(regID!=-1){
            Integer integer = regID;
            return "%" + integer.toString();
        }
        else if(returnValue!=null){
            return returnValue;
        }else {
            return "Error: Unknown return value"+ "returnType" + returnType +" returnValue:" + returnValue ;
        }
    }

    /**
     * 常量初值 ConstInitVal → ConstExp
     * | '{' [ ConstInitVal { ',' ConstInitVal } ] '}' // 1.常表达式初值 2.一维数组初值 3.二
     * 维数组初值
     * */

    /**
     * ConstInitVal → ConstExp
     *             → 5
     *
     *ConstInitVal → '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
     *             → {1, 2, 3}
     *
     * ConstInitVal → '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
     *             → {{1, 2}, {3, 4}}
     * */
    private ConstExpAST constExpAST;

    private ArrayList<ConstInitValAST> constInitValASTS;

    private int dimension = 0;

    // state = 1 , 1
    // state = 2 , {1,1}
    // state = 3 , {{1,1},{1,1}}
    private int state;

    // single exp
    public ConstInitValAST(ConstExpAST constExpAST) {
        this.constExpAST = constExpAST;
        this.state = 1;
        this.constInitValASTS =new ArrayList<>();
        this.dimension = 0;
    }

    // 二维或者三维
    public ConstInitValAST(ArrayList<ConstInitValAST> constInitValASTS){
        this.constInitValASTS = constInitValASTS;
        this.state = 2;
        this.dimension = constInitValASTS.get(0).dimension +1;
    }

    // { } 空的
    public ConstInitValAST(){
        this.constInitValASTS =new ArrayList<>();
        this.state =3;
    }

    //add the
    public void addConstInitValAST(ConstInitValAST constInitValAST) {
        if(this.state == constInitValAST.getState()+1) this.constInitValASTS.add(constInitValAST);
        else System.out.println("error: wrong dimention" );
    }





    public ConstExpAST getConstExpAST() {
        return constExpAST;
    }


    public int getState() {
        return state;
    }

    public ArrayList<ConstInitValAST> getConstInitValASTS() {
        return constInitValASTS;
    }
}
