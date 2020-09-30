import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    public ArrayList<String> storeFileIntoArray(BufferedReader file) throws IOException {

        String s;
        String ss = "";
        String[] transitions;
        ArrayList<String> LTSList = new ArrayList<>();
        while ((s = file.readLine())!= null){

            ss = ss+s;

        }
        //System.out.println(ss);
        transitions = ss.split("\\|");
        LTSList.addAll(Arrays.asList(transitions));
        /*for (String data : LTSList) {

            System.out.println(data);

        }*/
        return LTSList;

    }

    public Set<Transition> collectTransitions(ArrayList<String> LTSList){

        Set<Transition> transitions = new HashSet<>();
        for (String data : LTSList) {

            data = data.substring(1,data.length());
            data = data.substring(0,data.length()-1);
            String[] elements = data.split(",");
            Transition newTransition = new Transition();
            newTransition.setSource(elements[0]);
            newTransition.setAction(elements[1]);
            newTransition.setDestination(elements[2]);
            transitions.add(newTransition);

        }/*
        for (Transition data:transitions){

            System.out.println(data.toString());

        }*/
        return transitions;

    }

    public Set<String> collectActions(ArrayList<String> LTSList){

        Set<String> actions = new HashSet<>();
        for (String data : LTSList) {

            data = data.substring(1,data.length());
            data = data.substring(0,data.length()-1);
            String[] elements = data.split(",");
            actions.add(elements[1]);

        }
        for (String data : actions){
            //System.out.println(data);
        }
        return actions;


    }

    public Set<String> collectStates(ArrayList<String> LTSList){

        Set<String> states = new HashSet<>();
        for (String data : LTSList) {

            data = data.substring(1,data.length());
            data = data.substring(0,data.length()-1);
            String[] elements = data.split(",");
            states.add(elements[0]);
            states.add(elements[2]);

        }
        /*
        for (String data : states){
            System.out.println(data);
        }*/
        return states;


    }

    //get the bisimulation relation after computing the process
    public Set<String> computeBisimulation(Process process){
        Set<String> R = getR0(process.getStates()); //R0 set that won't change data of it
        Set<String> R0 = new HashSet<>(R);
        while (true){
            //Set<String> R1 = getResults(actions,transitions,R,R0);
            Set<String> R1 = getResults(process,R0);
            if (R1.containsAll(R0)){
                return R1;
            }else {
                R0.clear();
                R0.addAll(R1);
            }
        }
    }

    //calculate the relation set once
    public Set<String> getResults(Process process, Set<String> R0){
        Set<String> R = getR0(process.getStates()); //R0 set that won't change data of it
        //Set<Transition> transitions = process.getTransitions();
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
                //if(searchRelationFromR(x,x_prime,R,R0,transitions)&&searchRelationFromR(x_prime,x,R,R0,transitions)){
                if(searchRelationFromR(x,x_prime,process,R0)&&searchRelationFromR(x_prime,x,process,R0)){
                    String relation = x + "," + x_prime;
                    R1.add(relation);
                }
            }
        }

        return R1;
    }

    //get the result of search x,x',R0
    public boolean searchRelationFromR(String x, String x_prime, Process process, Set<String> R0){
        Set<Transition> transitions = process.getTransitions();
        Set<String> actions = process.getActions();
        Set<Transition> possible_transitions = new HashSet<>();
        //get all possible transitions started from x
        //get all possible targets from the possible transition
        Set<String> possible_targets = new HashSet<>();
        for (Transition transition: transitions){
            String source = transition.getSource();
            if (source.equals(x)){
                possible_transitions.add(transition);
                //get possible targets
                possible_targets.add(transition.getDestination());
            }
        }
        if (possible_transitions.size() == 0){
            return true;
        }
        Set<Boolean> resultSet = new HashSet<>();
        Set<Integer> possible_results = new HashSet<>();
        for (String y: possible_targets){
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

    //get the initial R set
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

    //check whether we can find a transition from the transition set or not
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

    //check whether we can find a transition with action a and target y from the transition set or not
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

    //check whether we can find a relation from the relation set or not
    public boolean searchRelation(String relation, Set<String> relations){
        boolean result = false;
        for (String data: relations){
            if(data.equals(relation)){
                result = true;
            }
        }
        return result;
    }

    //check whether we can find a state from the state array or not
    public boolean searchStates(String x, String[] xArray){
        for (String data: xArray){
            if (x.equals(data)){
                return true;
            }
        }
        return false;
    }

    //check whether the pair of states has a target or not
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

    //check whether the two relation sets are the same
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
