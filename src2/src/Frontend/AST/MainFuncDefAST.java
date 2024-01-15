package Frontend.AST;

public class MainFuncDefAST extends IASTNode{
    private BlockAST blockAST;
    public MainFuncDefAST(BlockAST blockAST) {
        this.blockAST = blockAST;
    }

    public BlockAST getBlockAST() {
        return blockAST;
    }
}
