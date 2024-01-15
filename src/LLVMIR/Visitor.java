package LLVMIR;
import Frontend.AST.*;
import Frontend.AST.DeclAST.*;
import Frontend.AST.ExpAST.*;
import LLVMIR.Type.*;
import LLVMIR.Value.*;
import LLVMIR.Value.Instruction.*;
import Utils.DataStruct.IList;
import com.sun.jdi.FloatType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Visitor {
    private final IRBuildFactory f = IRBuildFactory.getInstance();
    private Function CurFunction;

    private BasicBlock CurBasicBlock;
    private ArrayList<Value> fillInitVal = new ArrayList<>();
    private Value CurValue;
    private OP CurOP;
    private IRModule module;

    private boolean isGlobal = true;


    private int isFuncRParam = 0;


    // region 符号表
    //!-----符号表-----------------------------------------------------------
    private final ArrayList<GlobalVar> globalVars = new ArrayList<>();

    private final ArrayList<HashMap<String, Value>> symTbls = new ArrayList<>();
    //  tmpHashMap用于保存FuncFParams
    //  因为当你访问FuncFParams时，你还没有进入Block，而只有进入Block你才能push新的符号表
    //  所以为了把FuncFParams的声明也放进符号表，我们用tmpHashMap来保存
    private final HashMap<String, Value> tmpHashMap = new HashMap<>();

    //  用于记录标识符出现的次数，以防不同block定义的变量构建Value时重名
    private final HashMap<String, Integer>symCnt = new HashMap<>();

    private final ArrayList<BasicBlock> whileEntryBLocks = new ArrayList<>();
    private final ArrayList<BasicBlock> whileOutBlocks = new ArrayList<>();

    private int addSymCnt(String ident){
        int cnt = 0;
        if(symCnt.get(ident) == null){
            symCnt.put(ident, 0);
        }
        else{
            cnt = symCnt.get(ident) + 1;
            symCnt.replace(ident, cnt);
        }
        return cnt;
    }

    private void pushWhileEntry(BasicBlock whileEntryBlock){
        whileEntryBLocks.add(whileEntryBlock);
    }

    private void pushWhileOut(BasicBlock whileOutBlock){
        whileOutBlocks.add(whileOutBlock);
    }

    private void popWhileEntry(){
        int len = whileEntryBLocks.size();
        whileEntryBLocks.remove(len - 1);
    }

    private void popWhileOut(){
        int len = whileOutBlocks.size();
        whileOutBlocks.remove(len - 1);
    }

    private BasicBlock getWhileEntry(){
        int len = whileEntryBLocks.size();
        return whileEntryBLocks.get(len - 1);
    }

    private BasicBlock getWhileOut(){
        int len = whileOutBlocks.size();
        return whileOutBlocks.get(len - 1);
    }

    private void pushSymTbl(){
        symTbls.add(new HashMap<>());
    }

    private void popSymTbl(){
        int len = symTbls.size();
        symTbls.remove(len - 1);
    }

    private HashMap<String, Value> getNowSymTbl(){
        int len = symTbls.size();
        return symTbls.get(len - 1);
    }

    private void pushSymbol(String ident, Value value){
        int len = symTbls.size();
        symTbls.get(len - 1).put(ident, value);
    }
    //!-----符号表-----------------------------------------------------------

    // endregion


    // region Module
    public IRModule VisitCompUnit(CompUnitAST compUnitAST){
        ArrayList<Function> functions = new ArrayList<>();

        module = new IRModule(functions, globalVars);
        //  构建全局域
        pushSymTbl();

        ArrayList<DeclAST> declASTS = compUnitAST.getDeclASTS();
        for(DeclAST declAST : declASTS){
            visitDeclAST(declAST, true);
        }



        ArrayList<FuncDefAST> funcDefASTS = compUnitAST.getFuncDefASTS();
        for (FuncDefAST funcDefAST : funcDefASTS) {
            visitFuncDefAST(funcDefAST);
        }

        MainFuncDefAST mainFuncDefAST = compUnitAST.getMainFuncDefAST();
        visitMainFuncDef(mainFuncDefAST);

        return module;
    }
    private void visitFuncDefAST(FuncDefAST funcDefAST){
        String ident = funcDefAST.getIdent().getVal();
        String type = funcDefAST.getFuncTypeAST().getFuncTypeToken().getVal();

        CurFunction = f.buildFunction("@" + ident, type, module);
        CurBasicBlock = f.buildBasicBlock(CurFunction);
        fillInitVal = new ArrayList<>();
        pushSymbol(ident, CurFunction);
        tmpHashMap.clear();

        switch (funcDefAST.getState()){
            case 1->{
                visitBlockAST(funcDefAST.getBlockAST(), true);
                if(funcDefAST.getFuncTypeAST().getFuncTypeToken().getVal().equals("void")){
                    f.buildRetInst(CurBasicBlock);
                }
            }
            case 2->{
                FuncFParamsAST funcFParamsAST = funcDefAST.getFuncFParamsAST();
                ArrayList<FuncFParamAST> funcFParamASTS = funcFParamsAST.getFuncFParamASTS();
                for(FuncFParamAST funcFParamAST: funcFParamASTS){
                    String argumentIdent = funcFParamAST.getIdent().getVal();
                    String argumentType = funcFParamAST.getbType();
                    int cnt = addSymCnt(argumentIdent);
                    String identArg = "%" + argumentIdent + "_" +cnt;
                    switch (funcFParamAST.getState()){
                        case 1->{
                            Argument argument = f.buildArgument(identArg, argumentType, CurFunction);
                            tmpHashMap.put(argumentIdent, argument);
                        }
                        //! 2 is array type
                        case 2->{
                            //  构建dimList
                            ArrayList<Integer> dimList = new ArrayList<>();
                            ArrayList<ConstExpAST> constExpASTS = funcFParamAST.getConstExpASTS();
                            for(ConstExpAST constExpAST : constExpASTS){
                                visitConstExpAST(constExpAST);
                                dimList.add(Integer.parseInt(CurValue.getName()));
                            }

                            Argument argument = f.buildArgument(identArg, dimList, CurFunction);

                            tmpHashMap.put(argumentIdent, argument);
                        }
                    }

                }
                visitBlockAST(funcDefAST.getBlockAST(), true);
                if(funcDefAST.getFuncTypeAST().getFuncTypeToken().getVal().equals("void")){
                    f.buildRetInst(CurBasicBlock);
                }
            }
        }
//        //  Has FuncFParams
//        if(funcDefAST.getType() == 2){
//            FuncFParamsAST funcFParamsAST = funcDefAST.getFuncFParamsAST();
//            ArrayList<FuncFParamAST> funcFParamASTS = funcFParamsAST.getFuncFParamASTS();
//            for(FuncFParamAST funcFParamAST : funcFParamASTS){
//                //  平平无奇的起名环节
//                String rawIdentArg = funcFParamAST.getIdent().getVal();
//
//                String typeArg = funcFParamAST.getbType();
//
//                int cnt = addSymCnt(rawIdentArg);
//                String identArg = "%" + rawIdentArg + "_" + cnt;
//
//                //  平平无奇的普通参数环节
//                if(funcFParamAST.getType() == 1) {
//                    Argument argument = f.buildArgument(identArg, typeArg, CurFunction);
//                    tmpHashMap.put(rawIdentArg, argument);
//                }
//                //  相当有趣的数组参数环节
//                else if(funcFParamAST.getType() == 2){
//                    //  构建dimList
//                    ArrayList<Integer> dimList = new ArrayList<>();
//                    ArrayList<ConstExpAST> constExpASTS = funcFParamAST.getConstExpASTS();
//                    for(ConstExpAST constExpAST : constExpASTS){
//                        visitConstExpAST(constExpAST);
//                        dimList.add(Integer.parseInt(CurValue.getName()));
//                    }
//
//                    Argument argument = f.buildArgument(identArg, dimList, CurFunction);
//
//                    tmpHashMap.put(rawIdentArg, argument);
//                }
//            }
//        }
//
//        visitBlockAST(funcDefAST.getBlockAST(), true);
//
//        //  visitBlock之后，我们要检查一下每个block是否只有一条跳转指令
//        //  不然sb llvm编译过不了(震怒x
//        for(IList.INode<BasicBlock, Function> bbNode : CurFunction.getBbs()){
//            BasicBlock bb = bbNode.getValue();
//            boolean isTerminal = false;
//            for(IList.INode<Instruction, BasicBlock> instNode : bb.getInsts()){
//                Instruction inst = instNode.getValue();
//                if(isTerminal){
//                    inst.removeSelf();
//                }
//                else{
//                    if(inst instanceof RetInst || inst instanceof BrInst){
//                        isTerminal = true;
//                    }
//                }
//            }
//
//            //  如果没有ret语句，构建一个ret void
//            if(!isTerminal){
//                f.buildRetInst(CurBasicBlock);
//            }
//        }
    }

    private void visitMainFuncDef(MainFuncDefAST mainFuncDefAST) {
        isGlobal = false;
        Function function = f.buildFunction("@main","int",module);
        CurFunction =function;
        CurBasicBlock = f.buildBasicBlock(CurFunction);
        fillInitVal = new ArrayList<>();
        pushSymbol("main",CurFunction);
        tmpHashMap.clear();
        visitBlockAST(mainFuncDefAST.getBlockAST(),true);

    }

    private void visitBlockAST(BlockAST blockAST, boolean isEntry){
        pushSymTbl();
        //  当基本块是入口基本块时 构建Alloc指令
        //  再把Alloc的Value放到该函数的HashMap中
        //  为了保证这些参数表示的值和普通的值一样
        if(isEntry) {
            //!暂时没用函数参数
            for (String identArg : tmpHashMap.keySet()) {
                Value argument = tmpHashMap.get(identArg);
                AllocInst allocInst = f.buildAllocInst( argument.getType(), CurBasicBlock);
                f.buildStoreInst( argument, allocInst,CurBasicBlock);
                pushSymbol(identArg, allocInst);
            }
            CurFunction.setBbEntry(CurBasicBlock);
        }

        ArrayList<BlockItemAST> blockItemASTS = blockAST.getBlockItemASTS();
        for(BlockItemAST blockItemAST : blockItemASTS){
            visitBlockItemAST(blockItemAST);
        }

        popSymTbl();
    }
    private void visitBlockItemAST(BlockItemAST blockItemAST){
        if(blockItemAST.getDeclAST()!=null){
            visitDeclAST(blockItemAST.getDeclAST(), false);
        }
        else if(blockItemAST.getStmtAST()!=null){
            visitStmtAST(blockItemAST.getStmtAST());
        }
    }




    public void visitStmtAST(StmtAST stmtAST){
        switch (stmtAST.getState()){
            //! Lval=EXP
            case 1->{
                visitExpAST(stmtAST.getExpAST(),false);
                Value expValue = CurValue;
                //!visit Lval
                visitLvalAST(stmtAST.getlValAST(),false,true);
                f.buildStoreInst(expValue, CurValue,CurBasicBlock);
            }
            //! 2 empty
            case 2->{
            }
            //! 3 exp
            case 3->{visitExpAST(stmtAST.getExpAST(),false);}
            //!Block
            case 4->{
                visitBlockAST(stmtAST.getBlockAST(), false);
            }
            //! if(cond) stmt
            case 5->{
                BasicBlock trueBlock = f.buildBasicBlock(CurFunction);
                BasicBlock nxtBlock = f.buildBasicBlock(CurFunction);
                visitCondAST(stmtAST.getCondAST(), trueBlock, nxtBlock);

                CurBasicBlock = trueBlock;
                visitStmtAST(stmtAST.getIfStmtAST());

                f.buildBrInst(nxtBlock, CurBasicBlock);
                CurBasicBlock = nxtBlock;
            }
            //! if(cond) stmt;else stmt
            case 6->{
                BasicBlock TrueBlock = f.buildBasicBlock(CurFunction);
                BasicBlock FalseBlock = f.buildBasicBlock(CurFunction);
                BasicBlock NxtBlock = f.buildBasicBlock(CurFunction);

                visitCondAST(stmtAST.getCondAST(), TrueBlock, FalseBlock);

                //  构建TrueBlock
                CurBasicBlock = TrueBlock;
                visitStmtAST(stmtAST.getIfStmtAST());

                //  这里原理同上，为CurBlock构建Br指令
                f.buildBrInst(NxtBlock, CurBasicBlock);

                //  开始构建FalseBlock
                CurBasicBlock = FalseBlock;
                visitStmtAST(stmtAST.getElseStmtAST());

                //  原理同上，为CurBLock构建Br指令
                f.buildBrInst(NxtBlock, CurBasicBlock);

                //  最后令CurBlock为NxtBlock
                CurBasicBlock = NxtBlock;
            }
            //! never used
            case 7->{}
            //! for loop
            case 8->{
                ForStmtAST forStmtAST1 = stmtAST.getForStmt1();
                ForStmtAST forStmtAST2 = stmtAST.getForStmt2();
                CondAST condAST = stmtAST.getCondAST();
                StmtAST loopStmt = stmtAST.getStmtAST();

                BasicBlock condBlock = f.buildBasicBlock(CurFunction);
                BasicBlock trueBlock = f.buildBasicBlock(CurFunction);
                BasicBlock falseBlock = f.buildBasicBlock(CurFunction);
                BasicBlock forstmt2Block = f.buildBasicBlock(CurFunction);
                //! init,以后不会用第二次了
                if(forStmtAST1!=null){
                    visitForStmtAST(forStmtAST1,false,CurBasicBlock);
                }
                f.buildBrInst(condBlock,CurBasicBlock );

                //! cond


                pushWhileEntry(forstmt2Block);
                pushWhileOut(falseBlock);

                //! cond cond goto the true or out
                CurBasicBlock = condBlock;
                if(condAST!=null){
                    visitCondAST(condAST,trueBlock,falseBlock);
                }
                f.buildBrInst(trueBlock, CurBasicBlock);


                //! true go to the i++
                CurBasicBlock = trueBlock;
               if(loopStmt!=null){
                   visitStmtAST(loopStmt);
               }
                f.buildBrInst(forstmt2Block, CurBasicBlock);

                //! i++ go to cond
                CurBasicBlock = forstmt2Block;
                if(forStmtAST2!=null){
                    visitForStmtAST(forStmtAST2,false,forstmt2Block);
                }
                f.buildBrInst(condBlock, CurBasicBlock);


                CurBasicBlock = falseBlock;

                //  while内的指令构建完了，出栈
                popWhileEntry();
                popWhileOut();

            }
            //! break
            case 9->{
                if(whileOutBlocks.size() == 0) return;

                BasicBlock whileOutBlock = getWhileOut();
                f.buildBrInst(whileOutBlock, CurBasicBlock);
                CurBasicBlock = f.buildBasicBlock(CurFunction);
            }
            //! continue
            case 10->{
                if(whileEntryBLocks.size() == 0) return;

                BasicBlock whileEntryBlock = getWhileEntry();
                f.buildBrInst(whileEntryBlock, CurBasicBlock);
                CurBasicBlock = f.buildBasicBlock(CurFunction);
            }
            //! return noexp
            case 11->{
                CurValue =f.buildRetInst(CurBasicBlock);
            }
            //! return exp
            case 12->{
                visitExpAST(stmtAST.getExpAST(),false);
                CurValue =f.buildRetInst(CurBasicBlock,CurValue);
            }
            //! = getint()
            case 13->{
                visitLvalAST(stmtAST.getlValAST(), false, true);
                Function getIntFunc = new Function("@getint", new IntegerType(32));
                CallInst callInst = f.buildCallInst(CurBasicBlock, getIntFunc);
                f.buildStoreInst(callInst,CurValue,CurBasicBlock);
            }
            //! printf() no %d
//            case 14->{
//                String formatString = stmtAST.getFormatString().getFstr();
//                char[] chars = formatString.toCharArray();
//                int len = chars.length;
//                for(int i=1;i<len-1;i++){
//                    Function printfFunc = new Function("@putch", new VoidType());
//                    int chNum = (int)chars[i];
//                    Value printchValue = new Value(String.valueOf(chNum),new IntegerType(32));
//                    ArrayList<Value> values = new ArrayList<>();
//                    values.add(printchValue);
//                    f.buildCallInst(CurBasicBlock,printfFunc,values);
//                }
//            }
            //! printf()  %d
            case 14,15->{
                ArrayList<ExpAST> expASTS ;
                if(stmtAST.getExpASTS()!=null){
                    expASTS = stmtAST.getExpASTS();
                }else{
                    expASTS = new ArrayList<>();
                }

                String formatString = stmtAST.getFormatString().getFstr();
                char[] chars = formatString.toCharArray();
                int len = chars.length;
                int expIndex = 0;
                for(int i=1;i<len-1;i++){
                    Function printfFunc = new Function("@putch", new VoidType());
                    ArrayList<Value> values = new ArrayList<>();
                    if(chars[i]=='\\'&&chars[i+1]=='n'){
                        Value printchValue = new Value("10",new IntegerType(32));
                        values.add(printchValue);
                        f.buildCallInst(CurBasicBlock,printfFunc,values);
                        i+=1;
                        continue;
                    }
                    if(chars[i]=='%'&&chars[i+1]=='d'){
                        Function printfInt = new Function("@putint", new VoidType());
                        visitExpAST(expASTS.get(expIndex++),false);
                        Value printintValue = CurValue;
                        CurValue = null;
                        values.add(printintValue);
                        f.buildCallInst(CurBasicBlock,printfInt,values);
                        i++;
                        continue;
                    }
                    int chNum = (int)chars[i];
                    Value printchValue = new Value(String.valueOf(chNum),new IntegerType(32));
                    values.add(printchValue);
                    f.buildCallInst(CurBasicBlock,printfFunc,values);
                }
            }
        }
    }


    private void visitForStmtAST(ForStmtAST forStmtAST,boolean isCondBlock,BasicBlock condBasicBlock){

        visitExpAST(forStmtAST.getExpAST(),false);
        Value expValue = CurValue;
        //!visit Lval
        visitLvalAST(forStmtAST.getlValAST(),false,true);
        f.buildStoreInst(expValue, CurValue,CurBasicBlock);
    }

    private void visitLvalAST(LValAST lValAST,boolean isConst,boolean isPointer){

        //! isConst 表示外面想要的是一个常数值
        //! isPointer 表示令CurValue = pointer
        //! (!isPointer) 就是需要一次Load 或者是 Gep的指令

        if(isConst){
            //! isConst

            Value value = find(lValAST.getIdent().getVal());

            if (value instanceof ConstInteger) {
                ConstInteger constInteger = (ConstInteger) value;
                CurValue = f.buildNumber(constInteger.getVal());
            }
            //  常量数组
            else {
                value = find(lValAST.getIdent().getVal() + ";const");
                ConstArray constArray = (ConstArray) value;
                assert constArray != null;
                ArrayList<Integer> dimList = constArray.getDimList();
                ArrayList<Integer> arrayValues = constArray.getArrayValues();

                int dimLen = dimList.size();
                //  这里tmpMulDim用于辅助计算是arrayValues第?个元素
                int idx = 0;
                int[] tmpMulDim = new int[dimLen];
                for(int i = dimLen - 1; i >= 0; i--){
                    if(i == dimLen - 1) tmpMulDim[i] = 1;
                    else tmpMulDim[i] = dimList.get(i + 1) * tmpMulDim[i + 1];
                }

                ArrayList<ExpAST> expASTS = lValAST.getExpASTS();
                int expLen = expASTS.size();
                for(int i = 0; i < expLen; i++){
                    ExpAST expAST = expASTS.get(i);
                    visitExpAST(expAST, true);
                    int num = Integer.parseInt(CurValue.getName());
                    idx += num * tmpMulDim[i];
                }

                CurValue = f.buildNumber(arrayValues.get(idx));
            }
        }

        else{
            if(lValAST.getState()==1){
                //! // is not an array
                //! maybe a array pointer
                //! maybe just a pointer
                Value value = find(lValAST.getIdent().getVal());
                Type valueType = value.getType();

                //  lVal为FuncRParam
                if(isFuncRParam != 0 && valueType.isArrayType()){
                    if(!isPointer) {
                        CurValue = fetchVal(value);
                    }
                    else CurValue = value;
                }
                //  lVal为变量或常量
                //  为什么这里lVal还有可能是常量呢？
                //  是因为对于一些Exp如i*const_a，我们传进来的isConst是false
                //  但是const_a显然我们应该直接带入值，因此这里也有可能出现常量
                else{
                    if (value instanceof ConstInteger) {
                        ConstInteger constInteger = (ConstInteger) value;
                        CurValue = f.buildNumber(constInteger.getVal());
                    }
                    else {
                        CurValue = value;
                        if (!isPointer) {
                            CurValue = fetchVal(CurValue);
                        }
                    }
                }
            }
            else{
                //! must be an array
                //! is an array, maybe arr[1][2] or arr[][2]
                Value value = find(lValAST.getIdent().getVal());
                Type valueType = value.getType();
                ArrayList<Value> indexs = new ArrayList<>();

                //  判断是否为一个数组**或i32**
                boolean isFuncLParam = false;
                if(!valueType.isArrayType() && valueType.isPointerType()){
                    PointerType pointerType = (PointerType) valueType;
                    Type elementType = pointerType.getElementType();
                    if(elementType.isPointerType()){
                        isFuncLParam = true;
                    }
                }

                //  FuncLParam采用load， 正常数组gep多建一个0
                if(isFuncLParam){
                    value = f.buildLoadInst(value, CurBasicBlock);
                }
                else {
                    indexs.add(ConstInteger.constZero);
                }

                //  到这无论是FuncFParam还是正常定义的数组应该都是正常的ArrayType
                ArrayList<ExpAST> expASTS = lValAST.getExpASTS();
                for(ExpAST expAST : expASTS){
                    visitExpAST(expAST, false);
                    indexs.add(CurValue);
                }

                GepInst gepInst = f.buildGepInst(value, indexs, CurBasicBlock);
                CurValue = gepInst;
                if(!isPointer){
                    CurValue = fetchVal(gepInst);
                }

            }
//            //! not isConst
//            Value value = find(lValAST.getIdent().getVal());
//            Type valueType = value.getType();
//            ArrayList<Value> indexs = new ArrayList<>();
//
//            //  判断是否为一个数组**或i32**
//            boolean isFuncLParam = false;
//            if(!valueType.isArrayType() && valueType.isPointerType()){
//                PointerType pointerType = (PointerType) valueType;
//                Type elementType = pointerType.getElementType();
//                if(elementType.isPointerType()){
//                    isFuncLParam = true;
//                }
//            }
//
//            //  FuncLParam采用load， 正常数组gep多建一个0
//            if(isFuncLParam){
//                value = f.buildLoadInst(value, CurBasicBlock);
//            }
//            else {
//                indexs.add(ConstInteger.constZero);
//            }
//
//            //  到这无论是FuncFParam还是正常定义的数组应该都是正常的ArrayType
//            ArrayList<ExpAST> expASTS = lValAST.getExpASTS();
//            for(ExpAST expAST : expASTS){
//                visitExpAST(expAST, false);
//                indexs.add(CurValue);
//            }
//
//            GepInst gepInst = f.buildGepInst(value, indexs, CurBasicBlock);
//            CurValue = gepInst;
//            if(!isPointer){
//                CurValue = fetchVal(gepInst);
//            }
        }
        /*
         * 传入的LVal参数有几种情况x`
         * 1. 普通的i32*
         * 2. 数组[4 * i32]*
         * 3. 数组参数 [4 * i32]**
         * 4. 数组参数 i32**
         * 5. 常数
         *
         * 注意，数组参数一定和isFetch=True配套出现，
         * 因为不会出现为数组指针赋值的情况
         * */




        //! isConst biao

    }






    // endregion

    //region lor
    private void visitCondAST(CondAST condAST, BasicBlock TrueBlock, BasicBlock FalseBlock){
        CurValue = null;
        CurOP = null;
        visitLOrExpAST(condAST.getlOrExpAST(), TrueBlock, FalseBlock);
    }


    //! a&&b || c&&d || e&&f
    private void visitLOrExpAST(LOrExpAST lOrExpAST, BasicBlock trueBlock,BasicBlock falseBlock){
        BasicBlock nxtLorBlock = falseBlock;
        switch (lOrExpAST.getState()){
            //! a
            case 1->{
                visitLAndExpAST(lOrExpAST.getlAndExpAST(), trueBlock, nxtLorBlock);
                CurValue = f.buildCmpInst(CurValue,new ConstInteger(0),OP.Ne,CurBasicBlock);
                f.buildBrInst(CurValue, trueBlock, nxtLorBlock, CurBasicBlock);
            }
            //! a || b   a->b
            case 2->{
                nxtLorBlock = f.buildBasicBlock(CurFunction);
                visitLAndExpAST(lOrExpAST.getlAndExpAST(), trueBlock, nxtLorBlock);
                CurValue = f.buildCmpInst(CurValue,new ConstInteger(0),OP.Ne,CurBasicBlock);
                f.buildBrInst(CurValue, trueBlock, nxtLorBlock, CurBasicBlock);
                CurBasicBlock = nxtLorBlock;
                visitLOrExpAST(lOrExpAST.getlOrExpAST(), trueBlock, falseBlock);
            }
        }

    }
    private void visitLAndExpAST(LAndExpAST lAndExpAST, BasicBlock TrueBLock, BasicBlock FalseBlock){
        CurValue = null;
        CurOP = null;
        visitEqExpAST(lAndExpAST.getEqExpAST());

        if(lAndExpAST.getState() == 2){
            BasicBlock NxtLAndBlock = f.buildBasicBlock(CurFunction);
            CurValue = f.buildCmpInst(CurValue, ConstInteger.constZero, OP.Ne, CurBasicBlock);
            f.buildBrInst(CurValue, NxtLAndBlock, FalseBlock, CurBasicBlock);
            CurBasicBlock = NxtLAndBlock;
            visitLAndExpAST(lAndExpAST.getlAndExp(), TrueBLock, FalseBlock);
        }
    }

    private void visitEqExpAST(EqExpAST eqExpAST){
        Value TmpValue = CurValue;
        OP TmpOP = CurOP;
        CurValue = null;
        CurOP = null;
        visitRelExpAST(eqExpAST.getRelExpAST());

        if(TmpValue != null){
            TmpValue = compType(TmpValue);
            CurValue = f.buildCmpInst(TmpValue, CurValue, TmpOP, CurBasicBlock);
        }

        if(eqExpAST.getState() == 2){
            CurOP = StrToOP(eqExpAST.getOp());
            visitEqExpAST(eqExpAST.getEqExpAST());
        }
    }

    private void visitRelExpAST(RelExpAST relExpAST){
        Value TmpValue = CurValue;
        OP TmpOP = CurOP;
        CurValue = null;
        CurOP = null;
        visitAddExpAST(relExpAST.getAddExp(), false);

        if(TmpValue != null){
            TmpValue = compType(TmpValue);
            CurValue = f.buildCmpInst(TmpValue, CurValue, TmpOP, CurBasicBlock);
        }
        if(relExpAST.getState() == 2){
            CurOP = StrToOP(relExpAST.getOp());
            visitRelExpAST(relExpAST.getRelExp());
        }
    }
    //endregion


    //region vars

    private void visitDeclAST(DeclAST declAST, boolean isGlobal){
        if(declAST.getConstDeclAST()!=null) visitConstDeclAST(declAST.getConstDeclAST(), isGlobal);
        else visitVarDeclAST(declAST.getVarDeclAST(), isGlobal);
    }

    private void visitVarDeclAST(VarDeclAST varDeclAST, boolean isGlobal){
        ArrayList<VarDefAST> varDefASTS = varDeclAST.getVarDefs();
        for(VarDefAST varDefAST : varDefASTS){
            visitVarDefAST(varDefAST, isGlobal);
            fillInitVal = new ArrayList<>();
        }
    }
    private void visitConstDeclAST(ConstDeclAST constDeclAST, boolean isGlobal){
        ArrayList<ConstDefAST> constDefASTS = constDeclAST.getConstDefASTS();
        for(ConstDefAST constDefAST : constDefASTS){
            visitConstDefAST(constDefAST, isGlobal);
            fillInitVal = new ArrayList<>();
        }
    }

    private void visitConstDefAST(ConstDefAST constDefAST, boolean isGlobal){
        String rawIdent = constDefAST.getIdent().getVal();
        int cnt = addSymCnt(rawIdent);
        String ident = "@" + rawIdent + "_" + cnt;

        if(!isGlobal){
            switch (constDefAST.getState()){
                case 1->{
                    visitConstInitValAST(constDefAST.getConstInitValAST());
                    pushSymbol(rawIdent,CurValue);
                }
                //!数组
                case 2->{

                    int dimension =1;
                    ArrayList<Integer> dimensionsList = new ArrayList<>();
                    ArrayList<ConstExpAST> constDimensionsList = constDefAST.getConstExpASTS();
                    for(ConstExpAST constExpAST: constDimensionsList){
                        visitConstExpAST(constExpAST);
                        int dim = Integer.parseInt(CurValue.getName());
                        dimensionsList.add(dim);
                        dimension*=dim;
                    }

                    //! const must have initial
                    fillInitVal = new ArrayList<>();
                    //! 修改后arraylist肯定有，但是有几个就不知道了，对就是这样
                    visitConstInitValAST(constDefAST.getConstInitValAST());

                    ArrayList<Integer> _initVals = new ArrayList<>();
                    for(Value value: fillInitVal){
                        _initVals.add(Integer.parseInt(value.getName()));
                    }
                    ConstArray constArray = new ConstArray(ident, dimensionsList, _initVals);
                    //  添加;const来保证不会与ident重合，同时存下数组的初始值一遍直接解出初始值
                    pushSymbol(rawIdent + ";const", constArray);

                    //! I am not sure about the two symbol
                    AllocInst allocInst = f.buildArray(ident, dimensionsList, CurBasicBlock, true);
                    ArrayList<Value> indexs =new ArrayList<>();
                    int dimCount =dimensionsList.size();
                    for (int i = 0; i <= dimCount; i++) {
                        indexs.add(ConstInteger.constZero);
                    }

                    storeArrayInit(allocInst, dimCount,dimensionsList);
                    pushSymbol(rawIdent, allocInst);

                }
            }
        }else{
            switch (constDefAST.getState()){
                case 1->{
                    visitConstInitValAST(constDefAST.getConstInitValAST());
                    f.buildGlobalVar(ident, new IntegerType(32),true, CurValue, globalVars);
//                    CurValue = f.buildGlobalVar(ident, new PointerType(new IntegerType(32)),true, CurValue, globalVars);
//                    GlobalVar globalVar = (GlobalVar) CurValue;
//                    globalVar.setIsConstDecl(true);
                    pushSymbol(rawIdent,CurValue);
                }
                //! 数组
                case 2->{

                    int dimension =1;
                    ArrayList<Integer> dimensionsList = new ArrayList<>();
                    ArrayList<ConstExpAST> constDimensionsList = constDefAST.getConstExpASTS();
                    for(ConstExpAST constExpAST: constDimensionsList){
                        visitConstExpAST(constExpAST);
                        int dim = Integer.parseInt(CurValue.getName());
                        dimensionsList.add(dim);
                        dimension*=dim;
                    }

                    //! const must have initial
                    fillInitVal = new ArrayList<>();
                    //! 修改后arraylist肯定有，但是有几个就不知道了，对就是这样
                    visitConstInitValAST(constDefAST.getConstInitValAST());
                    ArrayList<Integer> _initVals = new ArrayList<>();
                    for(Value value: fillInitVal){
                        _initVals.add(Integer.parseInt(value.getName()));
                    }
                    ConstArray constArray = new ConstArray(ident, dimensionsList, _initVals);
                    //  添加;const来保证不会与ident重合，同时存下数组的初始值一遍直接解出初始值
                    pushSymbol(rawIdent + ";const", constArray);

                    //! I am not sure about the two symbol
                    GlobalVar globalVar = f.buildGlobalVar(ident,dimensionsList,fillInitVal,true,globalVars);
                    fillInitVal = new ArrayList<>();
                    pushSymbol(rawIdent, globalVar);
                }
            }
        }

    }
    private void storeArrayInit(AllocInst pointer, int dim,ArrayList<Integer> dimensionsList) {
        GepInst itPointer;
        ArrayList<Value> indexs;
        for (int i = 0; i < fillInitVal.size(); i++) {
            //  重新构建gep所需的indexs
            indexs = new ArrayList<>();
            for(int j = 0; j < dim; j++){
                indexs.add(ConstInteger.constZero);
            }
            indexs.add(new ConstInteger(i));
            itPointer = f.buildGepInst(pointer, indexs, CurBasicBlock);

            f.buildStoreInst( fillInitVal.get(i), itPointer,CurBasicBlock);
        }
    }



    private void visitVarDefAST(VarDefAST varDefAST, boolean isGlobal){
        //  这里rawIdent指的是未加@，cnt之类的ident(纯用户命名的ident)
        String rawIdent = varDefAST.getIdent().getVal();
//        ErrDump.error_b(rawIdent, getNowSymTbl(), varDefAST.getLine());
        int cnt = addSymCnt(rawIdent);
        String ident = "@" + rawIdent + "_" + cnt;
        int state=0;
        if(varDefAST.getConstExpASTS().size()==0){
            //! not array
            if(varDefAST.getInitValAST()!=null){
                //! have initval
                state = 2;
            }
            else{
                state = 1;
            }
        }
        else{
            //! array
            if(varDefAST.getInitValAST()!=null){
                state = 4;
            }
            else{
                state = 3;
            }
        }
        if(varDefAST.getGetintToken()!=null) state = 5;
        if(isGlobal){


            switch (state){





                //int a
                case 1->{
                    CurValue = new ConstInteger(0);
                    CurValue = f.buildGlobalVar(ident, new PointerType(new IntegerType(32)),false, CurValue, globalVars);
//                    globalVars.add((GlobalVar) CurValue);
                    pushSymbol(rawIdent,CurValue);

                }
                //int a = 0
                case 2->{
                    visitInitValAST(varDefAST.getInitValAST(), true);
                    CurValue = f.buildGlobalVar(ident, new PointerType(new IntegerType(32)),false, CurValue, globalVars);
//                    globalVars.add((GlobalVar) CurValue);
                    pushSymbol(rawIdent,CurValue);
                }
                case 3->{
                    //  totDim用于记录将数组展成一维有多少个元素，便于后续算字节数，构建指令
                    int totDim = 1;
                    //  访问ConstExpAST的List得到CurValue(肯定是ConstInteger)
                    //  这些CurValue的值就是数组的维度，放进一个dimList里
                    ArrayList<Integer> dimList = new ArrayList<>();
                    ArrayList<ConstExpAST> constExpASTS = varDefAST.getConstExpASTS();

                    for(ConstExpAST constExpAST : constExpASTS){
                        visitConstExpAST(constExpAST);
                        int dim = Integer.parseInt(CurValue.getName());
                        dimList.add(dim);
                        totDim = totDim * dim;
                    }
                    //构建InitValS
                    fillInitVal = new ArrayList<>();
                    GlobalVar globalVar;
                    //  没有显式初始化就填充0
                    for(int i = 0; i < totDim; i++){
                        fillInitVal.add(ConstInteger.constZero);
                    }
                    globalVar = f.buildGlobalVar(ident, dimList, fillInitVal, false,globalVars);
                    pushSymbol(rawIdent, globalVar);
                }
                case 4->{
                    //  totDim用于记录将数组展成一维有多少个元素，便于后续算字节数，构建指令
                    int totDim = 1;
                    //  访问ConstExpAST的List得到CurValue(肯定是ConstInteger)
                    //  这些CurValue的值就是数组的维度，放进一个dimList里
                    ArrayList<Integer> dimList = new ArrayList<>();
                    ArrayList<ConstExpAST> constExpASTS = varDefAST.getConstExpASTS();

                    for(ConstExpAST constExpAST : constExpASTS){
                        visitConstExpAST(constExpAST);
                        int dim = Integer.parseInt(CurValue.getName());
                        dimList.add(dim);
                        totDim = totDim * dim;
                    }
                    //构建InitValS
                    fillInitVal = new ArrayList<>();
                    GlobalVar globalVar;
                    visitInitValAST(varDefAST.getInitValAST(), true);

                    globalVar = f.buildGlobalVar(ident, dimList, fillInitVal, false,globalVars);
                    pushSymbol(rawIdent, globalVar);
                }
                case 5->{

                }
            }
        }
        else{
            switch (state){
                //int a
                case 1->{
                    AllocInst allocInst = f.buildAllocInst(new IntegerType(32), CurBasicBlock);
                    f.buildStoreInst(new ConstInteger(0),allocInst,  CurBasicBlock);
                    pushSymbol(rawIdent, allocInst);
                }
                //int a = 0
                case 2->{
                    AllocInst allocInst = f.buildAllocInst(new IntegerType(32), CurBasicBlock);
                    visitInitValAST(varDefAST.getInitValAST(), false);
                    f.buildStoreInst(CurValue,allocInst,  CurBasicBlock);
                    pushSymbol(rawIdent, allocInst);
                }
                default -> {
                    //! hava init val
                    if(varDefAST.getInitValAST()!=null){
                        int dimension =1;//!数组总元素数量
                        ArrayList<Integer> dimensionsList = new ArrayList<>();
                        ArrayList<ConstExpAST> constDimensionsList = varDefAST.getConstExpASTS();
                        for(ConstExpAST constExpAST: constDimensionsList){
                            visitConstExpAST(constExpAST);
                            int dim = Integer.parseInt(CurValue.getName());
                            dimensionsList.add(dim);
                            dimension*=dim;
                        }

                        AllocInst allocInst = f.buildArray(ident, dimensionsList, CurBasicBlock, false);
                        pushSymbol(rawIdent, allocInst);


                        int dimCount =dimensionsList.size();
                        fillInitVal = new ArrayList<>();
                        visitInitValAST(varDefAST.getInitValAST(),false);
                        storeArrayInit(allocInst, dimCount,dimensionsList);

                    }
                    //! not have init val
                    else{
                        //  totDim用于记录将数组展成一维有多少个元素，便于后续算字节数，构建指令
                        int totDim = 1;
                        //  访问ConstExpAST的List得到CurValue(肯定是ConstInteger)
                        //  这些CurValue的值就是数组的维度，放进一个dimList里
                        ArrayList<Integer> dimList = new ArrayList<>();
                        ArrayList<ConstExpAST> constExpASTS = varDefAST.getConstExpASTS();

                        for(ConstExpAST constExpAST : constExpASTS){
                            visitConstExpAST(constExpAST);
                            int dim = Integer.parseInt(CurValue.getName());
                            dimList.add(dim);
                            totDim = totDim * dim;
                        }

                        AllocInst allocInst = f.buildArray(ident, dimList, CurBasicBlock, false);
                        pushSymbol(rawIdent, allocInst);

                        ArrayList<Value> indexs = new ArrayList<>();
                        int dim = dimList.size();
                        for(int i = 0; i < dim + 1; i++){
                            indexs.add(ConstInteger.constZero);
                        }
                        GepInst pointer = f.buildGepInst(allocInst, indexs, CurBasicBlock);
                    }
                }
            }
        }
    }

    //endregion


    // region InitVal
    private void visitConstInitValAST(ConstInitValAST constInitValAST){
        switch (constInitValAST.getState()){
            case 1->{visitConstExpAST(constInitValAST.getConstExpAST());
                fillInitVal.add(CurValue);}
            case 2,3->{
                ArrayList<ConstInitValAST> constInitValASTS = constInitValAST.getConstInitValASTS();
                for(ConstInitValAST constInitValAST1 : constInitValASTS){
                    visitConstInitValAST(constInitValAST1);
                }
            }
        }
    }
    private void visitInitValAST(InitValAST initValAST, boolean isCal){
        switch (initValAST.getState()){
            case 1->{
                visitExpAST(initValAST.getExpAST(), isCal);
                fillInitVal.add(CurValue);
            }
            case 2,3->{
                ArrayList<InitValAST> initValASTS = initValAST.getInitValASTS();
                for(InitValAST initValAST1 : initValASTS){
                    visitInitValAST(initValAST1, isCal);
                }
            }
        }
    }
    // endregion


    //region Exp
    private void visitConstExpAST(ConstExpAST constExpAST){
        CurValue = null;
        CurOP = null;
        visitAddExpAST(constExpAST.getAddExp(),true);
    }
    private void visitExpAST(ExpAST expAST, boolean isConst){
        CurValue = null;
        CurOP = null;
        visitAddExpAST(expAST.getAddExpAST(), isConst);
    }
    private void visitAddExpAST(AddExpAST addExpAST, boolean isConstExp){
        if(!isConstExp){
            Value TmpValue = CurValue;
            OP TmpOP = CurOP;
            CurValue = null;
            CurOP = null;
            visitMulExpAST(addExpAST.getMulExpAST(), false);
            //  TmpValue用于保存上一层传进来的用于计算Value
            //  如果为null说明是最左边的第一次递归
            if(TmpValue != null){
                //  构建运算之前先compType
                TmpValue = compType(TmpValue);
                CurValue = f.buildBinaryInst(TmpOP, TmpValue, CurValue, CurBasicBlock);
            }
            if(addExpAST.getAddExpAST()!=null){
                CurOP = StrToOP(addExpAST.getOp());
                visitAddExpAST(addExpAST.getAddExpAST(), false);
            }
        }else{
            ConstInteger left = (ConstInteger) CurValue;
            OP TmpOP = CurOP;
            CurValue = null;
            CurOP = null;
            visitMulExpAST(addExpAST.getMulExpAST(), true);
            ConstInteger right = (ConstInteger) CurValue;
            if(left != null){
                CurValue = calValue(left.getVal(), TmpOP, right.getVal());
            }
            if(addExpAST.getAddExpAST()!=null){
                CurOP = StrToOP(addExpAST.getOp());
                visitAddExpAST(addExpAST.getAddExpAST(), true);
            }
        }
    }
    private void visitMulExpAST(MulExpAST mulExpAST, boolean isConstExp){
        if(!isConstExp) {
            Value TmpValue = CurValue;
            OP TmpOP = CurOP;
            CurValue = null;
            CurOP = null;
            visitUnaryExpAST(mulExpAST.getUnaryExpAST(), false);
            if(TmpValue != null){
                TmpValue = compType(TmpValue);
                CurValue = f.buildBinaryInst(TmpOP, TmpValue, CurValue, CurBasicBlock);
            }
            if (mulExpAST.getMulExpAST() !=null) {
                CurOP = StrToOP(mulExpAST.getOp());
                visitMulExpAST(mulExpAST.getMulExpAST(), false);
            }
        }
        else{
            ConstInteger left = (ConstInteger) CurValue;
            OP TmpOP = CurOP;
            CurValue = null;
            CurOP = null;
            visitUnaryExpAST(mulExpAST.getUnaryExpAST(), true);
            ConstInteger right = (ConstInteger) CurValue;
            if(left != null){
                CurValue = calValue(left.getVal(), TmpOP, right.getVal());
            }
            if(mulExpAST.getMulExpAST() !=null) {
                CurOP = StrToOP(mulExpAST.getOp());
                visitMulExpAST(mulExpAST.getMulExpAST(), true);
            }
        }
    }
    private void visitUnaryExpAST(UnaryExpAST unaryExpAST, boolean isConstExp){
        if(!isConstExp) {
            switch (unaryExpAST.getState()){
                case 1->{
                    visitPrimaryExpAST(unaryExpAST.getPrimaryExpAST(),false);
                }
                case 2->{
                    String funcName = unaryExpAST.getIdent().getVal();
                    Value findFunc = find(funcName);
                    Function function = (Function) findFunc;
                    if(unaryExpAST.getFuncRParamsAST()==null){
                        CurValue = f.buildCallInst(CurBasicBlock, function);
                    }else{
                        ArrayList<Value> values = new ArrayList<>();
                        ArrayList<ExpAST> expASTS = unaryExpAST.getFuncRParamsAST().getExpASTS();
                        isFuncRParam++;
                        for (ExpAST expAST : expASTS) {
                            visitExpAST(expAST, false);
                            values.add(CurValue);
                        }
                        isFuncRParam--;
                        CurValue = f.buildCallInst(CurBasicBlock, function, values);
                    }
                }
                case 3->{
                    visitUnaryExpAST(unaryExpAST.getUnaryExpAST(),false);
                    switch (unaryExpAST.getUnaryOpAST().getOperator()){
                        case "+"->{
                        }
                        case "-"->{
                            CurValue = f.buildBinaryInst(OP.Sub, ConstInteger.constZero, CurValue, CurBasicBlock);
                        }
                        case "!"->{
                            CurValue = f.buildCmpInst(ConstInteger.constZero, CurValue, OP.Eq, CurBasicBlock);
                        }
                    }
                }
            }
        }
        else {
            switch (unaryExpAST.getState()){
                case 1->{
                    visitPrimaryExpAST(unaryExpAST.getPrimaryExpAST(),true);
                }
                case 2->{
                    if(unaryExpAST.getFuncRParamsAST()==null){
                    }else{
                    }
                }
                case 3->{
                    visitUnaryExpAST(unaryExpAST.getUnaryExpAST(), true);
                    ConstInteger constInteger = (ConstInteger) CurValue;
                    switch (unaryExpAST.getUnaryOpAST().getOperator()){
                        case "+"->{}
                        case "-"->{CurValue = calValue(0, StrToOP("-"), constInteger.getVal());}
                        case "!"->{}
                    }

                }
            }
        }
    }
    private void visitPrimaryExpAST(PrimaryExpAST primaryExpAST, boolean isConstExp){
        if(!isConstExp) {
            switch (primaryExpAST.getState()){
                case 1->{ visitExpAST(primaryExpAST.getExpAST(), false);}
                case 2->{visitLvalAST(primaryExpAST.getlValAST(),false,false);}
                case 3->{visitNumberAST(primaryExpAST.getNumberAST());}
            }
        }
        else{
            switch (primaryExpAST.getState()){
                case 1->{ visitExpAST(primaryExpAST.getExpAST(),true);}
                case 2->{visitLvalAST(primaryExpAST.getlValAST(),true,false);}
                case 3->{
                    NumberAST numberAST = primaryExpAST.getNumberAST();
                    CurValue = f.buildNumber(numberAST.getIntConst());
                }
            }
        }
    }




    private void visitNumberAST(NumberAST numberAST){
        CurValue = f.buildNumber(numberAST.getIntConst());
    }


    private Value find(String ident){
        int len = symTbls.size();
        for(int i = len - 1; i >= 0; i--){
            HashMap<String, Value> symTbl = symTbls.get(i);
            Value res = symTbl.get(ident);
            if(res != null){
                return res;
            }
        }
        return null;
    }



    //endregion


    //region Utils
    private Value compType(Value tmp){
        //  构建cmp指令之前先检查类型
        //  构建cmp指令之前先检查类型
        IntegerType tmpType = (IntegerType) tmp.getType();
        IntegerType curType = (IntegerType) CurValue.getType();
        if(tmpType.getBitNum() != curType.getBitNum()){
            if(curType.getBitNum() == 1){
                CurValue = checkType(CurValue);
                return tmp;
            }
            else return checkType(tmp);
        }
        return tmp;
    }
    private Value checkType(Value value){
        if(value.getType().isIntegerType()){
            IntegerType integerType = (IntegerType) value.getType();
            int bit = integerType.getBitNum();
            if(bit == 1){
                return f.buildConversionInst(value, OP.Zext, CurBasicBlock);
            }
        }
        return value;
    }


    private ConstInteger calValue(int left, OP op, int right){
        switch (op) {
            case Add:
                return new ConstInteger(left + right);
            case Sub:
                return new ConstInteger(left - right);
            case Mul:
                return new ConstInteger(left * right);
            case Div:
                return new ConstInteger(left / right);
            case Mod:
                return new ConstInteger(left % right);
            default:
                return null;
        }
    }
    private OP StrToOP(String str){
        switch (str) {
            case "+":
                return OP.Add;
            case "-":
                return OP.Sub;
            case "*":
                return OP.Mul;
            case "/":
                return OP.Div;
            case "%":
                return OP.Mod;
            case "==":
                return OP.Eq;
            case "!=":
                return OP.Ne;
            case ">":
                return OP.Gt;
            case "<":
                return OP.Lt;
            case ">=":
                return OP.Ge;
            case "<=":
                return OP.Le;
        }
        return null;
    }

    private Value fetchVal(Value value){
        Type type = value.getType();

        if(type.isArrayType()){
            ArrayList<Value> indexs = new ArrayList<>();
            for(int i = 0; i < 2; i++) {
                indexs.add(ConstInteger.constZero);
            }
            return f.buildGepInst(value, indexs, CurBasicBlock);
        }
        else return f.buildLoadInst(value, CurBasicBlock);
    }


    private Value transType(Value value){
        if(value.getType().isIntegerType()){
            IntegerType integerType = (IntegerType) value.getType();
            int bit = integerType.getBitNum();
            if(bit == 1){
                return f.buildConversionInst( value,OP.Zext, CurBasicBlock);
            }
        }
        return value;
    }


    //endregion
    //! String To Operation

}


