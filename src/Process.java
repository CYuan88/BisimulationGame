import java.util.HashSet;
import java.util.Set;

public class Process {
    //three attributes of process: transitions, states, actions
    private Set<Transition> transitions;
    private Set<String> states;
    private Set<String> actions;

    public Process() {
        this.transitions = null;
        this.states = null;
        this.actions = null;
    }
    public Process(Set<Transition> transitions, Set<String> states, Set<String> actions) {
        this.transitions = transitions;
        this.states = states;
        this.actions = actions;
    }

    public Set<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(Set<Transition> transitions) {
        this.transitions = transitions;
    }

    public Set<String> getStates() {
        return states;
    }

    public void setStates(Set<String> states) {
        this.states = states;
    }

    public Set<String> getActions() {
        return actions;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    //get the bisimulation relation after computing the process
    public Set<String> computeBisimulation2(Process process){
        Set<String> R = getR0(process.getStates()); //R0 set that won't change data of it
        Set<String> actions = process.getActions();
        Set<Transition> transitions = process.getTransitions();
        Set<String> states = process.getStates();
        Set<String> R0 = new HashSet<>(R);
        while (true){
            Set<String> R1 = getResults(actions,transitions,R,R0);
            if (R1.containsAll(R0)){
                return R1;
            }else {
                R0.clear();
                R0.addAll(R1);

            }
        }
    }

    //calculate the relation set once
    public Set<String> getResults(Set<String> actions, Set<Transition> transitions, Set<String> R, Set<String> R0){
        Set<String> R1 = new HashSet<>();
        Set<String> states = new HashSet<>();
        //get all states from LTS
        for (String data : R){
            String[] element = data.split(",");
            states.add(element[0]);
            states.add(element[1]);
        }
        for (String x:states){
            for (String x_prime:states){
                if(searchRelationPlus(x,x_prime,R,R0,transitions)&&searchRelationPlus(x_prime,x,R,R0,transitions)){
                    String relation = x + "," + x_prime;
                    R1.add(relation);
                }
            }
        }

        return R1;
    }

    //get the result of search x,x',R0
    public boolean searchRelationPlus(String x, String x_prime, Set<String> R, Set<String> R0, Set<Transition> transitions){

        Set<String> states = new HashSet<>();
        //get all states from R
        for (String relation: R){
            String[] elements = relation.split(",");
            states.add(elements[0]);
            states.add(elements[1]);
        }
        Set<Transition> possible_transitions = new HashSet<>();
        //get all possible transitions started from x
        //all possible states from the possible transition
        Set<String> possible_states = new HashSet<>();
        for (Transition transition: transitions){
            String source = transition.getSource();
            if (source.equals(x)){
                possible_transitions.add(transition);
                //get possible targets
                possible_states.add(transition.getDestination());
            }
        }
        if (possible_transitions.size() == 0){
            return true;
        }

        Set<Boolean> resultSet = new HashSet<>();
        Set<Integer> possible_results = new HashSet<>();
        for (String y: possible_states){
            for (String a: actions){
                //if find x-a-y
                if (searchActionDestination(a,y,transitions)){
                    Set<Transition> possible_transitions_prime = new HashSet<>();
                    //get all possible transitions started from x_prime
                    for (Transition transition: transitions){
                        if (transition.getSource().equals(x_prime)){
                            possible_transitions_prime.add(transition);
                        }
                    }
                    Set<String> possible_target = new HashSet<>();
                    for (Transition transition: possible_transitions_prime){
                        if (transition.getAction().equals(a)&&transition.getSource().equals(x_prime)){
                            possible_target.add(transition.getDestination());
                        }
                    }
                    if (possible_target.size() != 0) {
                        possible_results.add(0);
                        for (String y_prime : possible_target) {
                            String y_y_prime = y + "," + y_prime;

                            if (searchRelation(y_y_prime, R0)) {
                                resultSet.add(true);
                            }
                        }
                    }else {
                        return false;
                    }
                }
            }
        }
        if (resultSet.size()!=0 &&resultSet.size() == possible_results.size()){
            return true;
        }
        return false;
    }

    public Set<String> getR0(Set<String> states){
        Set<String> R0 = new HashSet<>();
        for (String x: states){
            for (String x_prime: states){
                String relation = x + "," + x_prime;
                R0.add(relation);
            }
        }
        return R0;
    }

    public boolean searchTransition(Transition transition, Set<Transition> transitions){
        boolean result = false;
        for (Transition data: transitions){
            if (data.getAction().equals(transition.getAction())&&data.getSource().equals(transition.getSource())&&data.getDestination().equals(transition.getDestination())){
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean searchActionDestination(String a, String y, Set<Transition> transitions){
        boolean result = false;
        for (Transition data: transitions){
            if (data.getAction().equals(a)&&data.getDestination().equals(y)){
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean searchRelation(String relation, Set<String> relations){
        boolean result = false;
        for (String data: relations){
            if(data.equals(relation)){
                result = true;
            }
        }
        return result;
    }
    public boolean searchStates(String x, String[] xArray){
        for (String data: xArray){
            if (x.equals(data)){
                return true;
            }
        }
        return false;
    }

    public boolean checkIfStatesHaveNoTarget(Process process, String x){
        String[] states = x.split(",");
        Set<Transition> transitions = process.getTransitions();
        for (Transition transition: transitions){
            if (transition.getSource().equals(states[0]) || transition.getSource().equals(states[1])){
                return false;
            }
        }
        return true;
    }

    public boolean compareRelations(Set<String> r1,Set<String> r2){
        if(r1 == null && r2 ==null){//compare empty sets
            return true;
        }
        assert r1 != null;
        if(r1.size()!=r2.size()){//compare size
            return false;
        }
        return r1.containsAll(r2);//containsAll
    }
}
