package Frontend.AST.DeclAST;

import Frontend.AST.IASTNode;

import java.util.ArrayList;

public class VarDeclAST extends IASTNode {
    private ArrayList<VarDefAST> varDefs;
    private int state;
    public VarDeclAST(){
        this.varDefs = new ArrayList<VarDefAST>();
        this.state =1;
    }
    public VarDeclAST(ArrayList<VarDefAST> varDefs){
        this.varDefs = varDefs;
        this.state =2;
    }


    public ArrayList<VarDefAST> getVarDefs(){
        return varDefs;
    }


    public void addVarDefs( VarDefAST varDef){
        this.varDefs.add(varDef);
    }

}
