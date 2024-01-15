package LLVMIR;

import LLVMIR.Value.User;
import LLVMIR.Value.Value;

import javax.lang.model.element.VariableElement;

public class Use {
    private Value value;
    private final User user;
    /**
     * @param pos
     * ! pos表示该value在该user的operandList中的pos
     */

    private int pos;


    public Use(Value value,User user,int pos){
        this.value = value;
        this.user = user;
        this.pos = pos;
    }

    public Use(Value value,User user){
        this.value = value;
        this.user = user;
    }


    public int getPos() {
        return pos;
    }

    public User getUser() {
        return user;
    }

    public Value getValue() {
        return value;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public void setValue(Value value) {
        this.value = value;
    }


}
