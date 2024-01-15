package Frontend;

import Frontend.AST.*;
import Frontend.AST.DeclAST.*;
import Frontend.AST.ExpAST.*;
import Error.*;
import java.io.Console;
import java.util.ArrayList;

public class Parser {

    private static final Parser instance;

    private LexicalAnalyser lexicalAnalyser = LexicalAnalyser.getInstance();
    private ArrayList<Token> tokens = lexicalAnalyser.getTokenList().tokens;
    private TokenList tokenList = lexicalAnalyser.getTokenList();

    private Token curToken;
    private Token tmpToken;

    private CompUnitAST compUnitAST;

    public CompUnitAST getCompUnitAST() {
        return compUnitAST;
    }

    static {
        try {
            instance = new Parser();
        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Parser getInstance() {
        return instance;
    }

    public void parse(){
        compUnitAST = parseCompUnitAST();
    }
    public CompUnitAST parseCompUnitAST(){
        ArrayList<DeclAST> decls = new ArrayList<DeclAST>();
        ArrayList<FuncDefAST> funcDefASTS = new ArrayList<FuncDefAST>();
        MainFuncDefAST mainFuncDefAST;
        Token token;
        while (true){
            tmpToken = tokenList.getToken();
            if(tokenList.ahead(0).getType().equals(TokenType.INTTK) &&
                    tokenList.ahead(1).getType().equals(TokenType.MAINTK) ){
                mainFuncDefAST = parseMainFuncDefAST();
                break;
            }
            //const
            if(tmpToken.getType().equals(TokenType.CONSTTK)){
//                tokenList.back(1);
                DeclAST declAST = parseDeclAST();
                decls.add(declAST);
            }
            //int tk void tk
            else if(tmpToken.getType().equals(TokenType.INTTK)||tmpToken.getType().equals(TokenType.VOIDTK)){
//                token =tokenList.consume();
                tmpToken = tokenList.ahead(1);
                if(tmpToken.getType().equals(TokenType.IDENFR)){
                    //
                    if(tokenList.ahead(2).getType().equals(TokenType.LPARENT)){
//                        tokenList.back(1);
                        FuncDefAST funcDefAST = parseFuncDefAST();
                        funcDefASTS.add(funcDefAST);
                    }
                    //
                    else{
//                        tokenList.back(1);
                        DeclAST declAST = parseDeclAST();
                        decls.add(declAST);
                    }
                }
            }

        }
        System.out.println("<CompUnit>");
        return new CompUnitAST(decls,funcDefASTS,mainFuncDefAST);
    }

    private DeclAST parseDeclAST(){
        Token token = tokenList.getToken();
        if(token.getType().equals(TokenType.CONSTTK)){
            ConstDeclAST constDeclAST = parseConstDeclAST();
            return new DeclAST(constDeclAST);
        }
        else if(token.getType().equals(TokenType.INTTK)){
            VarDeclAST varDeclAST = parseVarDeclAST();
            return new DeclAST(varDeclAST);
        }

        //error
        return null;
    }

    private ConstDeclAST parseConstDeclAST(){
        tokenList.consume();
        tokenList.consume();

        ConstDefAST constDefAST = parseConstDefAST();
        ArrayList<ConstDefAST> constDefASTS = new ArrayList<ConstDefAST>();
        constDefASTS.add(constDefAST);
        Token token;
        tmpToken = tokenList.getToken();
        while (tmpToken.getType().equals(TokenType.COMMA)){
             token = tokenList.consume();
            ConstDefAST  cconstDefAST = parseConstDefAST();
            constDefASTS.add(cconstDefAST);
            tmpToken = tokenList.getToken();
        }
        match(TokenType.SEMICN);

        System.out.println("<ConstDecl>");
        return new ConstDeclAST(constDefASTS);
    }

    //常数定义 ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal // 包含普通变量、一维
    //数组、二维数组共三种情况
    private ConstDefAST parseConstDefAST(){
        Token ident = tokenList.consume();
        Token token ;
        tmpToken =tokenList.getToken();


        if(tmpToken.getType().equals(TokenType.ASSIGN)){
            token = tokenList.consume();
            ConstInitValAST constInitValAST = parseConstInitValAST();
            System.out.println("<ConstDef>");
            return new ConstDefAST(ident, constInitValAST);
        }
        // [
        ArrayList<ConstExpAST> constExpASTs = new ArrayList<ConstExpAST>();
        while (tmpToken.getType().equals(TokenType.LBRACK)){
            token = tokenList.consume();

            ConstExpAST cconstExpAST = parseConstExpAST();
            constExpASTs.add(cconstExpAST);
            match(TokenType.RBRACK);
        }
        // eat '='
        token = tokenList.consume();

        ConstInitValAST constInitValAST = parseConstInitValAST();
        System.out.println("<ConstDef>");

        return  new ConstDefAST(ident,constExpASTs,constInitValAST);
    }

    private ConstInitValAST parseConstInitValAST(){
        tmpToken = tokenList.getToken();
        Token token;
//        System.out.println("tst");
        //常量初值 ConstInitVal → ConstExp
        //| '{' [ ConstInitVal { ',' ConstInitVal } ] '}' // 1.常表达式初值 2.一维数组初值 3.二
        //维数组初值

        if(tmpToken.getType().equals(TokenType.LBRACE)){

            token = tokenList.consume();
            tmpToken = tokenList.getToken();
            if(tmpToken.getType().equals(TokenType.RBRACE)){
                // empty {}
                token = tokenList.consume();
                System.out.println("<ConstInitVal>");
                return new ConstInitValAST();
            }
            else{
                //
                ConstInitValAST constInitValAST = parseConstInitValAST();
                ArrayList<ConstInitValAST> constInitValASTS = new ArrayList<ConstInitValAST>();
                constInitValASTS.add(constInitValAST);
                tmpToken = tokenList.getToken();
                while (tmpToken.getType().equals(TokenType.COMMA)){
                    token =tokenList.consume();
                    constInitValAST = parseConstInitValAST();
                    constInitValASTS.add(constInitValAST);
                    tmpToken = tokenList.getToken();
                }
                //eat the right }
                token = tokenList.consume();
                System.out.println("<ConstInitVal>");
                return new ConstInitValAST(constInitValASTS);
            }
        }
        else{
//            System.out.println("tst");
            ConstExpAST constExpAST = parseConstExpAST();
            System.out.println("<ConstInitVal>");
            return new ConstInitValAST(constExpAST);
        }
    }

    private VarDeclAST parseVarDeclAST(){
        // eat the Btype
        Token token = tokenList.consume();

        VarDefAST varDefAST = parseVarDefAST();

        ArrayList<VarDefAST> varDefs = new ArrayList<VarDefAST>();
        varDefs.add(varDefAST);
        tmpToken = tokenList.getToken();
        while (tmpToken.getType().equals(TokenType.COMMA)){
            token = tokenList.consume();
            varDefAST = parseVarDefAST();
            varDefs.add(varDefAST);
            tmpToken = tokenList.getToken();
        }
        match(TokenType.SEMICN);

        System.out.println("<VarDecl>");
        return new VarDeclAST(varDefs);
    }

    private VarDefAST parseVarDefAST(){
        //变量定义 VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
        //| Ident { '[' ConstExp ']' } '=' InitVal
        Token ident =tokenList.consume();
        Token token;
        tmpToken = tokenList.getToken();
        // a[]
        if(tmpToken.getType().equals(TokenType.LBRACK)){
            ArrayList<ConstExpAST> constExpASTS = new ArrayList<ConstExpAST>();
            while (tmpToken.getType().equals(TokenType.LBRACK)){
                token = tokenList.consume();
                ConstExpAST constExpAST  = parseConstExpAST();
                constExpASTS.add(constExpAST);
//                token = tokenList.consume();
                match(TokenType.RBRACK);

                tmpToken = tokenList.getToken();
            }
            // a[] = Init
            if(tmpToken.getType().equals(TokenType.ASSIGN)){
                token = tokenList.consume();
                InitValAST initValAST = parseInitValAST();
                System.out.println("<VarDef>");
                return new VarDefAST(ident,constExpASTS,initValAST);
            }
            // a[]
            else{
                System.out.println("<VarDef>");
                return new VarDefAST(ident,constExpASTS);
            }
        }
        //  a=
        else if(tmpToken.getType().equals(TokenType.ASSIGN)){
            token = tokenList.consume();
            InitValAST initValAST = parseInitValAST();
            System.out.println("<VarDef>");
            return new VarDefAST(ident,initValAST);
        }
        // a
        else{
            System.out.println("<VarDef>");
            return new VarDefAST(ident);
        }

    }

    private InitValAST parseInitValAST(){
        tmpToken = tokenList.getToken();
        Token token;
        //变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'// 1.表达式初值 2.一维数组初值 3.二维数组初值

        if(tmpToken.getType().equals(TokenType.LBRACE)){
            token = tokenList.consume();
            tmpToken = tokenList.getToken();
            // {}
            if(tmpToken.getType().equals(TokenType.RBRACE)){
                token = tokenList.consume();
                return new InitValAST();
            }
            //{  [ InitVal { ',' InitVal } ] }
            ArrayList<InitValAST> initValASTS = new ArrayList<InitValAST>();
            InitValAST initValAST = parseInitValAST();
            initValASTS.add(initValAST);
            tmpToken = tokenList.getToken();

            while (tmpToken.getType().equals(TokenType.COMMA)){
                token = tokenList.consume();
                initValAST = parseInitValAST();
                initValASTS.add(initValAST);
                tmpToken = tokenList.getToken();
            }
            // eat the }
            token = tokenList.consume();
            System.out.println("<InitVal>");
            return new InitValAST(initValASTS);
        }

        // EXP
        else{
            ExpAST expAST = parseExpAST();
            System.out.println("<InitVal>");
            return new InitValAST(expAST);
        }
    }

    private FuncDefAST parseFuncDefAST(){
        FuncTypeAST funcTypeAST = parseFuncTypeAST();
        Token ident = tokenList.consume();
        Token token;
        tmpToken = tokenList.getToken();
        if(tmpToken.getType().equals(TokenType.LPARENT)){
            token = tokenList.consume();
            tmpToken = tokenList.getToken();
            if(tmpToken.getType().equals(TokenType.INTTK)){
                FuncFParamsAST funcFParamsAST = parseFuncFParamsAST();
                match(TokenType.RPARENT);

                BlockAST blockAST = parseBlockAST();
                System.out.println("<FuncDef>");
                return new FuncDefAST(funcTypeAST,ident,funcFParamsAST,blockAST);
            }
            else{
                match(TokenType.RPARENT);
                BlockAST blockAST = parseBlockAST();
                System.out.println("<FuncDef>");
                return new FuncDefAST(funcTypeAST,ident,blockAST);
            }
        }
        else{
            //error
            return null;
        }
//        Token token = tokenList.consume();

//        return new FuncDefAST(token.getVal());
    }

    private MainFuncDefAST parseMainFuncDefAST(){
        Token token = tokenList.consume();
        token = tokenList.consume();
        token = tokenList.consume();
//        token = tokenList.consume();
        match(TokenType.RPARENT);

        BlockAST blockAST = parseBlockAST();

        System.out.println("<MainFuncDef>");
        return new MainFuncDefAST(blockAST);
    }

    private FuncTypeAST parseFuncTypeAST(){
        Token token = tokenList.consume();
        System.out.println("<FuncType>");
        return new FuncTypeAST(token);
    }
//////////////////////////////////////////////////////////
    private FuncFParamsAST parseFuncFParamsAST(){
        Token token;
        ArrayList<FuncFParamAST> funcFParamASTS  = new ArrayList<FuncFParamAST>();
        FuncFParamAST funcFParamAST = parseFuncFParamAST();
        funcFParamASTS.add(funcFParamAST);
        tmpToken = tokenList.getToken();
        while (tmpToken.getType().equals(TokenType.COMMA)){
            token = tokenList.consume();
            funcFParamAST = parseFuncFParamAST();
            funcFParamASTS.add(funcFParamAST);
            tmpToken = tokenList.getToken();
        }

        System.out.println("<FuncFParams>");
        return new FuncFParamsAST(funcFParamASTS);
    }


    private FuncFParamAST parseFuncFParamAST(){
        Token token = tokenList.consume();
        String btype = token.getVal();
        Token ident = tokenList.consume();
        tmpToken = tokenList.getToken();

        if(tmpToken.getType().equals(TokenType.LBRACK)){
            ArrayList<Token> lbracks =new ArrayList<>();
            lbracks.add(tmpToken);

            token = tokenList.consume();
//            token = tokenList.consume();
//            tmpToken = tokenList.getToken();
            match(TokenType.RBRACK);
            // int a[]
            // int a[][3]
            ArrayList<ConstExpAST> constExpASTS = new ArrayList<ConstExpAST>();

            while (tmpToken.getType().equals(TokenType.LBRACK)){
                lbracks.add(token);
                token = tokenList.consume();
                ConstExpAST constExpAST = parseConstExpAST();
                constExpASTS.add(constExpAST);
//                token = tokenList.consume();
//                tmpToken =tokenList.getToken();
                match(TokenType.RBRACK);
            }
            FuncFParamAST funcFParamAST = new FuncFParamAST(btype,ident,constExpASTS);
            funcFParamAST.setLeftBrack(lbracks);
            System.out.println("<FuncFParam>");
            return funcFParamAST;
        }
        else{
            System.out.println("<FuncFParam>");
            return new FuncFParamAST(btype,ident);
        }
    }

    private BlockAST parseBlockAST(){
        Token token = tokenList.consume();
        ArrayList<BlockItemAST> blockItems = new ArrayList<BlockItemAST>();
        if(token.getType().equals(TokenType.LBRACE)){
            tmpToken = tokenList.getToken();
            while (!(tmpToken.getType().equals(TokenType.RBRACE))){
                BlockItemAST blockItemAST =  parseBlockItemAST();
                blockItems.add(blockItemAST);
                tmpToken = tokenList.getToken();
            }
            // eat the }
            token = tokenList.consume();
            System.out.println("<Block>");
            BlockAST blockAST = new BlockAST(blockItems);
            blockAST.setrBRACE(token);
            return blockAST;
        }
        else {
            //error
            return null;
        }

    }


    private BlockItemAST parseBlockItemAST(){
        Token token;
        tmpToken = tokenList.getToken();

        if(tmpToken.getType().equals(TokenType.CONSTTK)||tmpToken.getType().equals(TokenType.VOIDTK)
                    ||tmpToken.getType().equals(TokenType.INTTK)){
                DeclAST declAST =  parseDeclAST();
                return new BlockItemAST(declAST);
        }
        else {
//            System.out.println("in block item" +tmpToken.getType());
            StmtAST stmtAST = parseStmtAST();
            return new BlockItemAST(stmtAST);
        }
    }

    private StmtAST parseStmtAST(){

        Token token;
        tmpToken = tokenList.getToken();


        // empty expression
        if(tmpToken.getType().equals(TokenType.SEMICN)){
            token  = tokenList.consume();
            System.out.println("<Stmt>");
            return new StmtAST();
        }
        // Block  {
        else if(tmpToken.getType().equals(TokenType.LBRACE)){
            BlockAST blockAST = parseBlockAST();
            System.out.println("<Stmt>");
            return new StmtAST(blockAST);
        }
        // if
        else if(tmpToken.getType().equals(TokenType.IFTK)){
            token = tokenList.consume();
            token = tokenList.consume();
            CondAST condAST = parseCondAST();
//            token = tokenList.consume();
            match(TokenType.RPARENT);

            StmtAST ifstmtAST = parseStmtAST();

            tmpToken = tokenList.getToken();
//            token = tokenList.consume();
            if(tmpToken.getType().equals(TokenType.ELSETK)){
                token = tokenList.consume();
                StmtAST elsestmtAST = parseStmtAST();
                System.out.println("<Stmt>");
                return new StmtAST(condAST,ifstmtAST,elsestmtAST);
            }
            else{
                System.out.println("<Stmt>");
                return new StmtAST(condAST,ifstmtAST);
            }

        }
        // for
        else if(tmpToken.getType().equals(TokenType.FORTK)){
            // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个
            //ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
            token = tokenList.consume();
            token = tokenList.consume();
            tmpToken = tokenList.getToken();
            ForStmtAST forStmtAST1 =null;
            CondAST condAST =null;
            ForStmtAST forStmtAST2 =null;
            StmtAST sstmtAST;
            if(!tmpToken.getType().equals(TokenType.SEMICN)){
                forStmtAST1 = parseForStmtAST();
                // eat semicn
                token = tokenList.consume();
            }else token = tokenList.consume();
            tmpToken = tokenList.getToken();
            if(!tmpToken.getType().equals(TokenType.SEMICN)){
//                tokenList.back(1);
                condAST = parseCondAST();
                // eat semicn
                token = tokenList.consume();
            }else token = tokenList.consume();
            tmpToken = tokenList.getToken();
            if(!tmpToken.getType().equals(TokenType.RPARENT)){
//                tokenList.back(1);
                forStmtAST2 = parseForStmtAST();
                // eat )
                token = tokenList.consume();
            }else token = tokenList.consume();

            StmtAST stmtAST =parseStmtAST();
            System.out.println("<Stmt>");
            return new StmtAST(forStmtAST1,condAST,forStmtAST2,stmtAST);
        }
        // breaktk
        else if(tmpToken.getType().equals(TokenType.BREAKTK)){

            token =tokenList.consume();
//            token =tokenList.consume();
            match(TokenType.SEMICN);
            System.out.println("<Stmt>");
            StmtAST stmtAST =new StmtAST(new BreakAST());
            stmtAST.setBreakToken(token);
            return stmtAST;
        }
        // continue
        else if(tmpToken.getType().equals(TokenType.CONTINUETK)){
            token =tokenList.consume();
//            token =tokenList.consume();
            match(TokenType.SEMICN);

            System.out.println("<Stmt>");
            StmtAST stmtAST =new StmtAST(new ContinueAST());
            stmtAST.setContinueToken(token);
            return stmtAST;
        }
        // return
        else if(tmpToken.getType().equals(TokenType.RETURNTK)){

            Token returnToken =tokenList.consume();
//            token =tokenList.consume();
//            tmpToken =tokenList.getToken();

//            System.out.println("oh yeea");
            if(isExp()){
//                System.out.println(tmpToken.getType()+"isEXP!!!!!!!!!!!!");
//                System.out.println("RETURNING!!!!!!!!!!!!!!!!!!!");
                ExpAST expAST =parseExpAST();
                match(TokenType.SEMICN);
                System.out.println("<Stmt>");
                return new StmtAST(returnToken, expAST);
            }
            else{
                match(TokenType.SEMICN);
                System.out.println("<Stmt>");
                return new StmtAST(returnToken);
            }
//            if(tmpToken.getType().equals(TokenType.SEMICN)){
//                token =tokenList.consume();
//                System.out.println("<Stmt>");
//                return new StmtAST(new ReturnAST());
//            }else{
//                ExpAST expAST =parseExpAST();
//                token = tokenList.consume();
//                System.out.println("<Stmt>");
//                return new StmtAST(new ReturnAST(), expAST);
//            }
        }
        //print
        else if(tmpToken.getType().equals(TokenType.PRINTFTK)){
            Token printToken = new Token(tmpToken.getType(),tmpToken.getVal() ,tmpToken.getTokenLine() );
            token = tokenList.consume();
            token = tokenList.consume();
            FormatStringAST formatStringAST = parseFormatStringAST();
            ArrayList<ExpAST> expASTS =new ArrayList<ExpAST>();
            tmpToken = tokenList.getToken();
            while (tmpToken.getType().equals(TokenType.COMMA)){
                token = tokenList.consume();
                ExpAST expAST = parseExpAST();
                expASTS.add(expAST);
                tmpToken = tokenList.getToken();
            }
            match(TokenType.RPARENT);
            match(TokenType.SEMICN);

            if(expASTS.size()==0){
                System.out.println("<Stmt>");
                return new StmtAST(formatStringAST,printToken);
            }
            else{
                System.out.println("<Stmt>");
                return new StmtAST(formatStringAST, expASTS,printToken);
            }

        }

        // LVal '=' 'getint''('')'';'
        // LVal IS conflict with EXP
        else if(tmpToken.getType().equals(TokenType.IDENFR)&&tokenList.ahead(1).getType()!=TokenType.LPARENT){
            tmpToken = tokenList.getToken();
            int assign = tokenList.getIndex();
            int index = tokenList.getIndex();
            for (int i = index; i < tokenList.getTokens().size() && tokenList.ahead(i-index).getTokenLine() == tmpToken.getTokenLine(); i++) {
                if (tokens.get(i).getType() == TokenType.ASSIGN) {
                    assign = i;
                }
            }

            if(assign > index){
                LValAST lValAST = parseLValAST();
                //eat =
                token = tokenList.consume();
                tmpToken = tokenList.getToken();
                if(tmpToken.getType().equals(TokenType.GETINTTK)){
                    token = tokenList.consume();
                    token = tokenList.consume();
//                token = tokenList.consume();
                    match(TokenType.RPARENT);
                    //TODO TODO ALOT TODO !!!!! TMP IS HERE
//                token = tokenList.consume();
                    match(TokenType.SEMICN);
                    System.out.println("<Stmt>");
                    return new StmtAST(lValAST,new GetintAST());
                }
                //! getint
                else{
                    // Stmt → LVal '=' Exp ';
                    ExpAST expAST = parseExpAST();
//                    token = tokenList.consume();
                    match(TokenType.SEMICN);
                    System.out.println("<Stmt>");
                    return new StmtAST(lValAST,expAST);
                }
            }else{
                // 有  EXP
                ExpAST expAST = parseExpAST();
//        token = tokenList.consume();
                match(TokenType.SEMICN);

                System.out.println("<Stmt>");
                return new StmtAST(expAST);
            }

        }
        // 有  EXP
        ExpAST expAST = parseExpAST();
//        token = tokenList.consume();
        match(TokenType.SEMICN);

        System.out.println("<Stmt>");
        return new StmtAST(expAST);
    }
    private FormatStringAST parseFormatStringAST(){
        Token token = tokenList.consume();
        // TODO TODOTODOTODO
        return new FormatStringAST(token.getVal());
    }

    private ForStmtAST parseForStmtAST(){
        //语句 ForStmt → LVal '=' Exp // 存在即可
        LValAST lValAST = parseLValAST();
        Token token = tokenList.consume();
        ExpAST expAST = parseExpAST();
        System.out.println("<ForStmt>");
        return new ForStmtAST(lValAST,expAST);
    }

    private ExpAST parseExpAST(){
        tmpToken =tokenList.getToken();

        AddExpAST addExpAST = parseAddExpAST();
        System.out.println("<Exp>");
        return new ExpAST(addExpAST);
    }

    private CondAST parseCondAST(){
        tmpToken =tokenList.getToken();
        LOrExpAST lOrExpAST = parseLOrExpAST();
        System.out.println("<Cond>");
        return new CondAST(lOrExpAST);
    }

    private LValAST parseLValAST(){
//        System.out.println(tokenList.getToken().getType()+"????????????");
        Token ident = tokenList.consume();
        Token token ;
        tmpToken = tokenList.getToken();
        if(tmpToken.getType().equals(TokenType.LBRACK)){
            ArrayList<ExpAST> expASTS =new ArrayList<ExpAST>();
            while (tmpToken.getType().equals(TokenType.LBRACK)){
                token = tokenList.consume();
                ExpAST expAST = parseExpAST();
                expASTS.add(expAST);
                match(TokenType.RBRACK);
                tmpToken = tokenList.getToken();
//                token = tokenList.consume();
//                tmpToken = tokenList.getToken();
            }
            System.out.println("<LVal>");
            return new LValAST(ident,expASTS);
        }
        else{
            System.out.println("<LVal>");
            return new LValAST(ident);
        }
    }

    private PrimaryExpAST parsePrimaryExpAST(){
        Token token;
        tmpToken = tokenList.getToken();
//        System.out.println("tokenType:"+tmpToken.getType()+ "tokenLine:"+tmpToken.getTokenLine());
        if(tmpToken.getType().equals(TokenType.LPARENT)){
            token = tokenList.consume();
            ExpAST expAST = parseExpAST();
            token = tokenList.consume();
            System.out.println("<PrimaryExp>");
            return new PrimaryExpAST(expAST);
        }else if(tmpToken.getType().equals(TokenType.INTCON)){
            NumberAST numberAST = parseNumberAST();
            System.out.println("<PrimaryExp>");
            return new PrimaryExpAST(numberAST);
        }else{
            LValAST lValAST = parseLValAST();
            System.out.println("<PrimaryExp>");
            return new PrimaryExpAST(lValAST);
        }
//        System.out.println("meow meow meow");
//        System.out.println(tmpToken.getVal()+tmpToken.getType());
        //error
//        return null;
    }

    private NumberAST parseNumberAST(){
        Token token = tokenList.consume();
        System.out.println("<Number>");
        return new NumberAST(Integer.parseInt(token.getVal()));
    }
    private UnaryExpAST parseUnaryExpAST(){
        //一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // 3种情况均需覆盖,函
        //数调用也需要覆盖FuncRParams的不同情况
        //| UnaryOp UnaryExp // 存在即可
        tmpToken = tokenList.getToken();

//        System.out.println("tmpToken"+ tmpToken.getType());
//        System.out.println(tmpToken.getVal()+ tmpToken.getType() + "!!!!!!!!!!!!!!");
//        System.out.println(tmpToken.getType()+tmpToken.getVal());
        if(tmpToken.getType().equals(TokenType.PLUS)||tmpToken.getType().equals(TokenType.MINU)||tmpToken.getType().equals(TokenType.NOT)){
            UnaryOpAST unaryOp = parseUnaryOpAST();
            UnaryExpAST unaryExp = parseUnaryExpAST();
            System.out.println("<UnaryExp>");
            return new UnaryExpAST(unaryOp,unaryExp);
        }
        //Ident '('
        else if(tmpToken.getType().equals(TokenType.IDENFR)&&tokenList.ahead(1).getType().equals(TokenType.LPARENT)){
            //todo 这里没检查IDENT
            Token ident = tokenList.consume();
            Token token = tokenList.consume();
            tmpToken = tokenList.getToken();

            if (isExp()) {
                FuncRParamsAST funcRParamsAST=parseFuncRParamsAST();
                // if ) eat ;
                match(TokenType.RPARENT);
                System.out.println("<UnaryExp>");
                return new UnaryExpAST(ident,funcRParamsAST);

            }
            else{
                match(TokenType.RPARENT);
                System.out.println("<UnaryExp>");
                return new UnaryExpAST(ident);
            }
        }
        // error没处理直接 else了
        else{
//            System.out.println("tokenType:"+tmpToken.getType()+ "tokenLine:"+tmpToken.getTokenLine());
//            System.out.println("in unaryExp" + tmpToken.getVal());
            PrimaryExpAST primaryExpAST = parsePrimaryExpAST();
            System.out.println("<UnaryExp>");
            return  new UnaryExpAST(primaryExpAST);
        }
//        return null;
    }

    private UnaryOpAST parseUnaryOpAST(){
//        System.out.println("parseUnaryOp!!!!!!!!!!!!!!!!!");
        Token token = tokenList.consume();
        if(token.getType().equals(TokenType.PLUS)){
            System.out.println("<UnaryOp>");
            return new UnaryOpAST("+");
        }else if(token.getType().equals(TokenType.MINU)){
            System.out.println("<UnaryOp>");
            return new UnaryOpAST("-");
        }else if(token.getType().equals(TokenType.NOT)){
            System.out.println("<UnaryOp>");
            return new UnaryOpAST("!");
        }
        // ERROR
        return null;
    }
////////////////////////////////////////////////////////////////////////////////////////////////
    private FuncRParamsAST parseFuncRParamsAST(){
//        System.out.println("parseFuncRParams!!!!!!!!!!!!!!!!!");
        ArrayList<ExpAST> expASTS =new ArrayList<ExpAST>();
        ExpAST expAST = parseExpAST();
        expASTS.add(expAST);
        Token token ;
        tmpToken = tokenList.getToken();
        while (tmpToken.getType().equals(TokenType.COMMA)){
            token = tokenList.consume();
            expAST = parseExpAST();
            expASTS.add(expAST);
            tmpToken = tokenList.getToken();
        }
        System.out.println("<FuncRParams>");
        return new FuncRParamsAST(expASTS);
    }
    private MulExpAST parseMulExpAST(){
        UnaryExpAST unaryExpAST = parseUnaryExpAST();
        Token token;
        tmpToken = tokenList.getToken();
        switch (tmpToken.getType()){
            case MULT -> {
                System.out.println("<MulExp>");
                token = tokenList.consume();
                MulExpAST mulExpAST = parseMulExpAST();

                return new MulExpAST(unaryExpAST, "*", mulExpAST);
            }
            case DIV -> {
                System.out.println("<MulExp>");
                token = tokenList.consume();
                MulExpAST mulExpAST = parseMulExpAST();

                return new MulExpAST(unaryExpAST, "/", mulExpAST);
            }
            case MOD -> {
                System.out.println("<MulExp>");
                token = tokenList.consume();
                MulExpAST mulExpAST = parseMulExpAST();

                return new MulExpAST(unaryExpAST, "%", mulExpAST);
            }
        }
        System.out.println("<MulExp>");
        return new MulExpAST(unaryExpAST);
    }

    private AddExpAST parseAddExpAST(){
        MulExpAST mulExpAST = parseMulExpAST();
        Token token ;
        tmpToken = tokenList.getToken();

        switch (tmpToken.getType()){
            case PLUS -> {
                System.out.println("<AddExp>");
                token =tokenList.consume();
                AddExpAST addExpAST = parseAddExpAST();
                return new AddExpAST(mulExpAST,"+",addExpAST);
            }
            case MINU -> {
                System.out.println("<AddExp>");
                token =tokenList.consume();
                AddExpAST addExpAST = parseAddExpAST();
                return new AddExpAST(mulExpAST,"-",addExpAST);
            }
        }
        System.out.println("<AddExp>");
        return new AddExpAST(mulExpAST);
    }

    private RelExpAST parseRelExpAST() {
        AddExpAST addExpAST = parseAddExpAST();
        Token token;
        tmpToken = tokenList.getToken();
        switch (tmpToken.getType()){
            case GRE ->{
                System.out.println("<RelExp>");
                token =tokenList.consume();
                RelExpAST relExpAST = parseRelExpAST();

                return new RelExpAST(addExpAST, ">",relExpAST);
            }
            case GEQ -> {
                System.out.println("<RelExp>");
                token =tokenList.consume();
                RelExpAST relExpAST = parseRelExpAST();
                return new RelExpAST(addExpAST, ">=",relExpAST);
            }
            case LSS -> {
                System.out.println("<RelExp>");
                token =tokenList.consume();
                RelExpAST relExpAST = parseRelExpAST();
                return new RelExpAST(addExpAST, "<",relExpAST);
            }
            case LEQ -> {
                System.out.println("<RelExp>");
                token =tokenList.consume();
                RelExpAST relExpAST = parseRelExpAST();
                return new RelExpAST(addExpAST, "<=",relExpAST);
            }
        }
        System.out.println("<RelExp>");
        return new  RelExpAST(addExpAST);
    }

    private EqExpAST parseEqExpAST(){
        RelExpAST relExpAST = parseRelExpAST();
        Token token;
        tmpToken = tokenList.getToken();
        switch (tmpToken.getType()){
            case    EQL ->{
                System.out.println("<EqExp>");
                token =tokenList.consume();
                EqExpAST eqExpAST = parseEqExpAST();

                return new EqExpAST(relExpAST,"==",eqExpAST);
            }
            case NEQ ->{
                System.out.println("<EqExp>");
                token =tokenList.consume();
                EqExpAST eqExpAST = parseEqExpAST();
                return new EqExpAST(relExpAST,"!=",eqExpAST);
            }
        }
        System.out.println("<EqExp>");
        return new EqExpAST(relExpAST);
    }

    private LAndExpAST parseLAndExpAST(){
        EqExpAST eqExpAST = parseEqExpAST();
        Token token;
        tmpToken = tokenList.getToken();
        if(tmpToken.getType().equals(TokenType.AND)){
            System.out.println("<LAndExp>");
            token = tokenList.consume();
            LAndExpAST lAndExpAST = parseLAndExpAST();

            return new LAndExpAST(eqExpAST,"&&",lAndExpAST);
        }else{
            System.out.println("<LAndExp>");
            return new LAndExpAST(eqExpAST);
        }
    }

    private LOrExpAST parseLOrExpAST(){
        LAndExpAST lAndExpAST = parseLAndExpAST();
        Token token;
        tmpToken = tokenList.getToken();
        if(tmpToken.getType().equals(TokenType.OR)){
            System.out.println("<LOrExp>");
            token = tokenList.consume();
            LOrExpAST lOrExpAST = parseLOrExpAST();

            return new LOrExpAST(lAndExpAST, "||",lOrExpAST);
        }
        else {
            System.out.println("<LOrExp>");
            return new LOrExpAST(lAndExpAST);
        }
    }
    private ConstExpAST parseConstExpAST(){
        AddExpAST addExpAST = parseAddExpAST();
        System.out.println("<ConstExp>");
        return new ConstExpAST(addExpAST);
    }

    private Token match(TokenType tokenType) {
        switch (tokenType){
            case SEMICN ->{
                if(tokenList.getToken().getType().equals(TokenType.SEMICN)){
                    Token token = tokenList.consume();
                    tmpToken = tokenList.getToken();
                    return token;
                }
                else{
                    ErrorHandler.getInstance().addError(ErrorType.i,tokenList.ahead(-1).getTokenLine());
                }
            }
            case RBRACK -> {
                if(tokenList.getToken().getType().equals(TokenType.RBRACK)){
                    Token token = tokenList.consume();
                    tmpToken = tokenList.getToken();
                    return token;
                }
                else{
                    ErrorHandler.getInstance().addError(ErrorType.k,tokenList.ahead(-1).getTokenLine());
                }
            }
            case RPARENT -> {
                if(tokenList.getToken().getType().equals(TokenType.RPARENT)){
                    Token token = tokenList.consume();
                    tmpToken = tokenList.getToken();
                    return token;
                }
                else{
                    ErrorHandler.getInstance().addError(ErrorType.j,tokenList.ahead(-1).getTokenLine());
                }
            }
            default -> {
                tmpToken = tokenList.getToken();
            }
        }
        return null;
    }

    private boolean isExp() {
        return  tokenList.getToken().getType() == TokenType.IDENFR ||
                tokenList.getToken().getType() == TokenType.PLUS ||
                tokenList.getToken().getType() == TokenType.MINU ||
                tokenList.getToken().getType() == TokenType.NOT ||
                tokenList.getToken().getType() == TokenType.LPARENT ||
                tokenList.getToken().getType() == TokenType.INTCON;
    }
}
