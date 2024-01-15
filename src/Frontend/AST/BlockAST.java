package Frontend.AST;

import Frontend.Token;

import java.util.ArrayList;

public class BlockAST extends IASTNode{


    private ArrayList<BlockItemAST> blockItemASTS;
    private Token rBRACE;
    private int state;
    public BlockAST(){
        this.state = 1;
        this.blockItemASTS = new ArrayList<>();
    }

    public BlockAST(ArrayList<BlockItemAST> blockItemASTS){
        this.blockItemASTS = blockItemASTS;
        this.state = 2;
    }


    public int getState() {
        return state;
    }

    public ArrayList<BlockItemAST> getBlockItemASTS() {
        return blockItemASTS;
    }

    public Token getrBRACE() {
        return rBRACE;
    }

    public void setrBRACE(Token rBRACE) {
        this.rBRACE = rBRACE;
    }
}
