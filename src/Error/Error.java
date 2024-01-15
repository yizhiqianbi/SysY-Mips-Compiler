package Error;

public class Error implements Comparable<Error> {
    private int errorLine;
    private ErrorType errorType;

    public ErrorType getErrorType() {
        return errorType;
    }
    public int getErrorLine() {
        return errorLine;
    }

    public Error(int errorLine, ErrorType errorType){
        this.errorLine = errorLine;
        this.errorType = errorType;
    }


    @Override
    public String toString() {
        return errorLine+" "+errorType.toString();
    }


    @Override
    public int compareTo(Error o) {
        if (errorLine == o.errorLine) return 0;
        if (errorLine < o.errorLine) return -1;
        return 1;
    }
}
