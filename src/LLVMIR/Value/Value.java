package LLVMIR.Value;

import LLVMIR.Type.Type;
import LLVMIR.Use;

import java.util.ArrayList;
import java.util.List;

public class Value {
    private String name;
    private Type type;
    private ArrayList<Use> useArrayList;

    //!用来给value命名
    public static int valNumber = -1;
    public Value() {};

    public Value(String name,Type type){
        this.name = name;
        this.type = type;
        this.useArrayList = new ArrayList<Use>();
    }

    @Override
    public String toString() {
        return this.type+" "+this.name;
    }

    public void addUse(Use use){
        if(this.useArrayList ==null) this.useArrayList = new ArrayList<Use>();
        useArrayList.add(use);

    }

    public void addUses(List<Use> uses){
        if(this.useArrayList ==null) this.useArrayList = new ArrayList<Use>();
        useArrayList.addAll(uses);
    }

    // !!----------------------------------------------------------------

    public void removeUseByUser(User user) {
        ArrayList<Use> tmpUseList = new ArrayList<>(useArrayList);
        for (Use use : useArrayList) {
            if (use.getUser().equals(user)) {
                tmpUseList.remove(use);
            }
        }
        this.useArrayList = tmpUseList;
    }

    public void removeUse(Use use){
        if(useArrayList!=null){
            if(useArrayList.contains(use)){
                useArrayList.remove(use);
            }
        }
    }

    public void removeOneUseByUser(User user){
        for(int i=0;i<useArrayList.size();i++){
            if(useArrayList.get(i).getUser().equals(user)){
                useArrayList.remove(i);
                break;
            }
        }
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Use> getUseArrayList() {
        return useArrayList;
    }

    public static int getValNumber() {
        return valNumber;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void setValNumber(int valNumber) {
        Value.valNumber = valNumber;
    }

    public void setUseArrayList(ArrayList<Use> useArrayList) {
        this.useArrayList = useArrayList;
    }


    public boolean isNumber() {
        return this instanceof ConstInteger;
    }
}
