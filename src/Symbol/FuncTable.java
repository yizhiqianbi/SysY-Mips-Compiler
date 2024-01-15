package Symbol;

import java.util.ArrayList;

public class FuncTable extends Symbol {
    private FuncType type;
    private ArrayList<FuncParam> funcParams;
    public FuncTable(String table,FuncType type, ArrayList<FuncParam> funcParams) {
        super(table);
        this.funcParams = funcParams;
        this.type = type;
    }


    public ArrayList<FuncParam> getFuncParams() {
        return funcParams;
    }

    public FuncType getType() {
        return type;
    }


}
