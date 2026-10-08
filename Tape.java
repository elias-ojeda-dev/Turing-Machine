import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tape {
    private final List<Character> symbols;
    private final char blankSymbol;
    private int headPosition;

    public Tape(String input, char blankSymbol, int extraBlanks){
        this.blankSymbol = blankSymbol;
        this.symbols = new ArrayList<>();

        for(char c : input.toCharArray()){
            symbols.add(c);
        }
        for(int i=0; i<extraBlanks; i++){
            symbols.add(blankSymbol);
        }

        headPosition = 0;
    }

    public char read(){
        ensureWithinBounds();
        return symbols.get(headPosition);
    }

    public void write(char symbol){
        ensureWithinBounds();
        symbols.set(headPosition, symbol);
    }

    public void moveLeft(){
        headPosition--;
        if(headPosition < 0){
            symbols.add(0, blankSymbol);
            headPosition = 0;
        }
    }

    public void moveRight(){
        headPosition++;
        if(headPosition >= symbols.size()){
            symbols.add(blankSymbol);
        }
    }

    private void ensureWithinBounds(){
        if(headPosition >= symbols.size()){
            symbols.add(blankSymbol);
        }
        if(headPosition < 0){
            symbols.add(0, blankSymbol);
            headPosition = 0;
        }
    }

    public int getHeadPosition(){
        return headPosition;
    }

    public List<Character> getSymbols(){
        return Collections.unmodifiableList(symbols);
    }

    public int size(){
        return symbols.size();
    }
}
