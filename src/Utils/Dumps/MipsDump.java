package Utils.Dumps;

import Frontend.AST.MainFuncDefAST;
import LLVMIR.IRModule;
import LLVMIR.Type.*;
import LLVMIR.Value.*;
import LLVMIR.Value.Instruction.*;
import Utils.DataStruct.IList;
import Utils.DataStruct.Triple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class MipsDump {
    private static final MipsDump instance = new MipsDump();


    //! 你说的对所以这是干啥的
    private Map<String, Triple<String, Integer, Value>> mem = new HashMap<>();

    int spOff = 0, rec = 0;

    private String globalVarRename(String str){
        str = str.substring(1);
        String global_pre = "global_pre_" + str;
        return global_pre;
    }

    private String funcdefRename(String str){
        //! 为main量身打造
        if(str.equals("@main")){
            str = str.substring(1);
        }
        else{
            str = str.substring(1);
            str = "func_" + str;
        }

        return str;
    }

    int nowNum = 0;
    private void renameFunc(Function function){


        ArrayList<Argument> args = function.getArgs();
        for(Argument arg : args){
            arg.setName("mipsvar" + nowNum++);
        }

        IList<BasicBlock, Function> basicBlocks = function.getBbs();
        for (IList.INode<BasicBlock, Function> bbNode : basicBlocks) {
            BasicBlock basicBlock = bbNode.getValue();
            basicBlock.setName("mipsvar" + nowNum++);
            IList<Instruction, BasicBlock> instructions = basicBlock.getInsts();
            for (IList.INode<Instruction, BasicBlock> instNode : instructions) {
                Instruction inst = instNode.getValue();
                if(inst.hasname()){
                    inst.setName("mipsvar" + nowNum++);
                }

            }
        }
    }

    private void getGp(String name, Value value) {
        if (mem.containsKey(name)) {
            return;
        }
        mem.put(name, new Triple<>("$gp", 0, value));
    }

    private void getSp(String name, Value value) {
        if (mem.containsKey(name)) {
            return;
        }
        spOff -= 4;
        mem.put(name, new Triple<>("$sp", spOff, value));
    }

    private void getSpArray(String name, int offset, Value value) {
        if (mem.containsKey(name)) {
            return;
        }
        getSp(name, value);
        spOff -= offset;
        System.out.print("addu $t0, $sp, " + spOff + "\n");
        store("$t0", name);
    }

    public static MipsDump getInstance() {
        return instance;
    }
    IRModule ir;
    public void loadIRModule(IRModule ir){
        this.ir = ir;
    }

    private static int initArrayNow;

    public void dumpMips(IRModule ir) throws IOException {
        File file = new File ("mips.txt");
        PrintStream ps = new PrintStream(file);
        System.setOut(ps);

        System.out.print(".data\n");
        //! rename globalVar
        for(GlobalVar globalVar  : ir.getGlobalVars()){
            globalVar.setName(globalVarRename(globalVar.getName()));
        }
        //! Global Var
        for(GlobalVar globalVar  : ir.getGlobalVars()){
            Type type = globalVar.getType();
            if(type.isIntegerType()){
                getGp(globalVar.getName(), globalVar);
//                System.out.println("print integer");
                System.out.println(globalVar.getName() + ": .word " + globalVar.getValue().getName() + "\n");
            }else if(type.isArrayType()){
//                System.out.println("print array");
                getGp(globalVar.getName(), globalVar);
                System.out.print(globalVar.getName()+": .word ");
                ArrayType arrayType = (ArrayType) globalVar.getType();
                ArrayList<Value> values = globalVar.getValues();
                if(values.size() == 0){
                    int dim = 1;
                    ArrayType tmpArrayType = arrayType;
                    while (tmpArrayType.isArrayType()){
                        dim*=tmpArrayType.getEleDim();
                        tmpArrayType = (ArrayType)tmpArrayType.getElementType();
                    }
                    dim *= tmpArrayType.getEleDim();
                    dim *= 4;

                    //! --------------非常遗憾，我是用全0代替的玛德--------------------------------------------------

                    System.out.print(".space " + dim);
//                    System.out.print(arrayType.toString().replace("*", ""));
//                    System.out.print(" zeroinitializer\n");
                }
                else {
                    //  初始化当前输出的位置
                    initArrayNow = 0;
                    DumpInitArray(arrayType, values);
                    System.out.println();
                }
            }
        }
        //!Func Def
        /**
         *             if (function.isLibraryFunction()) {
         *                 if (Objects.equals(function.getName(), "getint"))
         *                     IOUtils.mips("\n.macro GETINT()\nli $v0, 5\nsyscall\n.end_macro\n");
         *                 else if (Objects.equals(function.getName(), "putint"))
         *                     IOUtils.mips("\n.macro PUTINT()\nli $v0, 1\nsyscall\n.end_macro\n");
         *                 else if (Objects.equals(function.getName(), "putch"))
         *                     IOUtils.mips("\n.macro PUTCH()\nli $v0, 11\nsyscall\n.end_macro\n");
         *                 else if (Objects.equals(function.getName(), "putstr"))
         *                     IOUtils.mips("\n.macro PUTSTR()\nli $v0, 4\nsyscall\n.end_macro\n");
         *             }*/
        /**
         *         IOUtils.mips("\n.text\n");
         *         IOUtils.mips("\njal main\n");
         *         IOUtils.mips("\nj return\n\n");*/

        System.out.print("\n.macro GETINT()\nli $v0, 5\nsyscall\n.end_macro\n");
        System.out.print("\n.macro PUTINT()\nli $v0, 1\nsyscall\n.end_macro\n");
        System.out.print("\n.macro PUTCH()\nli $v0, 11\nsyscall\n.end_macro\n");
        System.out.print("\n.macro PUTSTR()\nli $v0, 4\nsyscall\n.end_macro\n");

        System.out.print("\n.text\n");
        System.out.print("\njal main\n");
        System.out.print("\nj return\n\n");


        ArrayList<Function> functions = ir.getFunctions();
        for(Function f:functions){
//            ReNameFunc(f);
            f.setName(funcdefRename(f.getName()));
        }


        for(Function f:functions){
//            ReNameFunc(f);
            renameFunc(f);
            DumpFunction(f);
        }


        System.out.println("return:");

    }

    private static void DumpInitArray(ArrayType arrayType, ArrayList<Value> values) throws IOException {
        Type type = arrayType.getElementType();
//        DumpType(arrayType);
        if(type.isArrayType()){
            ArrayType sonType = (ArrayType) type;
            int len = arrayType.getEleDim();
//            System.out.print("[");
            for(int i = 0; i < len; i++){
                DumpInitArray(sonType, values);
                if(i != len - 1) System.out.print(", ");
            }
//            System.out.print("]");
        }
        else if(type.isPointerType()){
            System.out.print(" ");
//            System.out.print("[");
            int len = arrayType.getEleDim();
            for(int i = 0; i < len;i++){
                System.out.print(" " + values.get(initArrayNow++).getName());
                if(i != len - 1) System.out.print(", ");
            }
//            System.out.print("]");
        }
    }


    private void DumpFunction(Function function) throws IOException {
        System.out.print("\n" + function.getName() + ":\n");
        rec = function.getArgs().size();
        for (int i = 0; rec > 0; i++) {
            rec--;
            load("$t0", "$sp", 4 * rec);
            getSp(function.getArgs().get(i).getName(), function.getArgs().get(i));
            store("$t0", function.getArgs().get(i).getName());
        }
        rec = 0;
        for (IList.INode<BasicBlock, Function> blockEntry : function.getBbs()) {
            BasicBlock basicBlock = blockEntry.getValue();
            System.out.print("\n" + basicBlock.getName() + ":\n");

            for (IList.INode<Instruction, BasicBlock> instEntry : basicBlock.getInsts()) {
                Instruction iir = instEntry.getValue();
//                System.out.print("\n# " + iir.toString() + "\n\n");
                if (!(iir instanceof AllocInst)) {
                    getSp(iir.getName(), iir);
                }
                DumpInstruction(iir);
            }
            for (IList.INode<Instruction, BasicBlock> instEntry : basicBlock.getInsts()) {
                Instruction iir = instEntry.getValue();
//                System.out.print("\n# " + iir.toString() + "\n\n");
                if (!(iir instanceof AllocInst)) {
                    getSp(iir.getName(), iir);
                }


                DumpInstruction(iir);
            }


        }
    }

    private void DumpBasicBlock(BasicBlock bb) throws IOException {
        String bbName = bb.getName();
        bbName = bbName.replace("%", "");
        System.out.println(bbName + ":");
        IList<Instruction, BasicBlock> insts = bb.getInsts();
        for(IList.INode<Instruction, BasicBlock> instNode : insts){
//            DumpInstruction(instNode.getValue());
            System.out.print("\n");
        }
    }





    //!!!
    private void DumpInstruction(Instruction ir) {
        if (ir instanceof BinaryInst) parseBinary((BinaryInst) ir);
        else if (ir instanceof CallInst) parseCall((CallInst) ir);
        else if (ir instanceof RetInst) parseRet((RetInst) ir);
        else if (ir instanceof AllocInst) parseAlloc((AllocInst) ir);
        else if (ir instanceof LoadInst) parseLoad((LoadInst) ir);
        else if (ir instanceof StoreInst) parseStore((StoreInst) ir);
        else if (ir instanceof GepInst) parseGEP((GepInst) ir);
        else if (ir instanceof BrInst) parseBr((BrInst) ir);
        else if (ir instanceof ConversionInst) parseConv((ConversionInst) ir);
    }

    private void calc(BinaryInst b, String op, int type) {
        if (type == 0 && b.getOperand(0) instanceof ConstInteger) {
            load("$t0", b.getOperand(1).getName());
            System.out.print(op + " $t0, $t0, " + ((ConstInteger) b.getOperand(0)).getVal() + "\n");
            store("$t0", b.getName());
            return;
        }
        if (type <= 1 && b.getOperand(1) instanceof ConstInteger) {
            load("$t0", b.getOperand(0).getName());
            System.out.print(op + " $t0, $t0, " + ((ConstInteger) b.getOperand(1)).getVal() + "\n");
            store("$t0", b.getName());
            return;
        }
        load("$t0", b.getOperand(0).getName());
        load("$t1", b.getOperand(1).getName());
        System.out.print(op + " $t0, $t0, $t1\n");
        store("$t0", b.getName());
    }
    private void parseBinary(BinaryInst b){


        switch (b.getOp()){
            case Add -> {
                calc(b, "addu", 0);
            }
            case Sub -> {
                calc(b, "subu", 1);
            }
            case Mul -> {
                calc(b, "mul", 0);
            }
            case Div -> {
                calc(b, "div", 1);
            }
            case Mod -> {
                // 加载操作数到寄存器
                load("$t0", b.getOperand(0).getName());
                load("$t1", b.getOperand(1).getName());
                // 执行除法操作
                System.out.print("div $t0, $t1\n");
                // 获取除法操作的余数
                System.out.print("mfhi $t0\n");
                // 存储结果
                store("$t0", b.getName());
            }
        }
    }
    private void parseCall(CallInst callInst){
        Function function = callInst.getCallFunc();
        {
            store("$ra", "$sp", spOff - 4);
            rec = 1;
            int argSize = callInst.getCallFunc().getArgs().size();
            for (int i = 1; i <= argSize; i++) {
                rec++;
                load("$t0", callInst.getOperand(i).getName());
                store("$t0", "$sp", spOff - rec * 4);
            }
            System.out.print("addu $sp, $sp, " + (spOff - rec * 4) + "\n");

            char chararr[] = function.getName().toCharArray();

            if(chararr[0]=='@'){
//                System.out.print("" + function.getName().substring(1) + "\n");
                switch (function.getName()){
                    /*
                    *         System.out.print("\n.macro GETINT()\nli $v0, 5\nsyscall\n.end_macro\n");
        System.out.print("\n.macro PUTINT()\nli $v0, 1\nsyscall\n.end_macro\n");
        System.out.print("\n.macro PUTCH()\nli $v0, 11\nsyscall\n.end_macro\n");
        System.out.print("\n.macro PUTSTR()\nli $v0, 4\nsyscall\n.end_macro\n");*/
                    case "@getint"->{
                        System.out.println("GETINT()");
                    }
                    case "@putint"->{
                        System.out.println("PUTINT()");
                    }
                    case "@putch"->{
                        System.out.println("PUTCH()");
                    }
                    case "@putstr"->{
                        System.out.println("PUTSTR()");
                    }
                }
            }
            else{

                System.out.print("jal " + function.getName() + "\n");
            }

            System.out.print("addu $sp, $sp, " + (-spOff + rec * 4) + "\n");
            load("$ra", "$sp", spOff - 4);
            if (!(function.getType() instanceof VoidType)) {
                store("$v0", callInst.getName());
            }
        }
    }
    private void parseRet(RetInst ret){
        if (!ret.isVoid()) {
            load("$v0", ret.getOperand(0).getName());
        }
        System.out.print("jr $ra\n");
    }
    private void parseAlloc(AllocInst allocInst){
        if (allocInst.getType() instanceof PointerType) {
            PointerType pointerType = (PointerType) allocInst.getType();
            if (pointerType.getElementType() instanceof IntegerType) {
                getSp(allocInst.getName(), allocInst);
            } else if (pointerType.getElementType() instanceof ArrayType) {
                ArrayType arrayType = (ArrayType) pointerType.getElementType();
                int dim = arrayType.getEleDim();
                Type tmpType = arrayType.getElementType();
                ArrayType tmpArrayType;
                PointerType tmpPointerType;
                while (tmpType.isArrayType()){
                    tmpArrayType = (ArrayType) tmpType;
                    dim*=tmpArrayType.getEleDim();
                    tmpType = tmpArrayType.getElementType();
                    //! 如果事ArrayType
                    //!否则是PointerType
                }
                tmpPointerType = (PointerType)tmpType;
                dim *= 4;

                getSpArray(allocInst.getName(), dim, allocInst);
            }
        } else if (allocInst.getType() instanceof IntegerType) {
            getSp(allocInst.getName(), allocInst);
        } else if (allocInst.getType() instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) allocInst.getType();
            int dim = 1;
            ArrayType tmpArrayType = arrayType;
            while (tmpArrayType.isArrayType()){
                dim*=tmpArrayType.getEleDim();
                tmpArrayType = (ArrayType)tmpArrayType.getElementType();
            }
            dim *= tmpArrayType.getEleDim();
            dim *= 4;

            getSpArray(allocInst.getName(), dim, allocInst);
        }
    }
    private void parseLoad(LoadInst loadInst){
        if (loadInst.getOperand(0) instanceof GepInst) {
            load("$t0", loadInst.getOperand(0).getName());
            load("$t1", "$t0", 0);
            store("$t1", loadInst.getName());
        } else {
            load("$t0", loadInst.getOperand(0).getName());
            store("$t0", loadInst.getName());
        }
    }
    private void parseStore(StoreInst storeInst){
        if (storeInst.getOperand(1) instanceof GepInst) {
            load("$t0", storeInst.getOperand(0).getName());
            load("$t1", storeInst.getOperand(1).getName());
            store("$t0", "$t1", 0);
        } else {
            load("$t0", storeInst.getOperand(0).getName());
            store("$t0", storeInst.getOperand(1).getName());
        }
    }
    private void parseGEP(GepInst gepInst){
        PointerType pt = (PointerType) gepInst.getPointer().getType();
        int offsetNum;
        List<Integer> dims;
        if (pt.getElementType() instanceof ArrayType) {
            offsetNum = gepInst.getOperands().size() - 1;
            dims = ((ArrayType) pt.getElementType()).getDimList();
        } else {
            offsetNum = 1;
            dims = new ArrayList<>();
        }
        load("$t2", gepInst.getPointer().getName()); // arr
        int lastOff = 0;
        for (int i = 1; i <= offsetNum; i++) {
            int base = 4;
            if (pt.getElementType() instanceof ArrayType) {
                for (int j = i - 1; j < dims.size(); j++) {
                    base *= dims.get(j);
                }
            }
            if (gepInst.getOperand(i).isNumber()) {
//                System.out.println(gepInst.getOperand(i).getName());
                int dimOff = Integer.parseInt(gepInst.getOperand(i).getName()) * base;
                lastOff += dimOff;
                if (i == offsetNum) {
                    if (lastOff != 0) {
                        System.out.print("addu $t2, $t2, " + lastOff + "\n");
                    }
                    store("$t2", gepInst.getName());
                }
            } else {
                if (lastOff != 0) {
                    System.out.print("addu $t2, $t2, " + lastOff + "\n");
                }
                load("$t0", gepInst.getOperand(i).getName()); // offset
//                optimizeMul(gepInst.getOperand(i), new ConstInt(base), gepInst, true);
                System.out.print("addu $t2, $t2, $t0\n");
                store("$t2", gepInst.getName());
            }
            System.out.print("\n");
        }
    }
    private void parseBr(BrInst brInst){
        //! BrInst(Value value,BasicBlock iftrueBlock,BasicBlock elseBlock)
        if (brInst.getState()==2) {
            load("$t0", brInst.getJudVal().getName());
            System.out.print("beqz $t0, " + brInst.getFalseBlock().getName() + "\n");
            System.out.print("j " + brInst.getTrueBlock().getName() + "\n");
        }
        //! simple jump
        else if(brInst.getState()==1) {
            System.out.print("j " + brInst.getJumpBlock().getName() + "\n");
        }
    }
    private void parseConv(ConversionInst convInst){
        if (convInst.getOp() == OP.Zext) {
            load("$t0", convInst.getOperand(0).getName());
            store("$t0", convInst.getName());
        } else if (convInst.getOp() == OP.BitCast) {
            load("$t0", convInst.getOperand(0).getName());
            store("$t0", convInst.getName());
        }
    }



    private void load(String reg, String name) {
        if (isNumber(name)) {
            System.out.print("li " + reg + ", " + name + "\n");
        } else if (mem.get(name).getThird() instanceof GlobalVar) {
            System.out.print("la " + reg + ", " + name + "\n");
            if (((GlobalVar) mem.get(name).getThird()).getType().isIntegerType()) {
                System.out.print("lw " + reg + ", 0(" + reg + ")\n");
            }
        } else {
            System.out.print("lw " + reg + ", " + mem.get(name).getSecond() + "(" + mem.get(name).getFirst() + ")\n");
        }
    }

    private void load(String reg, String name, int offset) {
//        IOUtils.mips("lw " + reg + ", " + offset + "(" + name + ")\n");
        System.out.print("lw " + reg + ", " + offset + "(" + name + ")\n");
    }

    private void store(String reg, String name) {
        if (mem.get(name).getThird() instanceof GlobalVar) {
//            IOUtils.mips("la $t1, " + name + "\n");
            System.out.print("la $t1, " + name + "\n");
            if (((GlobalVar) mem.get(name).getThird()).getType().isIntegerType()) {
                System.out.print("sw " + reg + ", 0($t1)\n");
            }
        } else {
            System.out.print("sw " + reg + ", " + mem.get(name).getSecond() + "(" + mem.get(name).getFirst() + ")\n");
        }
    }

    private void store(String reg, String name, int offset) {
        System.out.print("sw " + reg + ", " + offset + "(" + name + ")\n");
    }

    private boolean isNumber(String str) {
        return str.matches("-?[0-9]+");
    }





}
