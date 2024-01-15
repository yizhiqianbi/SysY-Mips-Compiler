package Frontend.AST;

import Frontend.KeyValue;
import Frontend.Token;

public class IASTNode {
    public String globalIdent = "";
    //!这事局部符号栈用的，捏毛毛的，shit一样的结构，之后一定要改
    protected Token ident ;
    public boolean isGlobal = false;
    public boolean isFunc = false;

    public String funcType = "";



    private int level = -1;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public KeyValue keyValue = new KeyValue();

    public KeyValue getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(KeyValue keyValue) {
        this.keyValue = keyValue;
    }



    protected int regID = -1;

    public int getRegID() {
        return regID;
    }

    public void setRegID(int regID) {
        this.regID = regID;
    }
    protected String returnType;

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getReturnType() {
        return returnType;
    }

    protected String returnValue;

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }
    public String getPrintLLVM(){
        if(regID!=-1){
            Integer integer = regID;
            return "%" + integer.toString();
        }else if(isGlobal){
            return "@" + globalIdent;
        }
        else if(returnValue!=null){
            return returnValue;
        }else {
            return "Error: Unknown return value";
        }
    }

    public Token getIdent() {
        return ident;
    }

    public void setIdent(Token ident) {
        this.ident = ident;
    }
}
