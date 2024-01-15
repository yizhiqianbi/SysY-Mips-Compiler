package Frontend.AST;

import Frontend.AST.ExpAST.ExpAST;
import Frontend.AST.ExpAST.LValAST;
import Frontend.Token;

import java.util.ArrayList;

public class StmtAST extends IASTNode{
    private LValAST lValAST;
    private ExpAST expAST;
    private ArrayList<ExpAST> expASTS;

    private BlockAST blockAST;

    private CondAST condAST;

    private StmtAST ifStmtAST;
    private StmtAST elseStmtAST;

    private ForStmtAST forStmt1;
    private ForStmtAST forStmt2;

    private FormatStringAST formatString;

    private BreakAST breakAST;
    private Token breakToken;
    private ContinueAST continueAST;
    private Token continueToken;

    private ReturnAST returnAST;

    private GetintAST getintAST;
    private Token returnToken;

    private Token printToken;
    private StmtAST stmtAST;

    private int state;




// Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    public StmtAST(LValAST lValAST, ExpAST expAST){
        this.lValAST = lValAST;
        this.expAST = expAST;
        this.state =1;
    }

//| [Exp] ';' //有无Exp两种情况
    public StmtAST(){
        this.state =2;
    }

    public StmtAST(ExpAST expAST){
        this.expAST = expAST;
        this.state =3;
    }

//| Block
    public StmtAST(BlockAST blockAST){
        this.blockAST   = blockAST;
        this.state =4;
    }
//| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else

    public StmtAST(CondAST condAST, StmtAST ifStmtAST){
        this.condAST = condAST;
        this.ifStmtAST = ifStmtAST;
        this.state =5;
    }
    public StmtAST(CondAST condAST , StmtAST ifStmtAST,StmtAST elseStmtAST){
        this.condAST = condAST;
        this.ifStmtAST = ifStmtAST;
        this.elseStmtAST = elseStmtAST;
        this.state = 6;
    }
//| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个
//ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
//    有8种情况
//    我是8种情况分开写合适还是直接null合适捏
//    我感觉除了初始化这条都挺重要的
//    这种情况没用上过
     public StmtAST(ForStmtAST forStmt1, CondAST condAST, ForStmtAST forStmt2 ){
        this.forStmt1 = forStmt1;
        this.condAST = condAST;
        this.forStmt2 = forStmt2;
        this.state = 7;
     }
// TODO
    public StmtAST(ForStmtAST forStmt1, CondAST condAST, ForStmtAST forStmt2, StmtAST stmtAST ){
        this.forStmt1 = forStmt1;
        this.condAST = condAST;
        this.forStmt2 = forStmt2;
        this.stmtAST = stmtAST;
        this.state = 8;
    }

//     | 'break' ';' | 'continue' ';'
    public StmtAST(BreakAST breakAST){
        this.breakAST = breakAST;
        this.state = 9;
    }

    public StmtAST(ContinueAST continueAST){
        this.continueAST = continueAST;
        this.state = 10;
    }

//    | 'return' [Exp] ';' // 1.有Exp 2.无Exp


    public StmtAST(Token returnToken){
        this.returnToken = returnToken;
        this.state = 11;
    }
    public StmtAST(Token returnToken, ExpAST expAST){
        this.returnToken = returnToken;
        this.expAST = expAST;
        this.state = 12;
    }

//      | LVal '=' 'getint''('')'';'
    public StmtAST(LValAST lValAST,GetintAST getintAST){
        this.lValAST = lValAST;
        this.getintAST = getintAST;
        this.state = 13;
    }


//    | 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    public StmtAST(FormatStringAST formatString,Token printToken){
        this.printToken = printToken;
        this.formatString = formatString;
        this.state = 14;

    }


    public StmtAST(FormatStringAST formatString, ArrayList<ExpAST> expASTS,Token printToken){
        this.printToken = printToken;
        this.expASTS = expASTS;
        this.formatString = formatString;
        this.state = 15;
    }

    public ExpAST getExpAST() {
        return expAST;
    }

    public LValAST getlValAST() {
        return lValAST;
    }


    public ArrayList<ExpAST> getExpASTS() {
        if(expASTS==null){
            return new ArrayList<ExpAST>();
        }
        return expASTS;
    }

    public int getState() {
        return state;
    }

    public BlockAST getBlockAST() {
        return blockAST;
    }

    public BreakAST getBreakAST() {
        return breakAST;
    }

    public CondAST getCondAST() {
        return condAST;
    }

    public ContinueAST getContinueAST() {
        return continueAST;
    }

    public FormatStringAST getFormatString() {
        return formatString;
    }

    public ForStmtAST getForStmt1() {
        return forStmt1;
    }

    public ForStmtAST getForStmt2() {
        return forStmt2;
    }

    public GetintAST getGetintAST() {
        return getintAST;
    }

    public ReturnAST getReturnAST() {
        return returnAST;
    }

    public StmtAST getElseStmtAST() {
        return elseStmtAST;
    }

    public StmtAST getIfStmtAST() {
        return ifStmtAST;
    }

    public void setGetintAST(GetintAST getintAST) {
        this.getintAST = getintAST;
    }


    public Token getPrintToken() {
        return printToken;
    }

    public void setPrintToken(Token printToken) {
        this.printToken = printToken;
    }

    public Token getReturnToken() {
        return returnToken;
    }

    public StmtAST getStmtAST() {
        return stmtAST;
    }

    public Token getBreakToken() {
        return breakToken;
    }

    public Token getContinueToken() {
        return continueToken;
    }

    public void setBreakToken(Token breakToken) {
        this.breakToken = breakToken;
    }

    public void setContinueToken(Token continueToken) {
        this.continueToken = continueToken;
    }
}
