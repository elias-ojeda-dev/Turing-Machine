import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TransitionTable {
    private final Map<String, Map<Character, Transition>> transitions = new HashMap<>();

    public TransitionTable(){
        buildDefaultTable();
    }

    private void buildDefaultTable(){
        Map<Character, Transition> q0 = new HashMap<>();
        q0.put('0', new Transition("q1", 'X', 'R'));
        q0.put('Y', new Transition("q3", 'Y', 'R'));
        transitions.put("q0", q0);

        Map<Character, Transition> q1 = new HashMap<>();
        q1.put('0', new Transition("q1", '0', 'R'));
        q1.put('1', new Transition("q2", 'Y', 'L'));
        q1.put('Y', new Transition("q1", 'Y', 'R'));
        transitions.put("q1", q1);

        Map<Character, Transition> q2 = new HashMap<>();
        q2.put('0', new Transition("q2", '0', 'L'));
        q2.put('X', new Transition("q0", 'X', 'R'));
        q2.put('Y', new Transition("q2", 'Y', 'L'));
        transitions.put("q2", q2);

        Map<Character, Transition> q3 = new HashMap<>();
        q3.put('Y', new Transition("q3", 'Y', 'R'));
        q3.put('B', new Transition("q4", 'B', 'R'));
        transitions.put("q3", q3);

        transitions.put("q4", new HashMap<>());
    }

    public Optional<Transition> get(String state, char symbol){
        Map<Character, Transition> row = transitions.get(state);
        if(row == null){
            return Optional.empty();
        }
        return Optional.ofNullable(row.get(symbol));
    }
}
