package Symbol;

public abstract class Symbol {
    private String table;

    public Symbol(String table) {
        this.table = table;
    }

    public String getTable() {
        return table;
    }
}
