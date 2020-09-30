import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BGame extends Process{
    private Set<String> relations;
    public BGame(){
        super();
        relations = null;
    }
    public BGame(Set<Transition> transitions, Set<String> states, Set<String> actions, Set<String> relations){
        super(transitions,states,actions);
        this.relations = relations;
    }
    public BGame(Process process, Set<String> relations){
        new BGame(process.getTransitions(),process.getStates(),process.getActions(),relations);
    }

    public void startGame(Process process){
        //get the bisimulation relation of process
        Set<String> relations = process.computeBisimulation(process);
        for (String relation:relations){
            System.out.println(relation);
        }
        System.out.println("Processing game...");
        System.out.println("Game Start: Here are the transitions:");
        System.out.println("Source   Action    Target(States labeled with 1 are the starting states.)");
        for (Transition transition : process.getTransitions()){
            System.out.println(transition.toString());
        }
        //ask user to pick a pair of states to start the game (should not choose the same states)
        System.out.println("pick a pair of states(x,x') to start the game (should not choose the same states)");
        Scanner sc1 = new Scanner(System.in);
        String userPick1 = sc1.nextLine();
        String[] userPickElements = userPick1.split(",");
        if (userPickElements[0].equals(userPickElements[1])){
            //if not, restart the game
            System.out.println("Please choose two different start states!");
            startGame(process);
        }
        if (searchRelation(userPick1,relations)){
            //if the two states are bisimilar
            System.out.println("Unfortunately, you lose the game! ");
            startFailGameLoop(process,userPick1);

        }else {
            //if not
            System.out.println("Congratulations, you win the game! ");
            startWinGameLoop(process,userPick1);
        }
    }

    public void startFailGameLoop(Process process, String startStates){
        //get the bisimulation relation of process
        Set<String> relations = process.computeBisimulation(process);
        System.out.println("Would you like to continue to see how you lose or restart the game?(c,r)");
        Scanner sc2 = new Scanner(System.in);
        String userPick2 = sc2.nextLine();
        if (userPick2.equals("c")){
            startStates = pickStepForFailGame(process,startStates);
            if (startStates == null){
                System.out.println("That's why you lose.");
            }else {
                startFailGameLoop(process,startStates);
            }
        }else if(userPick2.equals("r")){
            startGame(process);
        }else {
            System.out.println("Please check the spell of your choose");
            startFailGameLoop(process,startStates);
        }
    }

    public String pickStepForFailGame(Process process, String startStates){
        if(checkIfStatesHaveNoTarget(process,startStates)){
            System.out.println("There is no steps that you can do from "+startStates+".");
            return null;
        }
        System.out.println("Please pick a step from ("+ startStates + "),format(source,action,target)");
        Scanner sc3 = new Scanner(System.in);
        String userPick3 = sc3.nextLine();
        String[] userPick3Elements = userPick3.split(",");
        Transition userPickTransition = new Transition(userPick3Elements[0],userPick3Elements[2],userPick3Elements[1]);
        String[] startStatesArray = startStates.split(",");
        Set<Transition> transitions = process.getTransitions();
        if (searchTransition(userPickTransition,transitions) && searchStates(userPick3Elements[0],startStatesArray)){
            Transition toolPickTransition = toolPickTransitionFail(process,startStates,userPickTransition);
            if (toolPickTransition == null ){
                System.out.println("No possible move left!");
                return null;
            }else {
                if (userPickTransition.getDestination().equals(toolPickTransition.getDestination())){
                    return null;
                }
                String nextStates = userPickTransition.getDestination()+","+toolPickTransition.getDestination();
                return nextStates;
            }
        }else {
            System.out.println("Please check your input!");
            pickStepForFailGame(process,startStates);
        }
        return null;
    }

    public Transition toolPickTransitionFail(Process process, String startState, Transition userPickTransition){
        Set<Transition> transitions = process.getTransitions();
        Set<Transition> toolPickPossibility = new HashSet<>();
        //get the toolStartState
        startState = startState.replaceAll(",","");
        String toolStartState = startState.replaceAll(userPickTransition.getSource(),"");
        for (Transition transition: transitions){
            if (transition.getAction().equals(userPickTransition.getAction()) && transition.getSource().equals(toolStartState)) {
                toolPickPossibility.add(transition);
            }
        }
        if (toolPickPossibility.size()==0){
            return null;
        }else {
            //get the first element of toolPickPossibility
            Iterator it = toolPickPossibility.iterator();
            return (Transition)it.next();
        }

    }

    public void startWinGameLoop(Process process, String startStates){
        //get the bisimulation relation of process
        Set<String> relations = process.computeBisimulation(process);
        System.out.println("Would you like to continue to see how you win or restart the game?(c,r)");
        Scanner sc2 = new Scanner(System.in);
        String userPick2 = sc2.nextLine();
        if (userPick2.equals("c")){
            startStates = pickStepForWinGame(process,startStates);
            if (startStates == null){
                System.out.println("That's why you win!");
                //System.out.println("There is no steps that tool can do! That's why you win.");
            }else {
                startWinGameLoop(process,startStates);
            }
        }else if(userPick2.equals("r")){
            startGame(process);
        }else {
            System.out.println("Please check the spell of your choose");
            startWinGameLoop(process, startStates);
        }
    }

    public String pickStepForWinGame(Process process, String startStates){
        System.out.println("Please pick a step from ("+ startStates + "),format(source,action,target)");
        Scanner sc3 = new Scanner(System.in);
        String userPick3 = sc3.nextLine();
        String[] userPick3Elements = userPick3.split(",");
        Transition userPickTransition = new Transition(userPick3Elements[0],userPick3Elements[2],userPick3Elements[1]);
        String[] startStatesArray = startStates.split(",");
        Set<Transition> transitions = process.getTransitions();
        if (searchTransition(userPickTransition,transitions) && searchStates(userPick3Elements[0],startStatesArray)){
            Transition toolPickTransition = toolPickTransitionWin(process,startStates,userPickTransition);
            if (toolPickTransition == null ){
                return null;
            }else {
                if (userPickTransition.getDestination().equals(toolPickTransition.getDestination())){
                    return null;
                }
                String nextStates = userPickTransition.getDestination()+","+toolPickTransition.getDestination();
                return nextStates;
            }
        }else {
            System.out.println("Please check your input!");
            pickStepForFailGame(process,startStates);
        }
        return null;
    }

    public Transition toolPickTransitionWin(Process process, String startState, Transition userPickTransition){
        if(checkIfStatesHaveNoTarget(process,startState)){
            System.out.println("There is no steps that tool can do from "+startState+".");
            return null;
        }
        Set<String> relations = process.computeBisimulation(process);
        Set<Transition> transitions = process.getTransitions();
        Set<Transition> toolPickPossibility = new HashSet<>();
        //get the toolStartState
        startState = startState.replaceAll(",","");
        String toolStartState = startState.replaceAll(userPickTransition.getSource(),"");
        for (String relation : relations){
            String[] elements = relation.split(",");
            if (elements[0].equals(userPickTransition.getDestination())){
                Transition transition = new Transition(toolStartState,elements[1],userPickTransition.getAction());
                toolPickPossibility.add(transition);
            }
        }
        if (toolPickPossibility.size() == 0){
            return null;
        }else {
            //get the first element of toolPickPossibility
            Iterator it = toolPickPossibility.iterator();
            return (Transition)it.next();
        }

    }

    public static void main(String[] args){
        Process process = new Process();
        BufferedReader file = null;
        try {
            file = new BufferedReader(new FileReader(args[0]));
        }catch (FileNotFoundException fnfe){
            System.out.println("File cannot be found");
            System.exit(1);
        }
        try {
            ArrayList<String> LTSList = process.storeFileIntoArray(file);
            Set<Transition> transitions = process.collectTransitions(LTSList);
            Set<String> actions = process.collectActions(LTSList);
            Set<String> states = process.collectStates(LTSList);
            //successfully storing data in to the LTS class object
            process.setTransitions(transitions);
            process.setActions(actions);
            process.setStates(states);

        }catch (IOException io){
            System.out.println("Data store error");
        }
        BGame bGame = new BGame(process,process.computeBisimulation(process));
        bGame.startGame(process);
    }
}
