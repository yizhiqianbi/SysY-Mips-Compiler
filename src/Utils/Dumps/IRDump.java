package Utils.Dumps;

import LLVMIR.IRModule;
import LLVMIR.Type.ArrayType;
import LLVMIR.Type.PointerType;
import LLVMIR.Type.Type;
import LLVMIR.Value.*;
import LLVMIR.Value.Instruction.*;
import Utils.DataStruct.IList;

import javax.swing.plaf.synth.SynthUI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class IRDump {
    private static final IRDump instance = new IRDump();
    private static int nowNum = 0;
    private static int initArrayNow;

    public static IRDump getInstance() {
        return instance;
    }


    private static void DumpLib() throws IOException {
        System.out.print("declare i32 @getint()\n");
        System.out.print("declare void @putint(i32)\n");
        System.out.print("declare void @putch(i32)\n");
        System.out.print("declare void @putstr(i8*)\n");
    }


    public void DumpModule(IRModule module) throws IOException {
        File file = new File ("llvm_ir.txt");
        PrintStream ps = new PrintStream(file);
        System.setOut(ps);


        DumpLib();

        ArrayList<GlobalVar> globalVars = module.getGlobalVars();
        for(GlobalVar globalVar : globalVars){
            if(!globalVar.isConst() || globalVar.getType().isArrayType()){
                DumpGlobalVar(globalVar);
                System.out.print("\n");
            }
        }


        ArrayList<Function> functions = module.getFunctions();
        for(Function f:functions){
//            System.out.print("FUNCTION!!!!!!!!!!!!!!" + f.getName());
            ReNameFunc(f);
            DumpFunction(f);
        }
    }

    private static void DumpType(Type type) throws IOException {
        if(type instanceof ArrayType){
            ArrayType arrayType = (ArrayType) type;
            Type eleType = arrayType.getElementType();
            int len = arrayType.getEleDim();
            if(!eleType.isArrayType()){
                System.out.print("[" + len + " x i32]");
            }
            else{
                System.out.print("[" + len + " x ");
                DumpType(eleType);
                System.out.print("]");
            }
        }
        else if(type instanceof PointerType){
            System.out.print("i32*");
        }
    }
    private static void DumpInitArray(ArrayType arrayType, ArrayList<Value> values) throws IOException {
        Type type = arrayType.getElementType();
        DumpType(arrayType);
        if(type.isArrayType()){
            ArrayType sonType = (ArrayType) type;
            int len = arrayType.getEleDim();
            System.out.print("[");
            for(int i = 0; i < len; i++){
                DumpInitArray(sonType, values);
                if(i != len - 1) System.out.print(", ");
            }
            System.out.print("]");
        }
        else if(type.isPointerType()){
            System.out.print(" ");
            System.out.print("[");
            int len = arrayType.getEleDim();
            for(int i = 0; i < len;i++){
                System.out.print("i32 " + values.get(initArrayNow++).getName());
                if(i != len - 1) System.out.print(", ");
            }
            System.out.print("]");
        }
    }

    private static void DumpGlobalVar(GlobalVar globalVar) throws IOException {
        if(globalVar.getType().isArrayType()){
            System.out.print(globalVar.getName());

            if(globalVar.isConst()){
                System.out.print(" = dso_local constant ");
            }
            else System.out.print(" = dso_local global ");

            ArrayType arrayType = (ArrayType) globalVar.getType();
            //  输出初始值
            ArrayList<Value> values = globalVar.getValues();
            if(values.size() == 0){
                System.out.print(arrayType.toString().replace("*", ""));
                System.out.print(" zeroinitializer\n");
            }
            else {
                //  初始化当前输出的位置
                initArrayNow = 0;
                DumpInitArray(arrayType, values);
            }
        }
//        //  为printf贴心设计
//        else if(globalVar.getType() instanceof StringType){
//            String strName = globalVar.getName();
//            StringType stringType = (StringType) globalVar.getType();
//            //  由于printf的fString中可能由\n等字符，所以要先预处理一下
//            String fString;
//            int len;
//            if(stringType.getMode() == 0) {
//                fString = calFString(stringType.getVal());
//                len = calFStrLen(fString);
//            }
//            else {
//                fString = "%d\\00";
//                len = 3;
//            }
//
//            out.write(strName + " = constant ");
//            out.write("[" + len + " x i8] c");
//            out.write("\"" + fString + "\"");
//        }
        //!
        else {
            if(globalVar.isConstDecl()){
                System.out.print(globalVar.getName() + " = dso_local constant i32 ");
                System.out.print(globalVar.getValue().getName());
            }
            else {
                System.out.print(globalVar.getName() + " = dso_local global i32 ");
                System.out.print(globalVar.getValue().getName());
            }
        }
    }


    private void DumpFunction(Function function) throws IOException {
        System.out.print("define dso_local ");
        if(function.getType().isIntegerType()){
            System.out.print("i32 ");
        }
        else System.out.print("void ");
        System.out.print(function.getName() + "(");


        ArrayList<Argument> arguments = function.getArgs();
        for(int i = 0; i < arguments.size(); i++){
            Argument argument = arguments.get(i);
            System.out.print(argument.toString());
            if(i != arguments.size() - 1) System.out.print(", ");
        }


        System.out.print(") {\n");

        IList<BasicBlock, Function> basicBlocks = function.getBbs();

        for(IList.INode<BasicBlock, Function> bbNode : basicBlocks){
            DumpBasicBlock(bbNode.getValue());
        }

        System.out.println("}\n");
    }



    private void DumpBasicBlock(BasicBlock bb) throws IOException {
        String bbName = bb.getName();
        bbName = bbName.replace("%", "");
        System.out.println(bbName + ":");
        IList<Instruction, BasicBlock> insts = bb.getInsts();
        for(IList.INode<Instruction, BasicBlock> instNode : insts){
            DumpInstruction(instNode.getValue());
            System.out.print("\n");
        }
    }
    private static void DumpInstruction(Instruction inst) throws IOException{
        if(inst instanceof AllocInst){
            System.out.print("    "+inst.getName());
            System.out.print(" = alloca ");
            Type type = inst.getType();
            if(type.isArrayType()){
                ArrayType arrayType = (ArrayType) type;
                String arrStr = arrayType.toString();
                System.out.print(arrStr.replace("*", "") + "\n");
            }
            else {
                PointerType pointerType = (PointerType) inst.getType();
                Type eleType = pointerType.getElementType();
                System.out.print(eleType);
            }
        }else if(inst instanceof BinaryInst){
            BinaryInst binaryInst = (BinaryInst) inst;
            Value left =binaryInst.getLeftVal();
            Value right = binaryInst.getRightVal();
            OP op =binaryInst.getOp();
            switch (op){
                case Add -> {
                    System.out.print("    "+inst.getName() + " = add i32 ");
                }
                case Sub -> {
                    System.out.print("    "+inst.getName() + " = sub i32 ");
                }
                case Mul -> {
                    System.out.print("    "+inst.getName() + " = mul i32 ");
                }
                case Div -> {
                    System.out.print("    "+inst.getName() + " = sdiv i32 ");
                }
                case Mod -> {
                    System.out.print("    "+inst.getName() + " = srem i32 ");
                }
            }
            System.out.print(left.getName() + ", " +right.getName());;
        }else if(inst instanceof BrInst){
            System.out.print("    "+inst.getInstString());
        }else if(inst instanceof CallInst){
            CallInst callInst = (CallInst) inst;
            String FuncName = callInst.getCallFunc().getName();
            System.out.print("    ");
            if(!callInst.getType().isVoidType()){
                System.out.print(callInst.getName() + " = ");
            }

            if(callInst.getType().isVoidType()) System.out.print("call void ");
            else if(callInst.getType().isIntegerType()) System.out.print("call i32 ");

            System.out.print(FuncName);
            System.out.print("(");

            ArrayList<Value> values = callInst.getValues();
            for(int i = 0; i < values.size(); i++){
                Value value = values.get(i);
                System.out.print(value.toString());
                if(i != values.size() - 1) System.out.print(", ");
            }
            System.out.print(")");

        }else if(inst instanceof ConversionInst){
            System.out.print("    "+inst.getInstString());
        }else if(inst instanceof GepInst){
            GepInst gepInst = (GepInst) inst;
            Value target = gepInst.getTarget();
            Type tarType = target.getType();
            if(tarType instanceof ArrayType) {
                ArrayType arrayType = (ArrayType) tarType;
                System.out.print("    "+gepInst.getName() + " = getelementptr ");
                DumpType(arrayType);
                System.out.print(", ");
                DumpType(arrayType);
                System.out.print("* " + target.getName());
            }
            else if(tarType instanceof PointerType){
                System.out.print("    "+gepInst.getName() + " = getelementptr i32, i32* ");
                System.out.print(target.getName());
            }
            ArrayList<Value> indexs = gepInst.getIndexs();
            for(Value index : indexs){
                System.out.print(", i32 " + index.getName());
            }
        }else if(inst instanceof LoadInst){
            System.out.print("    "+inst.getInstString());
        }else if(inst instanceof Move){
            System.out.print("    "+inst.getInstString());
        }else if(inst instanceof RetInst){
            System.out.print("    "+inst.getInstString());
        }else if(inst instanceof StoreInst){
            System.out.print("    "+inst.getInstString());
        }else if(inst instanceof CmpInst){
            CmpInst cmpInst = (CmpInst) inst;
            OP op = cmpInst.getOp();
            System.out.print("    "+cmpInst.getName() + " = icmp ");
            if(op == OP.Eq) System.out.print("eq");
            else if(op == OP.Ne) System.out.print("ne");
            else if(op == OP.Gt) System.out.print("sgt");
            else if(op == OP.Ge) System.out.print("sge");
            else if(op == OP.Lt) System.out.print("slt");
            else if(op == OP.Le) System.out.print("sle");

            Value left = cmpInst.getLeftVal();
            Value right = cmpInst.getRightVal();
            System.out.print(" " + left + ",");
            System.out.print(" " + right.getName());
        }
    }




    private static void ReNameFunc(Function function){
        nowNum = 0;

        ArrayList<Argument> args = function.getArgs();
        for(Argument arg : args){
            arg.setName("%v" + nowNum++);
        }

        IList<BasicBlock, Function> basicBlocks = function.getBbs();
        for (IList.INode<BasicBlock, Function> bbNode : basicBlocks) {
            BasicBlock basicBlock = bbNode.getValue();
            basicBlock.setName("%v" + nowNum++);
            IList<Instruction, BasicBlock> instructions = basicBlock.getInsts();
            for (IList.INode<Instruction, BasicBlock> instNode : instructions) {
                Instruction inst = instNode.getValue();
                if(inst.hasname()){
                    inst.setName("%v" + nowNum++);
                }

            }
        }
    }



}
