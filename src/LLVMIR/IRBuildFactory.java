package LLVMIR;

import LLVMIR.Type.*;
import LLVMIR.Value.*;
import LLVMIR.Value.Instruction.*;
import com.sun.jdi.FloatType;
import com.sun.source.tree.ArrayAccessTree;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class IRBuildFactory {
    private IRBuildFactory(){}

    private static final IRBuildFactory f = new IRBuildFactory();

    public static IRBuildFactory getInstance(){
        return f;
    }


    //构建Function
    public Function buildFunction(String name,String type,IRModule module){
        Function function;
        if(type.equals("int")){
            function = new Function(name,new IntegerType(32));
        }else{
            function = new Function(name,new VoidType());
        }
        module.addFunction(function);
        return function;
    }
    //数组参数
    public Argument buildArgument(String name, ArrayList<Integer> dimList, Function parentFunc){
        Type type;
        if(dimList.size() == 0) type = new PointerType(new IntegerType(32));
        else type = buildArrayType(dimList);
        Argument argument = new Argument(name, type, parentFunc);
        parentFunc.addArg(argument);
        return argument;
    }

    public Argument buildArgument(String name, String typeStr, Function parentFunc){
        Argument argument;
        if(typeStr.equals("int")) argument = new Argument(name, new IntegerType(32), parentFunc);
        else argument = new Argument(name, new VoidType(), parentFunc);
        parentFunc.addArg(argument);
        return argument;
    }



    //endregion



    //region GlobalVar
    public GlobalVar buildGlobalVar(String name,Type type, boolean isConst, Value initValue,ArrayList<GlobalVar> globalVars){
        GlobalVar globalVar = new GlobalVar(name, type,isConst, initValue);
        globalVars.add(globalVar);
        return globalVar;
    }
    public GlobalVar buildGlobalVar(String name, ArrayList<Integer> dimList, ArrayList<Value> initValS ,boolean isConst, ArrayList<GlobalVar> globalVars){
        ArrayType arrayType = (ArrayType) buildArrayType(dimList);
        GlobalVar globalVar = new GlobalVar(name, arrayType, isConst, initValS);
        globalVars.add(globalVar);
        return globalVar;
    }
    private Type buildArrayType(ArrayList<Integer> dimList){
        if(dimList.size() == 0){
            return new PointerType(new IntegerType(32));
        }
        ArrayList<Integer> tmpDimList = new ArrayList<>(dimList);
        int eleDim = tmpDimList.get(0);
        tmpDimList.remove(0);
        return new ArrayType(buildArrayType(tmpDimList), eleDim);
    }

    public AllocInst buildArray(String name, ArrayList<Integer> dimList, BasicBlock bb, boolean isConst){
        ArrayType arrayType = (ArrayType) buildArrayType(dimList);
        AllocInst allocInst = new AllocInst(name, arrayType, bb, isConst);
        bb.addInst(allocInst);
        return allocInst;
    }






    //endregion


    //region arguments
//    public Argument buildArgument(String name, ArrayList<Integer> dimList, Function parentFunc){
//        Type type;
//        if(dimList.size() == 0) type = new PointerType(new IntegerType(32));
//        else type = buildArrayType(dimList);
//        Argument argument = new Argument(name, type, parentFunc);
//        parentFunc.addArg(argument);
//        return argument;
//    }
//
//    public Argument buildArgument(String name, String typeStr, Function parentFunc){
//        Argument argument;
//        if(typeStr.equals("int")) argument = new Argument(name, new IntegerType(32), parentFunc);
//        else argument = new Argument(name, new VoidType(), parentFunc);
//        parentFunc.addArg(argument);
//        return argument;
//    }
//
//    private Type buildArrayType(ArrayList<Integer> dimList){
//        if(dimList.size() == 0){
//            return new PointerType(new IntegerType(32));
//        }
//        ArrayList<Integer> tmpDimList = new ArrayList<>(dimList);
//        int eleDim = tmpDimList.get(0);
//        tmpDimList.remove(0);
//        return new ArrayType(buildArrayType(tmpDimList), eleDim);
//    }

    //endregion


    //region Alloc and store and load
    //!普通
    public AllocInst getAllocInst(Type type){
        Type pointerTy = new PointerType(type);
        return new AllocInst(pointerTy);
    }
    //!数组
    public Instruction getAllocInst(Type type, ArrayList<Value> initValues){
        Type pointerTy = new PointerType(type);
        AllocInst allocInst = new AllocInst(pointerTy);
        allocInst.setInitValues(initValues);
        return allocInst;
    }
    public AllocInst buildAllocInst(Type type, BasicBlock bb){
        Type pointerTy = new PointerType(type);
        AllocInst allocInst = new AllocInst(pointerTy);
        bb.addInst(allocInst);
        return allocInst;
    }
    public LoadInst getLoadInst(Value pointer){
        Type type = ((PointerType) pointer.getType()).getElementType();
        return new LoadInst(pointer, type);
    }
    public LoadInst buildLoadInst(Value pointer, BasicBlock bb){
        Type type = ((PointerType) pointer.getType()).getElementType();
        LoadInst loadInst = new LoadInst(pointer, type);
        bb.addInst(loadInst);
        return loadInst;
    }

    public StoreInst getStoreInst(Value value, Value pointer){
        return new StoreInst(value, pointer);
    }



    public GepInst buildGepInst(Value target, ArrayList<Value> indexs,BasicBlock bb){
        //  索引的第一个参数不会改变类型
        ArrayList<Value> tmpIndexs = new ArrayList<>(indexs);
        tmpIndexs.remove(0);
        GepInst gepInst = new GepInst(indexs, target, calGepType(target.getType(), tmpIndexs), bb);
        bb.addInst(gepInst);
        return gepInst;
    }


    public StoreInst buildStoreInst(Value value, Value pointer, BasicBlock bb){
        StoreInst storeInst =  new StoreInst(value, pointer);
        bb.addInst(storeInst);
        return storeInst;
    }



    //endregion

    //region ret
    public RetInst getRetInst(BasicBlock bb){
        return new RetInst();
    }
    public RetInst buildRetInst(BasicBlock bb){
        Value voidValue = new Value("void", VoidType.voidType);
        RetInst retInst = new RetInst(voidValue);
        bb.addInst(retInst);
        retInst.setHasname(true);
        return retInst;
    }
    public RetInst buildRetInst(BasicBlock bb,Value value){
        RetInst retInst = new RetInst(value);
        bb.addInst(retInst);
        retInst.setHasname(true);
        return retInst;
    }
    //endregion


    //region call
    public CallInst buildCallInst(BasicBlock bb, Function callFunc, ArrayList<Value> values){
        CallInst callInst = new CallInst(callFunc, values);
        bb.addInst(callInst);

        buildCallRelation(bb.getParentFunction(), callFunc);
        return callInst;
    }
    public CallInst buildCallInst(BasicBlock bb, Function callFunc){
        CallInst callInst = new CallInst(callFunc);
        bb.addInst(callInst);

        buildCallRelation(bb.getParentFunction(), callFunc);
        return callInst;
    }
    private void buildCallRelation(Function caller, Function callee){
        if(callee.isLibFunction()) return;
        if(!callee.getCallerList().contains(caller)) {
            callee.addCaller(caller);
        }
        if(!caller.getCalleeList().contains(callee)){
            caller.addCallee(callee);
        }
    }

    //endregion

    //!----------BasicBlock------------------------------------------------------
    public BasicBlock buildBasicBlock(Function parentBasicBlock){
        BasicBlock bb = new BasicBlock(parentBasicBlock);
        parentBasicBlock.getBbs().add(bb.getNode());
        return bb;
    }

    public BinaryInst buildBinaryInst(OP op, Value left, Value right,BasicBlock bb){
        BinaryInst binaryInst = new BinaryInst(op, left, right, IntegerType.I32);
        bb.addInst(binaryInst);
        return binaryInst;
    }

    public Const buildNumber(int val){
        return new ConstInteger(val);
    }

    public ConversionInst buildConversionInst(Value value, OP op, BasicBlock bb){
        Type type = null;
        if(op == OP.Ftoi || op == OP.Zext){
            type = IntegerType.I32;
        }
        else if(op == OP.BitCast){
            type = new PointerType(IntegerType.I32);
        }

        ConversionInst conversionInst = new ConversionInst(value, type, op);
        bb.addInst(conversionInst);
        return conversionInst;
    }


    public CmpInst buildCmpInst(Value left, Value right, OP op, BasicBlock bb){
        CmpInst cmpInst = new CmpInst(left, right, op);
        bb.addInst(cmpInst);
        return cmpInst;
    }

    public BrInst buildBrInst(Value judVal, BasicBlock left, BasicBlock right, BasicBlock basicBlock){
            BrInst brInst = new BrInst(judVal, left, right);
            basicBlock.addInst(brInst);
            basicBlock.getNxtBlocks().clear();
            basicBlock.setNxtBlock(left);
            basicBlock.setNxtBlock(right);
            left.setPreBlock(basicBlock);
            right.setPreBlock(basicBlock);
            return brInst;
    }

    public BrInst buildBrInst(BasicBlock jumpBB, BasicBlock basicBlock){
            BrInst brInst = new BrInst(jumpBB);
            basicBlock.addInst(brInst);
            //  前驱后继关系
            basicBlock.getNxtBlocks().clear();
            basicBlock.setNxtBlock(jumpBB);
            jumpBB.setPreBlock(basicBlock);
            return brInst;
    }






    //!------Call Function----------------------------------------------------------
//    private void buildCallRelation(Function caller, Function callee){
//        if(callee.isLibFunction()) return;
//        if(!callee.getCallerList().contains(caller)) {
//            callee.addCaller(caller);
//        }
//        if(!caller.getCalleeList().contains(callee)){
//            caller.addCallee(callee);
//        }
//    }
//    public CallInst buildCallInst(Function callFunc, ArrayList<Value> values, BasicBlock bb){
//        CallInst callInst = new CallInst(callFunc, values);
//        bb.addInst(callInst);
//
//        buildCallRelation(bb.getParentFunction(), callFunc);
//        return callInst;
//    }


    private int calculate(int a, int b, String op){
        return switch (op){
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            case "%" -> a % b;
            case "<" -> a < b ? 1 : 0;
            case "<=" -> a <= b ? 1 : 0;
            case ">" -> a > b ? 1 : 0;
            case ">=" -> a >= b ? 1 : 0;
            case "==" -> a == b ? 1 : 0;
            case "!=" -> a != b ? 1 : 0;
            default -> 0;
        };
    }


    private Type calGepType(Type tarType, ArrayList<Value> indexs){
        if(indexs.size() == 0) return tarType;
        if(!tarType.isArrayType()){
            return new IntegerType(32);
        }
        ArrayType arrayType = (ArrayType) tarType;
        Type eleType = arrayType.getElementType();
        ArrayList<Value> tmpIndexs = new ArrayList<>(indexs);
        tmpIndexs.remove(0);
        return calGepType(eleType, tmpIndexs);
    }












}
