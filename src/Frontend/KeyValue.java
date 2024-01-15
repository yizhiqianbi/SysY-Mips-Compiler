package Frontend;

public class KeyValue {
    int dim=0;//维度
    int d1=0;//第一个维度数（例如，a[3]就是3）
    int d2=0;//第二个未读数（例如，a[2][4]就是4）
    String AddrType="i32";//这里是取址的类型，主要用于函数调用各种维度时使用
    String intVal="";//0维常数初值存储
    String [] d1Value = null;//1维常数初值存储
    String [][] d2Value = null;//2维常数初值存储


    public int getD1() {
        return d1;
    }

    public int getD2() {
        return d2;
    }

    public int getDim() {
        return dim;
    }

    public String getAddrType() {
        return AddrType;
    }

    public String getIntVal() {
        return intVal;
    }

    public String[] getD1Value() {
        return d1Value;
    }

    public String[][] getD2Value() {
        return d2Value;
    }

    public void setAddrType(String addrType) {
        AddrType = addrType;
    }

    public void setD1(int d1) {
        this.d1 = d1;
    }

    public void setD1Value(String[] d1Value) {
        this.d1Value = d1Value;
    }

    public void setD2(int d2) {
        this.d2 = d2;
    }

    public void setD2Value(String[][] d2Value) {
        this.d2Value = d2Value;
    }

    public void setIntVal(String intVal) {
        this.intVal = intVal;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }
}
