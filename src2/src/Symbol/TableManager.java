package Symbol;

import com.sun.jdi.Value;

import java.util.ArrayList;
import java.util.HashMap;

public class TableManager {
//    private ArrayList<HashMap>
    private final ArrayList<HashMap<String, TableValue>> symTbls = new ArrayList<>();
    public void pushSymTbl(){
        symTbls.add(new HashMap<>());
    }

    public void popSymTbl(){
        int len = symTbls.size();
        symTbls.remove(len - 1);
    }

    public HashMap<String, TableValue> getNowSymTbl(){
        int len = symTbls.size();
        return symTbls.get(len - 1);
    }

    public TableValue find(String ident){
        int len = symTbls.size();
        for(int i = len - 1; i >= 0; i--){
            HashMap<String, TableValue> symTbl = symTbls.get(i);
            TableValue res = symTbl.get(ident);
            if(res != null){
                return res;
            }
        }
        return null;
    }




}
