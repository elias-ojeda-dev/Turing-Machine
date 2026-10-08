public class Transition {
    private final String nextState;
    private final char writeSymbol;
    private final char direction;

    public Transition(String nextState, char writeSymbol, char direction){
        this.nextState = nextState;
        this.writeSymbol = writeSymbol;
        this.direction = direction;
    }

    public String getNextState(){
        return nextState;
    }

    public char getWriteSymbol(){
        return writeSymbol;
    }

    public char getDirection(){
        return direction;
    }
}
