package LLVMIR.Value;

import LLVMIR.IRModule;
import LLVMIR.Type.Type;
import LLVMIR.Value.Instruction.Instruction;
import Utils.DataStruct.IList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Function extends Value{
    private final IList<BasicBlock, Function> bbs;
    private final ArrayList<Argument> args;
    private boolean mayHasSideEffect;
    private boolean useGV;
    private boolean isLibFunction = false;
    private final HashSet<GlobalVar> loadGVs;
    private final HashSet<GlobalVar> storeGVs;

    //  callerList记录调用这个function的其他函数
    private final ArrayList<Function> callerList;
    //  calleeList记录这个function调用的其他函数
    private final ArrayList<Function> calleeList;
    private BasicBlock Exit;

    private BasicBlock bbEntry;


    //  Function的Type就是它返回值的type
    public Function(String name, Type type){
        super(name, type);
        this.bbs = new IList<>(this);
        this.args = new ArrayList<>();
        this.callerList = new ArrayList<>();
        this.calleeList = new ArrayList<>();
        this.loadGVs = new HashSet<>();
        this.storeGVs = new HashSet<>();
    }
    public Function(String name, Type type, IList<BasicBlock, Function> bbs, ArrayList<Argument> args){
        super(name, type);
        this.bbs = bbs;
        this.args = args;
        this.callerList = new ArrayList<>();
        this.calleeList = new ArrayList<>();
        this.loadGVs = new HashSet<>();
        this.storeGVs = new HashSet<>();
    }

    public ArrayList<Function> getCallerList(){
        return callerList;
    }

    public void addArg(Argument argument){ args.add(argument); }
    public boolean isLibFunction() {
        return isLibFunction;
    }

    public ArrayList<Function> getCalleeList() {
        return calleeList;
    }


    public void addCaller(Function function){
        if(!callerList.contains(function)) {
            this.callerList.add(function);
        }
    }

    public void addCallee(Function function){
        if(!calleeList.contains(function)) {
            this.calleeList.add(function);
        }
    }


    public ArrayList<Argument> getArgs() {
        return args;
    }

    public IList<BasicBlock, Function> getBbs() {
        return bbs;
    }


    public void setBbEntry(BasicBlock bbEntry) {
        this.bbEntry = bbEntry;

    }
}
