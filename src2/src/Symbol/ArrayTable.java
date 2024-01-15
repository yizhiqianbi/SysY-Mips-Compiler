package Symbol;

public class ArrayTable extends Symbol {
    private boolean isConst;
    private int dimension;

    public ArrayTable(String table,boolean isConst,int dimension) {
        super(table);
        this.isConst = isConst;
        this.dimension = dimension;
    }

    public int getDimension() {
        return dimension;
    }

    public boolean getIsConst() {
        return isConst;
    }
}
