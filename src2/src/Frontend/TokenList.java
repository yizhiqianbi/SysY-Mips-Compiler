package Frontend;

import java.util.ArrayList;

public class TokenList {
    //        File file = new File ("output.txt");
//        PrintStream ps = new PrintStream(file);
//        System.setOut(ps);
    public final ArrayList<Token> tokens = new ArrayList<>();
    private int index =0;

    public void add(Token token){
        tokens.add(token);
    }

    public boolean hasNext(){
        return index < tokens.size();
    }

    public Token getToken(){
        if(index >= tokens.size()){
            return tokens.get(tokens.size()-1);
        }
        return ahead(0);
    }

    public Token ahead(int count){
        if(index+count>=tokens.size()){
            return tokens.get(tokens.size()-1);
        }
        return tokens.get(index+count);
    }

    public Token consume(){
        System.out.println(tokens.get(index).getType() + " "+ tokens.get(index).getVal());
        if(index >= tokens.size()-1){
            index = tokens.size()-1;
            return tokens.get(index);
        }
        return tokens.get(index++);
    }
//    考虑吃多种
    public Token consume(TokenType... types){
        Token token = tokens.get(index);
        for(TokenType type : types){
            if(token.getType() == type){
                index++;
                return token;
            }
        }
        return null;
    }

    public Token consume(TokenType type){
        Token token = tokens.get(index);
        if(token.getType().equals(type)){
            index++;
            return token;
        }
        return null;
    }



    public int getIndex() {
        return index;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public void back(int backnum){
        index -= backnum;
    }


}
