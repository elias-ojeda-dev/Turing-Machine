import java.util.Optional;

public final class TuringMachineEngine {
    private static final char BLANK = 'B';
    private static final char LEFT = 'L';
    private static final char RIGHT = 'R';
    private static final String INITIAL_STATE = "q0";
    private static final String ACCEPT_STATE = "q4";

    private final TransitionTable transitionTable;

    private Tape tape;
    private String currentState;
    private int step;
    private boolean halted;
    private boolean accepted;
    private boolean graphicMode;
    private int maxSteps;
    private SimulationLogger logger;

    @SuppressWarnings("this-escape")
    public TuringMachineEngine(){
        this(new TransitionTable());
    }

    public TuringMachineEngine(TransitionTable transitionTable){
        this.transitionTable = transitionTable;
        reset();
    }

    public void start(String input, boolean graphicMode){
        int extraBlanks;
        if(graphicMode){
            extraBlanks = 10;
        }else{
            extraBlanks = Math.max(20, input.length());
        }

        this.tape = new Tape(input, BLANK, extraBlanks);
        this.currentState = INITIAL_STATE;
        this.step = 0;
        this.halted = false;
        this.accepted = false;
        this.graphicMode = graphicMode;
        if(graphicMode){
            this.maxSteps = 1000;
        }else{
            this.maxSteps = Math.max(10000, tape.size() * 100);
        }
        this.logger = new SimulationLogger("simulacion_turing.txt", input, graphicMode);
    }

    public StepStatus step(){
        if(tape == null || halted){
            if(halted && accepted){
                return StepStatus.ACCEPTED;
            }
            return StepStatus.REJECTED;
        }

        step++;
        char currentSymbol = tape.read();
        boolean shouldLog = graphicMode || step % 10 == 0 || step <= 50;

        Optional<Transition> transOpt = transitionTable.get(currentState, currentSymbol);
        if(!transOpt.isPresent()){
            if(logger != null){
                logger.logNoTransition(step, currentState, currentSymbol, shouldLog);
            }
            halt(false);
            return StepStatus.REJECTED;
        }

        Transition transition = transOpt.get();
        if(logger != null){
            logger.logStep(step, currentState, currentSymbol, transition, shouldLog);
        }

        applyTransition(transition);

        if(currentState.equals(ACCEPT_STATE)){
            if(logger != null){
                logger.logAcceptance(step);
            }
            halt(true);
            return StepStatus.ACCEPTED;
        }

        if(step > maxSteps){
            if(logger != null){
                logger.logLoopLimit(maxSteps);
            }
            halt(false);
            return StepStatus.LOOP_LIMIT;
        }

        return StepStatus.RUNNING;
    }

    private void applyTransition(Transition transition){
        tape.write(transition.getWriteSymbol());
        currentState = transition.getNextState();

        if(transition.getDirection() == RIGHT){
            tape.moveRight();
        }else if(transition.getDirection() == LEFT){
            tape.moveLeft();
        }
    }

    private void halt(boolean accepted){
        this.halted = true;
        this.accepted = accepted;
    }

    public final void reset(){
        this.tape = null;
        this.currentState = INITIAL_STATE;
        this.step = 0;
        this.halted = false;
        this.accepted = false;
        this.logger = null;
        this.graphicMode = true;
        this.maxSteps = 0;
    }

    public Tape getTape(){
        return tape;
    }

    public String getCurrentState(){
        return currentState;
    }

    public int getStep(){
        return step;
    }

    public boolean isHalted(){
        return halted;
    }

    public boolean isAccepted(){
        return accepted;
    }

    public char getBlankSymbol(){
        return BLANK;
    }

    public String getAcceptState(){
        return ACCEPT_STATE;
    }
}
