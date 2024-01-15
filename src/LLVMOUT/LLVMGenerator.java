package LLVMOUT;
import Frontend.AST.*;
import Frontend.AST.DeclAST.*;
import Frontend.AST.ExpAST.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;


public class LLVMGenerator {
    private ArrayList<IASTNode> stack = new ArrayList<>();
    private HashMap<String,IASTNode> global = new HashMap<>();
    private int currentLevel = 0;
    private static final LLVMGenerator instance = new LLVMGenerator();

    private int regId = 0;


    public static LLVMGenerator getInstance() {
        return instance;
    }

    public void OutPutAll(CompUnitAST compUnitAST) throws IOException {
        File file = new File ("llvm_ir.txt");
        PrintStream ps = new PrintStream(file);
        System.setOut(ps);

        System.out.print("declare i32 @getint()\n" +
                "declare void @putint(i32)\n" +
                "declare void @putch(i32)\n" +
                "declare void @putstr(i8*)\n");
        //! decls
        if(compUnitAST.getDeclASTS()!=null){
            for(DeclAST declAST : compUnitAST.getDeclASTS()){
                //! const decl
                if(declAST.getState()==1){
                    OutPutConstDecl(declAST.getConstDeclAST());
                }
                //! var decl
                else if(declAST.getState() == 2){
                    OutPutVarDecl(declAST.getVarDeclAST());
                }
            }
        }
        //! funcdecls
        if(compUnitAST.getFuncDefASTS()!=null){
            for(FuncDefAST funcDefAST : compUnitAST.getFuncDefASTS()){
                FuncTypeAST funcTypeAST = funcDefAST.getFuncTypeAST();

                String funcType = funcTypeAST.getFuncTypeToken().getVal();
                regId = 0;
//                if(funcType.equals("int")){
                String funcname = funcDefAST.getIdent().getVal();
                global.put(funcname,funcDefAST);
                funcDefAST.funcType = funcType;
                funcDefAST.isFunc = true;
                FuncFParamsAST funcFParamsAST = funcDefAST.getFuncFParamsAST();
                String funcName = funcDefAST.getIdent().getVal();
                if(funcType.equals("int"))  System.out.print("define dso_local i32 @"+funcName+"(");
                else System.out.print("define dso_local void @"+funcName+"(");

                //! state = 2 mean have funcFParams
                if(funcDefAST.getState()==2){
                    ArrayList<FuncFParamAST> funcFParamASTS = funcFParamsAST.getFuncFParamASTS();

                    for(int i=0;i<funcFParamASTS.size();i++){
                        FuncFParamAST funcFParamAST = funcFParamASTS.get(i);
                        String ident = funcFParamAST.getIdent().getVal();
                        IASTNode iastNode = new IASTNode();
                        iastNode.setRegID(regId++);
                        iastNode.setReturnType("int");

                        if(i!=funcFParamASTS.size()-1){
                            System.out.print("i32 "+iastNode.getPrintLLVM() +", ");
                        }
                        else{
                            System.out.print("i32 "+iastNode.getPrintLLVM());
                        }
                    }
                    System.out.print("){\n");
                    BlockAST blockAST = funcDefAST.getBlockAST();
                    regId++;

                    // ! 虽然但是这样子非常的丑陋，请你一定认清这一点
                    for(int i=0;i<funcFParamASTS.size();i++){
                        System.out.println("    %"+(regId+i) + " = " + "alloca " + "i32");
                    }
                    // store i32 %0, i32* %3
                    for(int i=0;i<funcFParamASTS.size();i++){
                        System.out.println("    store i32 %"+ i + ", i32* %"  + (i+regId));
                    }

                    for(int i=0;i<funcFParamASTS.size();i++){
                        FuncFParamAST funcFParamAST = funcFParamASTS.get(i);
                        String ident = funcFParamAST.getIdent().getVal();
                        IASTNode iastNode = new IASTNode();
                        iastNode.setRegID(regId++);
                        iastNode.setReturnType("int");
                        iastNode.setLevel(currentLevel+1);
                        stack.add(iastNode);
                        iastNode.setIdent(funcFParamAST.getIdent());
                    }

                    OutPutBlock(blockAST);

                    if(funcType.equals("void")) System.out.println("    " + "ret void");
                    System.out.println("}");
                }
                else{
                    System.out.print("){\n");
                    BlockAST blockAST = funcDefAST.getBlockAST();
                    regId++;
                    OutPutBlock(blockAST);

                    if(funcType.equals("void")) System.out.println("    " + "ret void");

                    System.out.println("}");
                }

//                }
//                else if(funcType.equals("void")){
//
//                }
            }
            regId = 0;
        }

        if(compUnitAST.getMainFuncDefAST()!=null){
            // ! print main
            MainFuncDefAST mainFuncDefAST = compUnitAST.getMainFuncDefAST();
            regId++;
            OutPutMainFuncDef(mainFuncDefAST);

        }


    }

    private void OutPutMainFuncDef(MainFuncDefAST mainFuncDefAST){
        //! define dso_local i32 @main() {
        System.out.println("define dso_local i32 @main(){");
        //! print main content
        if(mainFuncDefAST.getBlockAST()!=null){
            OutPutBlock(mainFuncDefAST.getBlockAST());
        }
        //! print

        System.out.println("}");
    }

    private void OutPutBlock(BlockAST blockAST){
        currentLevel++;
        if(blockAST.getBlockItemASTS()!=null){
            for(BlockItemAST blockItemAST   : blockAST.getBlockItemASTS()){
                OutPutBlockItem(blockItemAST);
            }
        }
        removeStackLevel(currentLevel);
        currentLevel--;
    }

    private void OutPutBlockItem(BlockItemAST blockItemAST){
        if(blockItemAST.getStmtAST()!=null){
            OutPutStmt(blockItemAST.getStmtAST());
        }
        else if(blockItemAST.getDeclAST()!=null){
            OutPutDecl(blockItemAST.getDeclAST());
        }
    }

    private void OutPutStmt(StmtAST stmtAST){
        //! step 1 main return
        switch (stmtAST.getState()){
            //return
            case 1->{
                // Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
//                System.out.println("good afternoon!");
                LValAST lValAST = stmtAST.getlValAST();
                ExpAST expAST = stmtAST.getExpAST();
                OutPutExp(expAST);
                String ident = lValAST.getIdent().getVal();
                IASTNode iastNode = getInStack(ident);
                if(iastNode==null){
                    iastNode = global.get(ident);
                }
                if(iastNode==null){

                }else{
                    // TODO 重构逻辑
                    if(iastNode.isGlobal){
                        if(iastNode instanceof ConstDefAST){
                            ConstDefAST constDefAST = (ConstDefAST)iastNode;
                            System.out.println("    store i32 " +expAST.getPrintLLVM() + ", i32* " + constDefAST.getPrintLLVM());
                        }else if(iastNode instanceof VarDefAST){
                            VarDefAST varDefAST = (VarDefAST)iastNode;
                            System.out.println("    store i32 " +expAST.getPrintLLVM() + ", i32* " + varDefAST.getPrintLLVM());
                        }
                    }else{
                        if(iastNode instanceof ConstDefAST){
                            ConstDefAST constDefAST = (ConstDefAST)iastNode;
                            System.out.println("    store i32 " +expAST.getPrintLLVM() + ", i32* " + constDefAST.getPrintLLVM());
                        }else if(iastNode instanceof VarDefAST){
                            VarDefAST varDefAST = (VarDefAST)iastNode;
                            System.out.println("    store i32 " + expAST.getPrintLLVM() + ", i32* " +varDefAST.getPrintLLVM());
                        }
                    }
                }

            }
            case 2->{
            }
            case 3->{
                ExpAST expAST = stmtAST.getExpAST();
                OutPutExp(expAST);
            }
            case 4->{
                BlockAST blockAST = stmtAST.getBlockAST();
                OutPutBlock(blockAST);
            }
            //!if
            case 5->{
                System.out.println("    br label %"+(regId+3));
                int YesID = regId+1;
                int NoID = -1;
                int RetID = regId+2;
                regId+=3;
                CondAST condAST = stmtAST.getCondAST();
                OutPutCond(condAST,true,YesID,NoID,RetID);


                StmtAST ifstmtAST = stmtAST.getIfStmtAST();
                StmtAST elsestmtAST = stmtAST.getElseStmtAST();

                System.out.println(YesID+": ifstmt");
                OutPutStmt(ifstmtAST);
                System.out.println("    br label %"+RetID);

                System.out.println(RetID+": runout if else");
            }
            //!YesID=now+1 NoID=nowID+2 RetID=nowID+3 CondID=nowID
            //!Echo 的做法是提供一个bock，这个跳转后的block专门负责跳转到正常位置
            case 6->{
                System.out.println("    br label %"+(regId+4));
                int YesID = regId+1;
                int NoID = regId+2;
                int RetID = regId+3;
                regId+=4;
                CondAST condAST = stmtAST.getCondAST();
                OutPutCond(condAST,true,YesID,NoID,RetID);


                StmtAST ifstmtAST = stmtAST.getIfStmtAST();
                StmtAST elsestmtAST = stmtAST.getElseStmtAST();

                System.out.println(YesID+": ifstmt");
                OutPutStmt(ifstmtAST);
                System.out.println("    br label %"+RetID);
                System.out.println(NoID+": elsestmt");
                OutPutStmt(elsestmtAST);
                System.out.println("    br label %"+RetID);

                System.out.println(RetID+": runout if else");

            }
            case 7->{
            }
            case 8->{
            }
            case 9->{
            }
            case 10->{
            }
            case 11->{
                //!扔到def里面去print了，
            }
            //return exp
            case 12->{
                ExpAST expAST =stmtAST.getExpAST();
                OutPutExp(stmtAST.getExpAST());
                System.out.println("    " + "ret i32 "+ stmtAST.getExpAST().getPrintLLVM());
            }
            //!getint
            case 13->{
                LValAST lValAST = stmtAST.getlValAST();
                String ident = lValAST.getIdent().getVal();
                IASTNode iastNode = getInStack(ident);


                if(iastNode == null){
                    iastNode = global.get(ident);
                }
                //! 同一个@ 同一个% 能否store多次
                //! const的变量其实不会变，所以少些一点
                if(iastNode instanceof VarDefAST){
                    VarDefAST varDefAST = (VarDefAST)iastNode;
                    System.out.println("    "+"%"+regId+" = call i32 @getint()");
                    System.out.println("    store i32 %" + regId+", i32* "+varDefAST.getPrintLLVM());
                    regId++;
                }else{}


            }
            //!print
            case 14->{
                String fmstr = stmtAST.getFormatString().getFstr();
                char[] strarray = fmstr.toCharArray();
                for(int i=1;i<strarray.length-1;i++){
                    if(strarray[i]=='\\'&&strarray[i+1]=='n'){
                        System.out.println("    call void @putch(i32 "+10+")");
                        i+=1;
                        continue;
                    }
                    System.out.println("    call void @putch(i32 "+(int)strarray[i]+")");
                }
            }
            //!print exps
            case 15->{
                String fmstr = stmtAST.getFormatString().getFstr();
                char[] strarray = fmstr.toCharArray();
                ArrayList<ExpAST> exps = stmtAST.getExpASTS();
                int expIdx = 0;
                for(int i=1;i<strarray.length-1;i++){
                    if(strarray[i]=='\\'&&strarray[i+1]=='n'){
                        System.out.println("    call void @putch(i32 "+10+")");
                        i+=1;
                        continue;
                    }
                    if(strarray[i]=='%'&&strarray[i+1]=='d'){
                        i++;
                        //!我这里如果是global会先取出来到%里面，然后再输出，虽然似乎没什么区别
                        OutPutExp(exps.get(expIdx));
                        System.out.println("    call void @putint(i32 "+exps.get(expIdx++).getPrintLLVM()+")");

                    }
                    else System.out.println("    call void @putch(i32 "+(int)strarray[i]+")");
                }
            }
        }
    }

//    private void OutPutFuncFParams(){
//
//    }
//    private void OutPutFuncFParam(){
//
//    }

    private void OutPutExp(ExpAST expAST){
//        expAST.setRegID(regId++);
        //! step 1 return
        if(expAST.getAddExpAST()!=null){
            if(currentLevel>0){
                AddExpAST addExpAST = expAST.getAddExpAST();
                OutPutAddExp(expAST.getAddExpAST());
                expAST.setRegID(addExpAST.getRegID());
                expAST.setReturnType(addExpAST.getReturnType());
                expAST.setReturnValue(addExpAST.getReturnValue());
            }
            else{
                OutPutAddExp(expAST.getAddExpAST());
                expAST.setRegID(expAST.getAddExpAST().getRegID());
                expAST.setReturnType("i32");
                expAST.setReturnValue(expAST.getAddExpAST().getReturnValue());
            }
        }
    }

    //! add Exp
    private void OutPutAddExp(AddExpAST addExpAST){
        if(addExpAST.getAddExpAST()!=null){
            if(currentLevel>0){
                //! 如果进行operate了，那一定就要存一个寄存器
                OutPutMulExp(addExpAST.getMulExpAST());
                OutPutAddExp(addExpAST.getAddExpAST());

                addExpAST.setRegID(regId++);
                addExpAST.setReturnType("int");
                //! add i32
                System.out.println( "    " +addExpAST.getPrintLLVM()+ " = "+ Operator(addExpAST.getOp()) +" i32 "+
                        addExpAST.getMulExpAST().getPrintLLVM() + ", " + addExpAST.getAddExpAST().getPrintLLVM());
            }
            else{
                MulExpAST mulExpAST1 = addExpAST.getMulExpAST();
                AddExpAST addExpAST1 = addExpAST.getAddExpAST();

                OutPutAddExp(addExpAST1);
                OutPutMulExp(mulExpAST1);
                addExpAST.setReturnType("i32");
                addExpAST.setReturnValue(mathCalculate(addExpAST1.getReturnValue(), addExpAST.getOp(), mulExpAST1.getReturnValue()));
            }

        }
        else{
            //! 否则有可能就直接是数值，所以直接调用下面的赋值
            MulExpAST mulExpAST = addExpAST.getMulExpAST();
            OutPutMulExp(mulExpAST);
            addExpAST.setRegID(mulExpAST.getRegID());
            addExpAST.setReturnValue(mulExpAST.getReturnValue());
            addExpAST.setReturnType(mulExpAST.getReturnType());
//            addExpAST.isGlobal = mulExpAST.isGlobal;
//            addExpAST.globalIdent = mulExpAST.globalIdent;
        }
    }
    private void OutPutMulExp(MulExpAST mulExpAST){

        if(mulExpAST.getMulExpAST()!=null){
            if(currentLevel>0){
                //! 如果进行operate了，那一定就要存一个寄存器
                UnaryExpAST unaryExpAST = mulExpAST.getUnaryExpAST();
                MulExpAST mulExpAST1 = mulExpAST.getMulExpAST();

                OutPutUnaryExp(unaryExpAST);
                OutPutMulExp(mulExpAST1);


                mulExpAST.setRegID(regId++);
                mulExpAST.setReturnType("int");
                mulExpAST.setReturnValue("");
                System.out.println("    " +  mulExpAST.getPrintLLVM()+ " = "+ Operator(mulExpAST.getOp()) + " i32 " +
                        mulExpAST.getUnaryExpAST().getPrintLLVM() + ", " + mulExpAST.getMulExpAST().getPrintLLVM());
            }
            else{

                UnaryExpAST unaryExpAST = mulExpAST.getUnaryExpAST();
                MulExpAST mulExpAST1 = mulExpAST.getMulExpAST();

                OutPutUnaryExp(unaryExpAST);
                OutPutMulExp(mulExpAST1);

                mulExpAST.setReturnType("i32");
                mulExpAST.setReturnValue(mathCalculate(mulExpAST1.getReturnValue(), mulExpAST.getOp(), unaryExpAST.getReturnValue()));
            }

        }else{
            //! 否则有可能就直接是数值，所以直接调用下面的赋值
            UnaryExpAST unaryExpAST = mulExpAST.getUnaryExpAST();
            OutPutUnaryExp(unaryExpAST);
            mulExpAST.setRegID(unaryExpAST.getRegID());
            mulExpAST.setReturnValue(unaryExpAST.getReturnValue());
            mulExpAST.setReturnType(unaryExpAST.getReturnType());
//            mulExpAST.isGlobal = unaryExpAST.isGlobal;
//            mulExpAST.globalIdent = unaryExpAST.globalIdent;
        }

    }
    private void OutPutUnaryExp(UnaryExpAST unaryExpAST){
        //! -> PrimaryExp
        if(unaryExpAST.getPrimaryExpAST()!=null){
            PrimaryExpAST primaryExpAST = unaryExpAST.getPrimaryExpAST();
            OutPutPrimaryExp(primaryExpAST);
            unaryExpAST.setRegID(primaryExpAST.getRegID());
            unaryExpAST.setReturnValue(primaryExpAST.getReturnValue());
            unaryExpAST.setReturnType(primaryExpAST.getReturnType());
//            unaryExpAST.isGlobal = primaryExpAST.isGlobal;
//            unaryExpAST.globalIdent = primaryExpAST.globalIdent;
        }
        //! -> UnaryOp UnaryExp
        else if(unaryExpAST.getUnaryOpAST()!=null)
        {
            if(currentLevel>0){
                //! unaryOp is about ±，我觉得直接用0+ 0-来代替好了
                OutPutUnaryExp(unaryExpAST.getUnaryExpAST());
                unaryExpAST.setRegID(regId++);
                unaryExpAST.setReturnType("int");
                System.out.println( "    " +unaryExpAST.getPrintLLVM() + " " + Operator(unaryExpAST.getUnaryOpAST().getOperator()) + " 0, "
                        + unaryExpAST.getUnaryExpAST().getPrintLLVM());
            }
            else{
//                AddExpAST addExpAST1 = addExpAST.getAddExpAST();
//                MulExpAST mulExpAST1 = addExpAST.getMulExpAST();
//                addExpAST.setReturnType("i32");
//                addExpAST.setReturnValue(mathCalculate(addExpAST1.getReturnValue(), addExpAST.getOp(), mulExpAST1.getReturnValue()));
                UnaryOpAST unaryOp1 = unaryExpAST.getUnaryOpAST();
                UnaryExpAST unaryExpAST1 = unaryExpAST.getUnaryExpAST();
                OutPutUnaryExp(unaryExpAST1);
                unaryExpAST.setReturnType("i32");
                if(unaryOp1.getOperator().equals("-")){
                    unaryExpAST.setReturnValue(mathCalculate("0",unaryOp1.getOperator(),unaryExpAST1.getReturnValue()));
                }
                else unaryExpAST.setReturnValue(unaryExpAST1.getReturnValue());
            }
        }
        //! -> it is function
        else if(unaryExpAST.getIdent()!=null){
            String tmpfuncname = global.get(unaryExpAST.getIdent().getVal()).funcType;
            if(tmpfuncname.equals("int")){
                if(unaryExpAST.getFuncRParamsAST()!=null){
                    FuncRParamsAST funcRParamsAST = unaryExpAST.getFuncRParamsAST();
                    int len =funcRParamsAST.getExpASTS().size();
                    ArrayList<ExpAST> expASTS = funcRParamsAST.getExpASTS();
                    String funcName = unaryExpAST.getIdent().getVal();
                    //!这里暂时直接用funcName，但是具体链接还没有做
                    for(int i=0;i<len;i++){
                        OutPutExp(expASTS.get(i));
                    }
                    unaryExpAST.setReturnType("int");
                    unaryExpAST.setRegID(regId++);
                    unaryExpAST.setReturnValue("");
                    System.out.print("    "+unaryExpAST.getPrintLLVM()+" = call i32 "+"@"+funcName+"(");
                    for(int i=0;i<len;i++){
                        if(i!=len-1) System.out.print("i32 "+expASTS.get(i).getPrintLLVM()+", ");
                        else System.out.print("i32 "+expASTS.get(i).getPrintLLVM());
                    }
                    System.out.print(")\n");
                }
                else{
                    unaryExpAST.setReturnType("int");
                    unaryExpAST.setRegID(regId++);
                    unaryExpAST.setReturnValue("");
                    String funcName = unaryExpAST.getIdent().getVal();
                    System.out.print("    "+unaryExpAST.getPrintLLVM()+" = call i32 "+"@"+funcName+"(");
                    System.out.print(")\n");
                }


            }
            else{

                if(unaryExpAST.getFuncRParamsAST()!=null){
                    FuncRParamsAST funcRParamsAST = unaryExpAST.getFuncRParamsAST();
                    int len =funcRParamsAST.getExpASTS().size();
                    ArrayList<ExpAST> expASTS = funcRParamsAST.getExpASTS();
                    String funcName = unaryExpAST.getIdent().getVal();
                    //!这里暂时直接用funcName，但是具体链接还没有做
                    for(int i=0;i<len;i++){
                        OutPutExp(expASTS.get(i));
                    }
//                    unaryExpAST.setReturnType("int");
//                    unaryExpAST.setRegID(regId++);
//                    unaryExpAST.setReturnValue("");
                    System.out.print("    "+"call void "+"@"+funcName+"(");
                    for(int i=0;i<len;i++){
                        if(i!=len-1) System.out.print("i32 "+expASTS.get(i).getPrintLLVM()+", ");
                        else System.out.print("i32 "+expASTS.get(i).getPrintLLVM());
                    }
                    System.out.print(")\n");
                }
                else{
//                    unaryExpAST.setReturnType("int");
//                    unaryExpAST.setRegID(regId++);
//                    unaryExpAST.setReturnValue("");
                    String funcName = unaryExpAST.getIdent().getVal();
                    System.out.print("    "+"call void "+"@"+funcName+"(");
                    System.out.print(")\n");
                }
            }

        }
    }
    private void OutPutPrimaryExp(PrimaryExpAST primaryExpAST){

        //! step 1 return
        switch (primaryExpAST.getState()){
            case 1->{
                ExpAST expAST = primaryExpAST.getExpAST();
                OutPutExp(expAST);
                primaryExpAST.setRegID(expAST.getRegID());
                primaryExpAST.setReturnValue(expAST.getReturnValue());
                primaryExpAST.setReturnType(expAST.getReturnType());
            }
            //! primary -> LVal
            case 2->{
                LValAST lValAST = primaryExpAST.getlValAST();
//                OutPutLval(lValAST);
                String ident = lValAST.getIdent().getVal();
                IASTNode iastNode = getInStack(ident);
                if(iastNode==null){
                    iastNode = global.get(ident);
                }
                if(iastNode==null){
                    System.out.println("where are you " + ident);
                }
                else{
                    if(currentLevel > 0){
//                        ConstDefAST constDefAST = (ConstDefAST) iastNode;
                        primaryExpAST.setRegID(regId++);
                        //! 取值不需要改变
//                            constDefAST.setRegID(primaryExpAST.getRegID());
                        primaryExpAST.setReturnType("int");
                        primaryExpAST.setReturnValue(iastNode.getReturnValue());
                        System.out.println("    "+primaryExpAST.getPrintLLVM() +  " = load i32, i32* " + iastNode.getPrintLLVM());
//                        if(iastNode instanceof ConstDefAST){
//                            ConstDefAST constDefAST = (ConstDefAST) iastNode;
//                            primaryExpAST.setRegID(regId++);
//                            //! 取值不需要改变
////                            constDefAST.setRegID(primaryExpAST.getRegID());
//                            primaryExpAST.setReturnType("int");
//                            primaryExpAST.setReturnValue(constDefAST.getReturnValue());
//                            System.out.println("    "+primaryExpAST.getPrintLLVM() +  " = load i32, i32* " + constDefAST.getPrintLLVM());
////                        primaryExpAST.isGlobal = constDefAST.isGlobal;
////                        primaryExpAST.globalIdent = constDefAST.getIdent().getVal();
//                        }else{
//                            VarDefAST varDefAST = (VarDefAST) iastNode;
//                            primaryExpAST.setRegID(regId++);
//                            //! 取值不需要改变
////                            varDefAST.setRegID(primaryExpAST.getRegID());
//                            primaryExpAST.setReturnValue("int");
//                            primaryExpAST.setReturnType(varDefAST.getReturnType());
//                            System.out.println("    "+primaryExpAST.getPrintLLVM() +  " = load i32, i32* " + varDefAST.getPrintLLVM());
////                        primaryExpAST.isGlobal = varDefAST.isGlobal;
////                        primaryExpAST.globalIdent = varDefAST.getIdent().getVal();
//                        }
                    }
                    else{
                        if(iastNode instanceof ConstDefAST){
                            ConstDefAST constDefAST = (ConstDefAST) iastNode;
                            primaryExpAST.setRegID(constDefAST.getRegID());
                            primaryExpAST.setReturnType(constDefAST.getReturnType());
                            primaryExpAST.setReturnValue(constDefAST.getReturnValue());
                        }
                        else{
                            VarDefAST varDefAST = (VarDefAST) iastNode;
                            primaryExpAST.setRegID(varDefAST.getRegID());
                            primaryExpAST.setReturnType(varDefAST.getReturnType());
                            primaryExpAST.setReturnValue(varDefAST.getReturnValue());
                        }
                    }
//                    System.out.println("I find you" + ident);
                }
                //! LVal


            }
            case 3->{
                Integer number = primaryExpAST.getNumberAST().getIntConst();
                OutPutNumber(primaryExpAST.getNumberAST());
                primaryExpAST.setReturnType("i32");
                primaryExpAST.setReturnValue(number.toString());
            }
        }

    }
    private void OutPutLval(LValAST lValAST){

    }
    private void OutPutNumber(NumberAST numberAST){
        //! step 1 return
    }


    //! Decl         → ConstDecl | VarDecl
    public void OutPutDecl(DeclAST declAST){
        switch (declAST.getState()){
            case 1->{
                OutPutConstDecl(declAST.getConstDeclAST());
            }
            case 2->{
                OutPutVarDecl(declAST.getVarDeclAST());
            }
        }
    }

    //! ConstDecl    → 'const' BType ConstDef { ',' ConstDef } ';'
    public void OutPutConstDecl(ConstDeclAST constDeclAST){
        if(constDeclAST.getConstDefASTS()!=null){
            for(ConstDefAST constDefAST: constDeclAST.getConstDefASTS()){
                OutPutConstDef(constDefAST);
            }
        }
    }
    // TODO 符号表好好处理，
    //! ConstDef     → Ident  '=' ConstInitVal
    public void OutPutConstDef(ConstDefAST constDefAST){
        if(currentLevel == 0){
            //!暂时咱不考虑
            //!考虑initval没有，那就初始化设置为0,当然constdef里面本身就肯定有initval的文法
            ConstInitValAST constInitValAST = constDefAST.getConstInitValAST();
            if(constInitValAST!=null){
                OutPutConstInitVal(constInitValAST);
                System.out.println("@"+ constDefAST.getIdent().getVal() + " = dso_local constant i32 "+constInitValAST.getPrintLLVM());
                constDefAST.setReturnType("int");
                constDefAST.setReturnValue(constInitValAST.getReturnValue());
            }

            global.put(constDefAST.getIdent().getVal(),constDefAST);
            constDefAST.isGlobal = true;
            constDefAST.globalIdent = constDefAST.getIdent().getVal();
        }
        else{
            constDefAST.setRegID(regId++);
            constDefAST.setReturnType("int");
            constDefAST.setLevel(currentLevel);
            System.out.println("    %"+constDefAST.getRegID()+ " = alloca i32");
            ConstInitValAST constInitValAST = constDefAST.getConstInitValAST();
            OutPutConstInitVal(constInitValAST);
            constDefAST.setReturnValue(constInitValAST.getReturnValue());
            System.out.println("    store i32 " + constInitValAST.getPrintLLVM() + ", i32* " + constDefAST.getPrintLLVM());
            stack.add(constDefAST);
        }
        constDefAST.setLevel(currentLevel);
    }
    //! ConstInitVal → ConstExp
    public void OutPutConstInitVal(ConstInitValAST constInitValAST){
        ConstExpAST constExpAST = constInitValAST.getConstExpAST();
        OutPutConstExp(constExpAST);
        constInitValAST.setReturnValue(constExpAST.getReturnValue());
        constInitValAST.setReturnType(constExpAST.getReturnType());
        constInitValAST.setRegID(constExpAST.getRegID());
    }
    //! ConstExp     → AddExp
    public void OutPutConstExp(ConstExpAST constExpAST){
        AddExpAST addExpAST = constExpAST.getAddExp();
        OutPutAddExp(addExpAST);
        constExpAST.setRegID(addExpAST.getRegID());
        constExpAST.setReturnType(addExpAST.getReturnType());
        constExpAST.setReturnValue(addExpAST.getReturnValue());
    }

    //! VarDef       → Ident | Ident '=' InitVal
    public void OutPutVarDecl(VarDeclAST varDeclAST){
        if(varDeclAST.getVarDefs()!=null){
            for(VarDefAST varDefAST : varDeclAST.getVarDefs()){
                OutPutVarDef(varDefAST);
            }
        }
    }
    //! VarDef       → Ident | Ident '=' InitVal
    public void OutPutVarDef(VarDefAST varDefAST){
        if(currentLevel == 0){
            InitValAST initValAST = varDefAST.getInitValAST();
            varDefAST.setReturnType("int");
            if(initValAST!=null){
                OutPutInitVal(initValAST);
                varDefAST.setReturnValue(initValAST.getReturnValue());
                System.out.println("@"+ varDefAST.getIdent().getVal() + " = dso_local global i32 "+initValAST.getPrintLLVM());

            }else{
                varDefAST.setReturnValue("0");
                System.out.println("@"+ varDefAST.getIdent().getVal() + " = dso_local global i32 "+"0");
            }
            global.put(varDefAST.getIdent().getVal(),varDefAST);
            varDefAST.isGlobal = true;
            varDefAST.globalIdent = varDefAST.getIdent().getVal();
        }
        else{
            varDefAST.setRegID(regId++);
            varDefAST.setReturnType("int");
            varDefAST.setLevel(currentLevel);
            System.out.println("    %"+varDefAST.getRegID()+ " = alloca i32");
            InitValAST initValAST = varDefAST.getInitValAST();
            if(initValAST!=null){
                OutPutInitVal(initValAST);
                varDefAST.setReturnValue(initValAST.getReturnValue());
                System.out.println("    store i32 " + initValAST.getPrintLLVM() + ", i32* " + varDefAST.getPrintLLVM());
            }else{
                varDefAST.setReturnValue("0");
            }

            stack.add(varDefAST);
        }
        varDefAST.setLevel(currentLevel);
    }
    //! InitVal      → Exp
    public void OutPutInitVal(InitValAST initValAST){
        ExpAST expAST = initValAST.getExpAST();
        OutPutExp(expAST);
        initValAST.setReturnType(expAST.getReturnType());
        initValAST.setReturnValue(expAST.getReturnValue());
        initValAST.setRegID(expAST.getRegID());
    }



    //! OutPut  Cond
    public void OutPutCond(CondAST condAST,boolean haveElse,int YesID,int NoID,int RetID){
        LOrExpAST lOrExpAST=condAST.getlOrExpAST();
        OutPutLOrExp(lOrExpAST,YesID,NoID,RetID);
    }

    public void OutPutLOrExp(LOrExpAST lOrExpAST,int YesID,int NoID,int RetID){
        ArrayList<LOrExpAST> lOrExpASTs = new ArrayList<LOrExpAST>();
        lOrExpASTs.add(lOrExpAST);
        LOrExpAST tmpLOrExpAST = lOrExpAST;
        while (tmpLOrExpAST.getlOrExpAST()!=null){
            lOrExpASTs.add(tmpLOrExpAST.getlOrExpAST());
            tmpLOrExpAST = tmpLOrExpAST.getlOrExpAST();
        }
        //!在最后一个lOrExpASTs
        for (int i = 0; i < lOrExpASTs.size(); i++) {
            LOrExpAST orExpAST = lOrExpASTs.get(i);
            LAndExpAST lAndExpAST = orExpAST.getlAndExpAST();
            System.out.println(regId++ + ":");
            if(i==lOrExpASTs.size()-1){
                //!todo 这里应该改返回的跳转
                OutPutLAndExp(lAndExpAST, YesID, NoID,RetID);
            }
            else OutPutLAndExp(lAndExpAST, YesID, NoID,RetID);
            System.out.println("    %"+ regId++ +" = icmp ne i32 "+lAndExpAST.getPrintLLVM()+", 0");
            if(NoID==-1){
                System.out.println("    br i1 %"+(regId-1)+", label %"+YesID+", label %"+(regId));
            }else{
                System.out.println("    br i1 %"+(regId-1)+", label %"+YesID+", label %"+(regId));
            }
        }
    }

    public void OutPutLAndExp(LAndExpAST lAndExpAST,int YesID,int NoID,int RetID){
        ArrayList<LAndExpAST> lAndExpASTs = new ArrayList<LAndExpAST>();
        lAndExpASTs.add(lAndExpAST);
        LAndExpAST tmpLAndExpASt = lAndExpAST;
        while (tmpLAndExpASt.getlAndExp()!=null){
            lAndExpASTs.add(tmpLAndExpASt.getlAndExp());
            tmpLAndExpASt = tmpLAndExpASt.getlAndExp();
        }
        for (int i = 0;i<lAndExpASTs.size();i++) {
            LAndExpAST llandExpAST = lAndExpASTs.get(i);
            EqExpAST eqExpAST = llandExpAST.getEqExpAST();
            OutPutEqExp(eqExpAST);
            llandExpAST.setRegID(eqExpAST.getRegID());
            llandExpAST.setReturnValue(eqExpAST.getReturnValue());
            llandExpAST.setReturnType(eqExpAST.getReturnType());
        }
    }

    public void OutPutEqExp(EqExpAST eqExpAST){
        if(eqExpAST.getEqExpAST()!=null){
            EqExpAST eqExpAST1 = eqExpAST.getEqExpAST();
            RelExpAST relExpAST = eqExpAST.getRelExpAST();
            OutPutEqExp(eqExpAST1);
            OutPutRelExp(relExpAST);
            System.out.println("    %"+ regId +" = icmp " + Operator(eqExpAST.getOp())+ " i32 "+
                    eqExpAST1.getPrintLLVM()+", "+relExpAST.getPrintLLVM());
            regId++;
            System.out.println("    %"+regId+" = zext i1 %"+(regId-1)+" to i32");
            eqExpAST.setRegID(regId++);
            eqExpAST.setReturnType("int");
            eqExpAST.setReturnValue("");
        }else{
            RelExpAST relExpAST = eqExpAST.getRelExpAST();
            OutPutRelExp(relExpAST);
            eqExpAST.setRegID(relExpAST.getRegID());
            eqExpAST.setReturnType(relExpAST.getReturnType());
            eqExpAST.setReturnValue(relExpAST.getReturnValue());
        }
    }

    public void OutPutRelExp(RelExpAST relExpAST){
        if(relExpAST.getRelExp()!=null){
            RelExpAST relExpAST1 = relExpAST.getRelExp();
            AddExpAST addExpAST = relExpAST.getAddExp();
            OutPutRelExp(relExpAST1);
            OutPutAddExp(addExpAST);
            System.out.println("    %"+ regId++ +" = icmp " + Operator(relExpAST.getOp())+ "i32 "+
                    relExpAST1.getPrintLLVM()+", "+addExpAST.getPrintLLVM());
            relExpAST.setRegID(regId++);
            relExpAST.setReturnType("int");
            relExpAST.setReturnValue("");
        }else{
            AddExpAST addExpAST = relExpAST.getAddExp();
            OutPutAddExp(addExpAST);
            relExpAST.setRegID(addExpAST.getRegID());
            relExpAST.setReturnValue(addExpAST.getReturnValue());
            relExpAST.setReturnType(addExpAST.getReturnType());
        }
    }





    //!-------------------------作为工具使用---------------------------------------
    public String Operator(String op){
        String opt="";
        switch(op){
            case "+": opt="add";break;
            case "-": opt="sub";break;
            case "*": opt="mul";break;
            case "/": opt="sdiv";break;
            case "%": opt="srem";break;
            case "==": opt="eq";break;
            case "!=": opt="ne";break;
            case ">": opt="sgt";break;
            case ">=": opt="sge";break;
            case "<": opt="slt";break;
            case "<=": opt="sle";break;
            case "&&": opt="and";break;
            case "||": opt="or";break;
            //TODO !
            case "!": opt="!todo";break;
        }
        return opt;
    }

    public String mathCalculate(String left,String op,String right){
        int a=Integer.parseInt(left);
        int b=Integer.parseInt(right);
        int ans=0;
        switch(op){
            case "+":ans=a+b;break;
            case "-":ans=a-b;break;
            case "*":ans=a*b;break;
            case "/":ans=a/b;break;
            case "%":ans=a%b;break;
            case "==": ans=(a==b)?1:0;break;
            case "!=": ans=(a!=b)?1:0;break;
            case ">": ans=(a>b)?1:0;break;
            case ">=": ans=(a>=b)?1:0;break;
            case "<": ans=(a<b)?1:0;break;
            case "<=": ans=(a<=b)?1:0;break;
        }
        return ans+"";
    }

    public String RIGID2RIG(int regId){
        return "%"+regId;
    }

    private IASTNode getInStack(String ident){
        int len = stack.size();
        for(int i=len-1;i>=0;i--){
            IASTNode iastNode = stack.get(i);
            if(iastNode instanceof ConstDefAST){
                ConstDefAST constDefAST = (ConstDefAST)iastNode;
                if(constDefAST.getIdent().getVal().equals(ident))
                    return constDefAST;
//                System.out.println("ConstDef");
            }
            else if(iastNode instanceof VarDefAST){
                VarDefAST varDefAST = (VarDefAST) iastNode;
                if(varDefAST.getIdent().getVal().equals(ident))
                    return varDefAST;
            }
            else{
                //TODO 给iastNode加上ident
                if(iastNode.getIdent().getVal().equals(ident))
                    return iastNode;
            }
        }
        return null;
    }

    private void removeStackLevel(int level){
        stack.removeIf(e->e.getLevel()==level);
    }






}
