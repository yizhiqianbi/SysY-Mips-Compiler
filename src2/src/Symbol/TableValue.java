package Symbol;

import Frontend.Token;
import Frontend.TokenType;

import java.util.function.ToDoubleBiFunction;

public class TableValue {
    private int line;
    private int num;
    private FuncType funcType;
    private int state;
    public TableValue(){
        this.state = 0;
    }
    public TableValue(int line){
        this.line = line;
    }


    public void setLine(int line) {
        this.line = line;
    }
}
