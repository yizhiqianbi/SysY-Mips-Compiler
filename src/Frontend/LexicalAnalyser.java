package Frontend;
import java.io.*;
import Error.*;
public class LexicalAnalyser {
    private static final LexicalAnalyser instance;
    private static final FileReader fileReader;
    private static final PushbackReader pushbackReader;
    private TokenList tokenList = new TokenList();

    private int readin;

    private char c;

    private int lineNum=1;
    private int readIndex=0;

    static {
        try {

            instance = new LexicalAnalyser();
            fileReader = new FileReader("testfile.txt");
            pushbackReader = new PushbackReader(fileReader);

        }catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static LexicalAnalyser getInstance() {
        return instance;
    }

//读入单个单词
    private char getChar() throws IOException {
        readin = pushbackReader.read();
        c=(char)readin;
        return c;
    }

    private char ungetChar() throws IOException {
        pushbackReader.unread(readin);
        c=(char)readin;
        return c;
    }


    private Token getToken() throws IOException {
        StringBuilder sb = new StringBuilder();
        while ((readin = pushbackReader.read())!=-1) {
            c=(char) readin;
            while (c == ' ' || c == '\t' || c == '\n'|| c == '\r'){
                if(c == '\n') lineNum++;
                readin = pushbackReader.read();
                c = (char)readin;
            }
            if(isAlpha(c) || c=='_'){
                return Ident();
            }
            if(isDigit(c)){
                return MNumber();
            }
            if(c=='"'){
                return MString();
            }

            switch (c){
                case '/':
                    getChar();
                    if(c != '*' && c!= '/'){
                        pushbackReader.unread(c);
                        return new Token(TokenType.DIV,"/",lineNum);
                    }
                    else Comment();
                    break;
                case '&':
                    getChar();
                    if(c=='&') return new Token(TokenType.AND , "&&" , lineNum);
                    else LexError();
                    break;
                case '|':
                    getChar();
                    if(c=='|') return new Token(TokenType.OR,"||",lineNum);
                    else LexError();
                    break;
                case '+':
                    return new Token(TokenType.PLUS, "+",lineNum );
                case '-':
                    return new Token(TokenType.MINU, "-",lineNum );
                case '*':
                    return new Token(TokenType.MULT, "*",lineNum );
                case '%':
                    return new Token(TokenType.MOD, "%",lineNum );
                case '!':
                    if(getChar()=='='){
                        return new Token(TokenType.NEQ,"!=",lineNum);
                    }
                    else{
//                            回退一格,但是c不变
                        pushbackReader.unread(readin);
                        return new Token(TokenType.NOT,"!",lineNum);
                    }
                case '<':
                    if(getChar()=='=') {
                        return new Token(TokenType.LEQ, "<=", lineNum);
                    }

                    else{
                        pushbackReader.unread(readin);
                        return new Token(TokenType.LSS, "<", lineNum);
                    }

                case '>':
                    if(getChar()=='=') {
                        return new Token(TokenType.GEQ, ">=", lineNum);
                    }
                    else{
                        pushbackReader.unread(readin);
                        return new Token(TokenType.GRE, ">", lineNum);
                    }
                case '=':
                    if(getChar()=='=') {
                        return new Token(TokenType.EQL, "==", lineNum);
                    }
                    else{
                        pushbackReader.unread(readin);
                        return new Token(TokenType.ASSIGN, "=", lineNum);
                    }
                case ';':
                    return new Token(TokenType.SEMICN,";", lineNum);
                case ',':
                    return new Token(TokenType.COMMA,",",lineNum);

                case '(':
                    return new Token(TokenType.LPARENT,"(",lineNum);
                case ')':
                    return new Token(TokenType.RPARENT,")",lineNum);
                case '[':
                    return new Token(TokenType.LBRACK,"[",lineNum);
                case ']':
                    return new Token(TokenType.RBRACK,"]",lineNum);

                case '{':
                    return new Token(TokenType.LBRACE,"{",lineNum);

                case '}':
                    return new Token(TokenType.RBRACE,"}",lineNum);
                default: return null;
            }
        }
        return null;
    }

    private Token Ident() throws IOException{
        StringBuilder sb = new StringBuilder();
        while (isAlphaOrDigit(c) || c== '_'){
            sb.append(c);
            getChar();
        }
        pushbackReader.unread(readin);
        String ident = sb.toString();
        return switch (ident) {
//            case 'IntConst' -> new Token(TokenType.INTCON,"IntConst",lineNum);
//            我是滞涨，写了一堆可以用ident代替的东西
            case "main"-> new Token(TokenType.MAINTK,ident,lineNum);
            case "const"-> new Token(TokenType.CONSTTK,ident,lineNum);
            case "int" -> new Token(TokenType.INTTK,ident,lineNum);
            case "break" -> new Token( TokenType.BREAKTK,ident,lineNum);
            case "continue" -> new Token( TokenType.CONTINUETK,ident,lineNum);
            case "if" -> new Token(TokenType.IFTK, ident, lineNum);
            case "else" -> new Token(TokenType.ELSETK, ident,lineNum);
            case "for" -> new Token(TokenType.FORTK, ident, lineNum);
            case "getint" -> new Token(TokenType.GETINTTK, ident,lineNum);
            case "printf" -> new Token(TokenType.PRINTFTK, ident,lineNum);
            case "return" -> new Token(TokenType.RETURNTK,ident, lineNum);
            case "void" -> new Token(TokenType.VOIDTK, ident,lineNum);
            default -> new Token(TokenType.IDENFR, ident,lineNum);
        };
    }

    private Token MString() throws IOException{
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        while (getChar() != '"'){
            sb.append(c);
        }
        sb.append('"');
        String  str = sb.toString();
        char[] charArray = str.toCharArray();
        int len = charArray.length;
        char d;
        for(int i = 1; i < len-1;i++){
            d=charArray[i];
            if(!(d == 32 || d == 33 || (d >= 40 && d <= 126) || d == 37)){
                ErrorHandler.getInstance().addError(ErrorType.a,lineNum);
//                System.out.println("LEX ERROR 1" + d);
            }
            else if(d==37){
                if(charArray[i+1]!='d'){
                    ErrorHandler.getInstance().addError(ErrorType.a,lineNum);
//                    System.out.println("LEX ERROR 2");
                }
            }
            else if(d == 92){
                if(charArray[i+1]!='n'){
                    ErrorHandler.getInstance().addError(ErrorType.a,lineNum);
//                    System.out.println("LEX ERROR 3");
                }
            }
        }
        return new Token(TokenType.STRCON,str,lineNum);
    }

    /*
    * boolean isFloat = false;
        boolean isHex = false;
        boolean isOct;
        StringBuilder numBuilder = new StringBuilder();
        isOct = (c == '0');
        while (isNumber(c) || c == 'x' || c == 'X' || c == '+' || c == '-'
                || c == 'p' || c == 'P' || c == '.' ||
                (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')){
            if(c == '.'){
                isFloat = true;
                isOct = false;
            }
            else if(c == 'X' || c == 'x'){
                isHex = true;
                isOct = false;
            }
            else if(c == 'p' || c == 'P'){
                isFloat = true;
                isHex = true;
            }
            else if((c == 'e' || c == 'E') && !isHex){
                isFloat = true;
            }
            else if(c == '+' || c == '-'){
                String nowStr = numBuilder.toString();
                char last = nowStr.charAt(nowStr.length() - 1);
                if(last != 'p' && last != 'P' && last != 'e' && last != 'E'){
                    break;
                }
                if((last == 'e' || last == 'E') && !isFloat){
                    break;
                }
            }
            numBuilder.append(c);
            readChar();
        }
        in.unread(c);
        String num = numBuilder.toString();
        if(num.equals("0")) isOct = false;
        if(isFloat && isHex) return new Token(TokenType.HEXFCON, num);
        else if(isFloat) return new Token(TokenType.DECFCON, num);
        else if(isHex) return new Token(TokenType.HEXCON, num);
        else if(isOct) return new Token(TokenType.OCTCON, num);
        else return new Token(TokenType.DECCON, num);
*/

    private Token MNumber() throws IOException{
        StringBuilder sb = new StringBuilder();
        sb.append(c);

        while (true){
            getChar();
            if(isAlphaOrDigit(c) && isAlphaOrDigit(c)){
                sb.append(c);
            }

            else if(!isDigit(c) && isAlphaOrDigit(c) ){
                LexError();
                break;
            }
            else break;
        }
        String num = sb.toString();
        pushbackReader.unread(readin);
        return new Token(TokenType.INTCON,num,lineNum);
    }

    private void Comment() throws IOException{
        if(c=='/'){
            while (c != '\n' && c != '\uFFFF'){
                getChar();
            }
            lineNum++;
        }
        else if(c == '*'){
            while (true){
                if(readin == -1) return ;
                if(getChar()=='*'){
                    if(getChar()!='/'){
                        pushbackReader.unread(readin);
                    }
                    else{
                        return;
                    }
                }
//                todo 不确定放哪里合适
                if(readin == -1) return ;
            }
        }
    }

    private void LexError(){

    }



    //    检测读入
    private boolean isAlpha(char x){
        return (x >= 'a' && x <= 'z') || (x >='A' && x <= 'Z');
    }

    private boolean isDigit(char x){
        return (x >= '0' && x<='9');
    }

    private boolean isAlphaOrDigit(char x){
        return (x <= 'z' && x >= 'a') || (x <= 'Z' && x >= 'A') || (x <= '9' && x >= '0');
    }

    public TokenList lex() throws IOException{

//        File file = new File ("output.txt");
//        PrintStream ps = new PrintStream(file);
//        System.setOut(ps);
//        System.setOut(ps);
        while (true){
            Token token = getToken();
            if(token == null) break;
            tokenList.add(token);


//            System.out.println(token.getType().toString()+" "+token.getVal());
        }
        return tokenList;
    }

    public TokenList getTokenList() {
        return tokenList;
    }
}
