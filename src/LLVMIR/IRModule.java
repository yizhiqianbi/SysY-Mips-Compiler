package LLVMIR;

import Frontend.AST.MainFuncDefAST;
import LLVMIR.Value.GlobalVar;
import LLVMIR.Value.Function;
import java.util.ArrayList;

public class IRModule {
    private final ArrayList<Function> functions;
    private final ArrayList<GlobalVar> globalVars;



    public IRModule(ArrayList<Function> functions, ArrayList<GlobalVar> globalVars){
        this.functions = functions;
        this.globalVars = globalVars;
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public ArrayList<GlobalVar> getGlobalVars() {
        return globalVars;
    }

    public void addGlobalVar(GlobalVar globalVar){
        globalVars.add(globalVar);
    }

    public void addFunction(Function function){
        functions.add(function);
    }

}
