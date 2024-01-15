package Error;

import Frontend.AST.*;
import Frontend.AST.DeclAST.*;
import Frontend.AST.ExpAST.*;
import Frontend.TokenType;
import Symbol.*;
import Utils.DataStruct.Triple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class ErrorHandler {
    private static final ErrorHandler instance = new ErrorHandler();

    public static ErrorHandler getInstance() {
        return instance;
    }

    private List<Triple<Map<String, Symbol>,Boolean, FuncType>> symbolTables = new ArrayList<>();
    private void addSymbolTable(boolean isFunc, FuncType funcType) {
        symbolTables.add(new Triple<>(new HashMap<>(), isFunc, funcType));
    }

    private void removeSymbolTable() {
        symbolTables.remove(symbolTables.size() - 1);
    }

    private boolean containsInCurrent(String ident) {
        return symbolTables.get(symbolTables.size() - 1).getFirst().containsKey(ident);
    }

    private boolean contains(String ident) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).getFirst().containsKey(ident)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInFunc() {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).getSecond()) {
                return true;
            }
        }
        return false;
    }

    private FuncType getFuncType() {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).getSecond()) {
                return symbolTables.get(i).getThird();
            }
        }
        return null;
    }

    private boolean isCurrentFunc() {
        symbolTables.get(symbolTables.size() - 1).getSecond();
        return symbolTables.get(symbolTables.size() - 1).getSecond();
    }

    private FuncType getCurrentFuncType() {
        symbolTables.get(symbolTables.size() - 1).getThird();
        return symbolTables.get(symbolTables.size() - 1).getThird();
    }

    private void put(String ident, Symbol symbol) {
        symbolTables.get(symbolTables.size() - 1).getFirst().put(ident, symbol);
    }

    private Symbol get(String ident) {
        for (int i = symbolTables.size() - 1; i >= 0; --i) {
            if (symbolTables.get(i).getFirst().containsKey(ident)) {
                return symbolTables.get(i).getFirst().get(ident);
            }
        }
        return null;
    }

    public ArrayList<Error> errors = new ArrayList<>();
    public static int loopLevel =0 ;

    public boolean printErrors() throws FileNotFoundException {
        File file = new File ("error.txt");
        PrintStream ps = new PrintStream(file);
        System.setOut(ps);
        errors.sort(Error::compareTo);
        if(errors.size()==0){
            return false;
        }
        for (Error error : errors) {
//            IOUtils.error(error.toString());

            System.out.println(error.toString());
        }
        return true;
    }

    public void addError(ErrorType type,int line) {
        Error newError = new Error(line,type);
        for (Error error : errors) {
            if (error.getErrorLine() == newError.getErrorLine() && error.getErrorType().equals(newError.getErrorType())) {
                return;
            }
        }
        errors.add(newError);
    }

    public void addError(Error newError) {
        for (Error error : errors) {
            if (error.equals(newError)) {
                return;
            }
        }
        errors.add(newError);
    }


    /*
    编译单元 CompUnit → {Decl} {FuncDef} MainFuncDef // 1.是否存在Decl 2.是否存在FuncDef
声明 Decl → ConstDecl | VarDecl // 覆盖两种声明
常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // 1.花括号内重复0
次 2.花括号内重复多次
基本类型 BType → 'int' // 存在即可
常数定义 ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal // 包含普通变量、一维
数组、二维数组共三种情况
常量初值 ConstInitVal → ConstExp
| '{' [ ConstInitVal { ',' ConstInitVal } ] '}' // 1.常表达式初值 2.一维数组初值 3.二
维数组初值
变量声明 VarDecl → BType VarDef { ',' VarDef } ';' // 1.花括号内重复0次 2.花括号内重复
多次
变量定义 VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
| Ident { '[' ConstExp ']' } '=' InitVal
变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'// 1.表达式初值 2.一维数
组初值 3.二维数组初值
函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // 1.无形参 2.有形参
主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block // 存在main函数
函数类型 FuncType → 'void' | 'int' // 覆盖两种类型的函数
函数形参表 FuncFParams → FuncFParam { ',' FuncFParam } // 1.花括号内重复0次 2.花括号内
重复多次
函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量2.一维
数组变量 3.二维数组变量
语句块 Block → '{' { BlockItem } '}' // 1.花括号内重复0次 2.花括号内重复多次
语句块项 BlockItem → Decl | Stmt // 覆盖两种语句块项
语句 Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
| [Exp] ';' //有无Exp两种情况
| Block
| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个
ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
| 'break' ';' | 'continue' ';'
| 'return' [Exp] ';' // 1.有Exp 2.无Exp
| LVal '=' 'getint''('')'';'
| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
语句 ForStmt → LVal '=' Exp // 存在即可
表达式 Exp → AddExp 注：SysY 表达式是int 型表达式 // 存在即可
条件表达式 Cond → LOrExp // 存在即可
左值表达式 LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number // 三种情况均需覆盖
数值 Number → IntConst // 存在即可
一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // 3种情况均需覆盖,函
数调用也需要覆盖FuncRParams的不同情况
| UnaryOp UnaryExp // 存在即可
单目运算符 UnaryOp → '+' | '−' | '!' 注：'!'仅出现在条件表达式中 // 三种均需覆盖
函数实参表 FuncRParams → Exp { ',' Exp } // 1.花括号内重复0次 2.花括号内重复多次 3.Exp需
要覆盖数组传参和部分数组传参
乘除模表达式 MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp // 1.UnaryExp
2.* 3./ 4.% 均需覆盖
加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp // 1.MulExp 2.+ 需覆盖 3.- 需
覆盖
关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp // 1.AddExp
2.< 3.> 4.<= 5.>= 均需覆盖
相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp // 1.RelExp 2.== 3.!= 均需
覆盖
逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp // 1.EqExp 2.&& 均需覆盖
逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp // 1.LAndExp 2.|| 均需覆盖
常量表达式 ConstExp → AddExp 注：使用的Ident 必须是常量 // 存在即可
     */

    // 我们设计的框架是再走一遍，遇到声明就
    //编译单元 CompUnit → {Decl} {FuncDef} MainFuncDef // 1.是否存在Decl 2.是否存在FuncDef

    public void compUnitError(CompUnitAST compUnitAST){

        addSymbolTable(false, null);
        //编译单元 CompUnit → {Decl} {FuncDef} MainFuncDef // 1.是否存在Decl 2.是否存在FuncDef
        for(DeclAST declAST: compUnitAST.getDeclASTS()){
            declError(declAST);
        }
        for(FuncDefAST funcDefAST: compUnitAST.getFuncDefASTS()){
            funcDefError(funcDefAST);
        }
        mainFuncError(compUnitAST.getMainFuncDefAST());
    }
    //声明 Decl → ConstDecl | VarDecl // 覆盖两种声明
    public void declError(DeclAST declAST){
        //1 const
        if(declAST.getState() == 1){
            constDeclError(declAST.getConstDeclAST());
        }
        //2 var
        else if(declAST.getState() == 2){
            varDeclError(declAST.getVarDeclAST());
        }
    }


    //常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // 1.花括号内重复0
    //次 2.花括号内重复多次
    public void constDeclError(ConstDeclAST constDeclAST){

        for(ConstDefAST constDefAST: constDeclAST.getConstDefASTS()){
            constDefError(constDefAST);
        }

    }




//    public void btypeError(BType bType){
//
//    }

    //常数定义 ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal // 包含普通变量、一维
    //数组、二维数组共三种情况
    public void constDefError(ConstDefAST constDefAST){
        // TODO what will do after return
        if (containsInCurrent(constDefAST.getIdent().getVal())) {
            ErrorHandler.getInstance().addError(ErrorType.b,constDefAST.getIdent().getTokenLine());
            return;
        }
        switch (constDefAST.getState()){
            case 1->{
                put(constDefAST.getIdent().getVal(), new ArrayTable(constDefAST.getIdent().getVal(),true,0 ));
            }
            case 2->{
                for(ConstExpAST constExpAST: constDefAST.getConstExpASTS()){
                    constExpError(constExpAST);
                }
                put(constDefAST.getIdent().getVal(), new ArrayTable(constDefAST.getIdent().getVal(),true,constDefAST.getConstExpASTS().size() ));
            }
        }
        constInitValError(constDefAST.getConstInitValAST());
    }

    //常量初值 ConstInitVal → ConstExp
    //| '{' [ ConstInitVal { ',' ConstInitVal } ] '}' // 1.常表达式初值 2.一维数组初值 3.二
    //维数组初值
    public void constInitValError(ConstInitValAST constInitValAST){
        switch (constInitValAST.getState()){
            case 1 ->{
                constExpError(constInitValAST.getConstExpAST());
            }
            case 2 ->{
                //TODO feel like there is something wrong here
                for(ConstInitValAST constInitValAST1 : constInitValAST.getConstInitValASTS() ){
                    constInitValError(constInitValAST1);
                }
            }

        }
    }
    //变量声明 VarDecl → BType VarDef { ',' VarDef } ';' // 1.花括号内重复0次 2.花括号内重复
    //多次

    public void varDeclError(VarDeclAST varDeclAST){
        for(VarDefAST varDefAST : varDeclAST.getVarDefs()){
            varDefError(varDefAST);
        }
    }
    //变量定义 VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
    //| Ident { '[' ConstExp ']' } '=' InitVal
    public void varDefError(VarDefAST varDefAST){
        if(containsInCurrent(varDefAST.getIdent().getVal())){
            ErrorHandler.getInstance().addError(ErrorType.b,varDefAST.getIdent().getTokenLine());
            return;
        }

        if(!varDefAST.getConstExpASTS().isEmpty()){
            for(ConstExpAST constExpAST: varDefAST.getConstExpASTS()){
                constExpError(constExpAST);
            }
        }
        put(varDefAST.getIdent().getVal(),new ArrayTable(varDefAST.getIdent().getVal(),false,varDefAST.getConstExpASTS().size()));
        if(varDefAST.getInitValAST()!=null){
            initValError(varDefAST.getInitValAST());
        }

    }
    //变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'// 1.表达式初值 2.一维数
    //组初值 3.二维数组初值
    public void initValError(InitValAST initValAST){
        if(initValAST.getExpAST()!=null){
            expError(initValAST.getExpAST());
        }else{
            for(InitValAST initValAST1: initValAST.getInitValASTS()){
                initValError(initValAST1);
            }
        }
    }
    //函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // 1.无形参 2.有形参
    public void funcDefError(FuncDefAST funcDefAST){
        if(containsInCurrent(funcDefAST.getIdent().getVal())){
            ErrorHandler.getInstance().addError(ErrorType.b,funcDefAST.getIdent().getTokenLine());
            return;
        }
        FuncType funcType ;
        if(funcDefAST.getFuncTypeAST().getFuncTypeToken().getType().equals(TokenType.VOIDTK)) funcType = FuncType.VOID;
        else if(funcDefAST.getFuncTypeAST().getFuncTypeToken().getType().equals(TokenType.INTTK)) funcType = FuncType.INT;
        else funcType =null;
        // 无参数
        if(funcDefAST.getFuncFParamsAST()==null){
            put(funcDefAST.getIdent().getVal(),new FuncTable(funcDefAST.getIdent().getVal(),funcType,new ArrayList<>()));
        }

        else{
            ArrayList<FuncParam> params = new ArrayList<>();
            for(FuncFParamAST funcFParamAST: funcDefAST.getFuncFParamsAST().getFuncFParamASTS()){
                params.add(new FuncParam(funcFParamAST.getIdent().getVal(),funcFParamAST.getLeftBrack().size() ));
            }
            put(funcDefAST.getIdent().getVal(),new FuncTable(funcDefAST.getIdent().getVal(),funcType,params ));
        }
        addSymbolTable(true,funcType);
        if(funcDefAST.getFuncFParamsAST()!=null){
            funcFParamsError(funcDefAST.getFuncFParamsAST());
        }
        blockError(funcDefAST.getBlockAST());
        removeSymbolTable();
    }

    //主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block // 存在main函数
    public void mainFuncError(MainFuncDefAST mainFuncDefAST){
        put("main", new FuncTable("main",FuncType.INT,new ArrayList<>()));
        addSymbolTable(true,FuncType.INT);
        blockError(mainFuncDefAST.getBlockAST());
        removeSymbolTable();
    }

    //函数类型 FuncType → 'void' | 'int' // 覆盖两种类型的函数
    public void funcTypeError(FuncTypeAST funcTypeAST){
        // it seems useless
    }

    //函数形参表 FuncFParams → FuncFParam { ',' FuncFParam } // 1.花括号内重复0次 2.花括号内重复多次
    public void funcFParamsError(FuncFParamsAST funcFParamsAST){
        for(FuncFParamAST funcFParamAST: funcFParamsAST.getFuncFParamASTS()){
            funcFParamError(funcFParamAST);
        }
    }

    //函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量2.一维数组变量 3.二维数组变量
    public void funcFParamError(FuncFParamAST funcFParamAST){
        if(containsInCurrent(funcFParamAST.getIdent().getVal())){
            ErrorHandler.getInstance().addError(ErrorType.b,funcFParamAST.getIdent().getTokenLine());
            // todo return or not need to pay attendion
            return;
        }
        put(funcFParamAST.getIdent().getVal(),new ArrayTable(funcFParamAST.getIdent().getVal(),
                false, funcFParamAST.getLeftBrack().size()));

    }

    //语句块 Block → '{' { BlockItem } '}' // 1.花括号内重复0次 2.花括号内重复多次
    public void blockError(BlockAST blockAST){
        for(BlockItemAST blockItemAST: blockAST.getBlockItemASTS()){
            blockItemError(blockItemAST);
        }
        //TODO TODO 这里没有完全看明白 这里没有完全看明白
        if (isCurrentFunc()) {
            if (getCurrentFuncType() == FuncType.INT) {
                if (blockAST.getBlockItemASTS().isEmpty() ||
                        blockAST.getBlockItemASTS().get(blockAST.getBlockItemASTS().size() - 1).getStmtAST() == null ||
                        blockAST.getBlockItemASTS().get(blockAST.getBlockItemASTS().size() - 1).getStmtAST().getReturnToken() == null) {
                    ErrorHandler.getInstance().addError( ErrorType.g,blockAST.getrBRACE().getTokenLine());
                }
            }
        }


    }

    //语句块项 BlockItem → Decl | Stmt // 覆盖两种语句块项
    public void blockItemError(BlockItemAST blockItemAST){
        // TODO 返回值这种东西我决定放在Parser里面去做
        if(blockItemAST.getDeclAST()!=null){
            declError(blockItemAST.getDeclAST());
        }else{
            stmtError(blockItemAST.getStmtAST());
        }
    }

    //语句 Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    //| [Exp] ';' //有无Exp两种情况
    //| Block
    //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个
    //ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
    //| 'break' ';' | 'continue' ';'
    //| 'return' [Exp] ';' // 1.有Exp 2.无Exp
    //| LVal '=' 'getint''('')'';'
    //| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    public void stmtError(StmtAST stmtAST){
        switch (stmtAST.getState()){
            // Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
            case 1 ->{
                lvalError(stmtAST.getlValAST());


                 if(get(stmtAST.getlValAST().getIdent().getVal()) instanceof  ArrayTable){
                                     ArrayTable arrayTable = (ArrayTable) get(stmtAST.getlValAST().getIdent().getVal());
                                    if(arrayTable.getIsConst()){
                                         ErrorHandler.getInstance().addError(ErrorType.h, stmtAST.getlValAST().getIdent().getTokenLine());
                                    }
                 }
                 else{
                     ErrorHandler.getInstance().addError(ErrorType.c, stmtAST.getlValAST().getIdent().getTokenLine());
                 }


                expError(stmtAST.getExpAST());
            }
            //| [Exp] ';' //有无Exp两种情况
            case 2 ->{
                // do nothing
            }
            //| [Exp] ';' //有无Exp两种情况

            case 3 ->{
                expError(stmtAST.getExpAST());
            }
            //| Block

            case 4 ->{
                addSymbolTable(false, null);
                blockError(stmtAST.getBlockAST());
                removeSymbolTable();
            }
            //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
            case 5 ->{
                condError(stmtAST.getCondAST());
                stmtError(stmtAST.getIfStmtAST());
            }
            //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
            case 6 ->{
                condError(stmtAST.getCondAST());
                stmtError(stmtAST.getIfStmtAST());
                stmtError(stmtAST.getElseStmtAST());
            }
            //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个
            //ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
            // TODO  seems 7 is useless
//            case 7 ->{
//                forStmtError(stmtAST.getForStmt1());
//                condError(stmtAST.getCondAST());
//                forStmtError(stmtAST.getForStmt2());
//
////                loopLevel++;
//            }
            //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个
            //ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
            case 8 ->{
                if(stmtAST.getForStmt1()!=null) forStmtError(stmtAST.getForStmt1());
                if(stmtAST.getCondAST()!=null) condError(stmtAST.getCondAST());
                if(stmtAST.getForStmt2()!=null) forStmtError(stmtAST.getForStmt2());
                loopLevel++;
                stmtError(stmtAST.getStmtAST());
                loopLevel--;


            }
            //     | 'break' ';' | 'continue' ';'
            case 9 ->{
                if(loopLevel==0){
                    ErrorHandler.getInstance().addError(ErrorType.m,stmtAST.getBreakToken().getTokenLine());
                }
                // it seems nothing
            }
            //     | 'break' ';' | 'continue' ';'

            case 10 ->{
                if(loopLevel==0){
                    ErrorHandler.getInstance().addError(ErrorType.m,stmtAST.getContinueToken().getTokenLine());
                }
                // it seems nothin
            }
            //    | 'return' [Exp] ';' // 1.有Exp 2.无Exp

            //TODO 11 seems no use
            case 11 ->{
                ////
                ////多此一举了，因为有没有 exp已经分类好了
//                if(isInFunc()){
//                    if(getFuncType() == FuncType.INT && stmtAST.getExpAST()==null && stmtAST.getReturnToken()==null){
//                        ErrorHandler.getInstance().addError(ErrorType.g,stmtAST.getReturnToken().getTokenLine());
//                    }
//                }
            }
            //    | 'return' [Exp] ';' // 1.有Exp 2.无Exp
            case 12 ->{
                ////多此一举了，因为有没有 exp已经分类好了
                if(isInFunc()){
                    if(getFuncType() == FuncType.VOID && stmtAST.getExpAST()!=null){
                        ErrorHandler.getInstance().addError(ErrorType.f,stmtAST.getReturnToken().getTokenLine());
                    }
                }
                expError(stmtAST.getExpAST());
            }
            //      | LVal '=' 'getint''('')'';'
            case 13->{
                //检查Lval单个本身是否有问题
                lvalError(stmtAST.getlValAST());
                //检查Lval是否是const的赋值
                if(get(stmtAST.getlValAST().getIdent().getVal()) instanceof  ArrayTable){
                    ArrayTable arrayTable = (ArrayTable) get(stmtAST.getlValAST().getIdent().getVal());
                    if(arrayTable.getIsConst()){
                        ErrorHandler.getInstance().addError(ErrorType.h, stmtAST.getlValAST().getIdent().getTokenLine());
                    }
                }
            }
            case 14,15->{
                String fString= stmtAST.getFormatString().getFstr();
                char[] charArray = fString.toCharArray();
                int strLenth = fString.length();
                int numFormStr=0;
                int numOfExp = stmtAST.getExpASTS().size();
                for(int i=0; i<strLenth; i++) {
                    if(i<strLenth-1 && charArray[i] == '%' && charArray[i+1] == 'd'){
                        numFormStr++;
                    }
                }
                if(numFormStr != numOfExp){
//                    System.out.println(numFormStr + " " + numOfExp);
                    ErrorHandler.getInstance().addError(ErrorType.l, stmtAST.getPrintToken().getTokenLine());
                }
                for(ExpAST expAST: stmtAST.getExpASTS()){
                    expError(expAST);
                }
            }
        }
    }

    //语句 ForStmt → LVal '=' Exp // 存在即可
    public void forStmtError(ForStmtAST forStmtAST){
        lvalError(forStmtAST.getlValAST());
        expError(forStmtAST.getExpAST());
    }
    //表达式 Exp → AddExp 注：SysY 表达式是int 型表达式 // 存在即可
    public void expError(ExpAST expAST){
        addExpError(expAST.getAddExpAST());
    }

    //条件表达式 Cond → LOrExp // 存在即可
    public void condError(CondAST condAST){
        lOrExpError(condAST.getlOrExpAST());
    }

    //左值表达式 LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
    public void lvalError(LValAST lValAST){
        if(!contains(lValAST.getIdent().getVal())){
            ErrorHandler.getInstance().addError(ErrorType.c,lValAST.getIdent().getTokenLine());
            return;
        }
        switch (lValAST.getState()){
            case 1 ->{

            }
            case 2 ->{
                for(ExpAST expAST: lValAST.getExpASTS()){
                    expError(expAST);
                }
            }
        }


    }

    //基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number // 三种情况均需覆盖
    public void primaryExpError(PrimaryExpAST primaryExpAST){
        switch (primaryExpAST.getState()){
            case 1 ->{
                expError(primaryExpAST.getExpAST());
            }
            case 2 ->{
                lvalError(primaryExpAST.getlValAST());
            }
            case 3 ->{
                numberError(primaryExpAST.getNumberAST());
            }
        }
    }

    //数值 Number → IntConst // 存在即可
    public void numberError(NumberAST numberAST){
        // it seemes no use
    }

    //一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // 3种情况均需覆盖,函
    //数调用也需要覆盖FuncRParams的不同情况
    //| UnaryOp UnaryExp // 存在即可
    public void unaryExpError(UnaryExpAST unaryExpAST){
        // it seems have no use
        switch (unaryExpAST.getState()){
            // primary
            case 1->{
                primaryExpError(unaryExpAST.getPrimaryExpAST());
            }
            //ident
//            case 2->{
//
//            }
            //ident funcrparams
            case 2->{
                if(!contains(unaryExpAST.getIdent().getVal())){
                    ErrorHandler.getInstance().addError(ErrorType.c, unaryExpAST.getIdent().getTokenLine() );
                    return;
                }
                Symbol symbol = get(unaryExpAST.getIdent().getVal());
                if(!(symbol instanceof FuncTable)){
//                    System.out.println("meow1");
                    ErrorHandler.getInstance().addError(ErrorType.e,unaryExpAST.getIdent().getTokenLine());
                    return;
                }
                FuncTable funcTable = (FuncTable) symbol;
                if(unaryExpAST.getFuncRParamsAST() == null){
                    if(funcTable.getFuncParams().size()!=0){
                        ErrorHandler.getInstance().addError(ErrorType.d,unaryExpAST.getIdent().getTokenLine());
                    }
                }
                else{
                    if(funcTable.getFuncParams().size()!=(unaryExpAST.getFuncRParamsAST().getExpASTS().size())){
                        ErrorHandler.getInstance().addError(ErrorType.d,unaryExpAST.getIdent().getTokenLine());
                        return;
                    }
                    List<Integer> funcFParamDimensions = new ArrayList<>();
                    for(FuncParam funcParam: funcTable.getFuncParams()){
                        funcFParamDimensions.add(funcParam.getDimension());
                    }
                    List<Integer> funcRParamDimensions = new ArrayList<>();
                    if(unaryExpAST.getFuncRParamsAST()!=null){
                        funcRParamsError(unaryExpAST.getFuncRParamsAST());
                        for(ExpAST expAST: unaryExpAST.getFuncRParamsAST().getExpASTS()){
                            FuncParam funcRParam =getFuncParamInExp(expAST);
                            // TODO now the bug is here !
                            if(funcRParam != null){
                                if(funcRParam.getName() == null){
                                    funcRParamDimensions.add(funcRParam.getDimension());
                                }
                                else{
                                    Symbol symbol1 = get(funcRParam.getName());
                                    if(symbol1 instanceof ArrayTable){
                                        funcRParamDimensions.add(((ArrayTable) symbol1).getDimension() - funcRParam.getDimension());
                                    }
                                    else if(symbol1 instanceof FuncTable){
                                        funcRParamDimensions.add(((FuncTable) symbol1).getType() == FuncType.VOID ? -1 : 0);
                                    }
                                }
                            }
                        }
                    }
                    if (!Objects.equals(funcFParamDimensions, funcRParamDimensions)) {
//                        System.out.println("meow");
                        ErrorHandler.getInstance().addError( ErrorType.e,unaryExpAST.getIdent().getTokenLine());
//                        System.out.println("meow");
                    }

                }

            }
            //opexp unaryexp
            case 3->{
                unaryExpError(unaryExpAST.getUnaryExpAST());
            }
        }
    }
    //一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // 3种情况均需覆盖,函
    //数调用也需要覆盖FuncRParams的不同情况
    //| UnaryOp UnaryExp // 存在即可


    private FuncParam getFuncParamInExp(ExpAST expAST) {
        // Exp -> AddExp
        return getFuncParamInAddExp(expAST.getAddExpAST());
    }
    private FuncParam getFuncParamInLVal(LValAST lValAST) {
        return new FuncParam(lValAST.getIdent().getVal(), lValAST.getExpASTS().size());
    }
    private FuncParam getFuncParamInPrimaryExp(PrimaryExpAST primaryExpAST) {
        // PrimaryExp -> '(' Exp ')' | LVal | Number
        if (primaryExpAST.getExpAST() != null) {
            return getFuncParamInExp(primaryExpAST.getExpAST());
        } else if (primaryExpAST.getlValAST() != null) {
            return getFuncParamInLVal(primaryExpAST.getlValAST());
        } else {
            return new FuncParam(null, 0);
        }
    }
    private FuncParam getFuncParamInUnaryExp(UnaryExpAST unaryExpAST) {
        if (unaryExpAST.getPrimaryExpAST() != null) {
            return getFuncParamInPrimaryExp(unaryExpAST.getPrimaryExpAST());
        } else if (unaryExpAST.getIdent() != null) {
            return get(unaryExpAST.getIdent().getVal()) instanceof FuncTable ? new FuncParam(unaryExpAST.getIdent().getVal(), 0) : null;
        } else {
            return getFuncParamInUnaryExp(unaryExpAST.getUnaryExpAST());
        }
    }
    private FuncParam getFuncParamInMulExp(MulExpAST mulExpAST) {
        return getFuncParamInUnaryExp(mulExpAST.getUnaryExpAST());
    }
    private FuncParam getFuncParamInAddExp(AddExpAST addExpAST) {
        // AddExp -> MulExp | MulExp ('+' | '-') AddExp
        return getFuncParamInMulExp(addExpAST.getMulExpAST());
    }


    //函数实参表 FuncRParams → Exp { ',' Exp } // 1.花括号内重复0次 2.花括号内重复多次 3.Exp需
    //要覆盖数组传参和部分数组传参
    public void funcRParamsError(FuncRParamsAST funcRParamsAST){
        for(ExpAST expAST : funcRParamsAST.getExpASTS()){
            expError(expAST);
        }
    }

    //乘除模表达式 MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp // 1.UnaryExp
    //2.* 3./ 4.% 均需覆盖
    public void mulExpError(MulExpAST mulExpAST){
        switch (mulExpAST.getState()){
            case 1 ->{
                unaryExpError(mulExpAST.getUnaryExpAST());
            }

            case 2 ->{
                unaryExpError(mulExpAST.getUnaryExpAST());
                mulExpError(mulExpAST.getMulExpAST());
            }
        }
    }

    //加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp // 1.MulExp 2.+ 需覆盖 3.- 需
    //覆盖
    public void addExpError(AddExpAST addExpAST){
        switch (addExpAST.getState()){
            case 1 ->{
                mulExpError(addExpAST.getMulExpAST());
            }
            case 2 ->{
                mulExpError(addExpAST.getMulExpAST());
                addExpError(addExpAST.getAddExpAST());
            }

        }
    }

    //关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp // 1.AddExp
    //2.< 3.> 4.<= 5.>= 均需覆盖
    public void relExpError(RelExpAST relExpAST){
        switch (relExpAST.getState()){
            case 1 ->{
                addExpError(relExpAST.getAddExp());
            }
            case 2 ->{
                addExpError(relExpAST.getAddExp());
                relExpError(relExpAST.getRelExp());
            }
        }
    }

    //相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp // 1.RelExp 2.== 3.!= 均需
    //覆盖
    public void eqExpError(EqExpAST eqExpAST){
        switch (eqExpAST.getState()){
            case 1 ->{
                relExpError(eqExpAST.getRelExpAST());
            }
            case 2 ->{
                relExpError(eqExpAST.getRelExpAST());
                eqExpError(eqExpAST.getEqExpAST());
            }
        }
    }

    //逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp // 1.EqExp 2.&& 均需覆盖
    public void lAndExpError(LAndExpAST lAndExpAST){
        switch (lAndExpAST.getState()){
            case 1->{
                eqExpError(lAndExpAST.getEqExpAST());
            }
            case 2->{
                eqExpError(lAndExpAST.getEqExpAST());
                lAndExpError(lAndExpAST.getlAndExp());
            }
        }
    }

    //逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp // 1.LAndExp 2.|| 均需覆盖
    // 要改左递归
    public void lOrExpError(LOrExpAST lOrExpAST){
        switch (lOrExpAST.getState()){
            case 1->{
                lAndExpError(lOrExpAST.getlAndExpAST());
            }
            case 2->{
                lAndExpError(lOrExpAST.getlAndExpAST());
                lOrExpError(lOrExpAST.getlOrExpAST());
            }
        }
    }
    //常量表达式 ConstExp → AddExp 注：使用的Ident 必须是常量 // 存在即可
    public void constExpError(ConstExpAST constExpAST){
        addExpError(constExpAST.getAddExp());
    }


}
