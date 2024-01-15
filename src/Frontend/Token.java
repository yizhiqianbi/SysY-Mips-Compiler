package Frontend;

public class Token {
    private TokenType type;
    private String val;

    private int tokenLine;


    public Token(TokenType type, String val ,int tokenLine) {
        this.type = type;
        this.val = val;
        this.tokenLine = tokenLine;
    }

    public TokenType getType(){
        return type;
    }

    public String getVal() {
        return val;
    }

    public int getTokenLine(){
        return tokenLine;
    }

}
