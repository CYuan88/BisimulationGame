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
        this(process.getTransitions(),process.getStates(),process.getActions(),relations);
    }

    public Set<String> getRelations() {
        return relations;
    }

    public void setRelations(Set<String> relations) {
        this.relations = relations;
    }

    public void startGame(Process process){
        //ask user to pick a pair of states to start the game (should not choose the same states)
        System.out.println("pick a pair of states(x,x') to start the game (should not choose the same states)");
        //make a check
        try {
            Scanner sc1 = new Scanner(System.in);
            String userPick1 = sc1.nextLine();
            String[] userPickElements = userPick1.split(",");
            if (!searchStates(userPickElements[0], process.getStates()) || !searchStates(userPickElements[1], process.getStates())){
                System.out.println("One or both of "+userPick1+" are not the states of this LTS!");
                startGame(process);
                return;
            }
            //check whether the two states are the same
            if (userPickElements[0].equals(userPickElements[1])){
                //if not, restart the game
                System.out.println("Please choose two different start states!");
                startGame(process);
                return;
            }
            //check whether the two states are bisimilar or not
            if (process.searchRelation(userPick1,relations)){
                //if the two states are bisimilar
                System.out.println("Unfortunately, you lose the game! ");
                //Initial the goneState set, for now, it is empty
                Set<String> goneState = new HashSet<>();
                continueFailedGame(userPick1,goneState);

            }else {
                //if not
                System.out.println("Congratulations, you win the game! ");
                continueWinedGame(userPick1);
            }
        }catch (ArrayIndexOutOfBoundsException aiofbe){
            System.out.println("Please enter two states in format (x,x')!");
            startGame(process);
        }
    }

    public void continueFailedGame(String states,Set<String> goneState){
        String[] startStates = states.split(",");
        //check if attacker already lose the game or not
        if (this.checkIfStatesHaveNoTarget(this,states)){
            //if have no target
            System.out.println("There is no step that you can do from "+ states +"! That's why you lose the game!");
            return;
        }
        if (goneState.containsAll(this.getStates())){
            //if all the states are picked once
            System.out.println("All the states of the LTS have been picked once! That's why you lose the game!");
            return;
        }
        System.out.println("Pick one transition start from "+ states + " (source,action,target):");
        try {
            Scanner sc2 = new Scanner(System.in);
            String userPick2 = sc2.nextLine();
            String[] userPickElements = userPick2.split(",");
            Transition userPickTransition = new Transition(userPickElements[0],userPickElements[2],userPickElements[1]);
            if (!searchStartStates(userPickElements[0],startStates)){
                System.out.println("The source "+userPickElements[0]+"of transition is not one of the start states on this step!");
                continueFailedGame(states,goneState);
                return;
            }
            if (!searchTransition(userPickTransition,this.getTransitions())){
                System.out.println("Transition "+userPick2+" is not the transition of this LTS!");
                continueFailedGame(states,goneState);
                return;
            }
            Set<Transition> toolPickTransitionPossi = toolPlayFailedGame(states,userPickTransition);
            //make a check of two situations
            if (toolPickTransitionPossi.size() == 1){
                //get the first element of toolTransitionPossibility
                Iterator it = toolPickTransitionPossi.iterator();
                Transition toolPickTransition = (Transition) it.next();
                System.out.println("The tool can just choose one step now:");
                for (Transition transition:toolPickTransitionPossi){
                    System.out.println(transition.toString());
                }
                String next_states = userPickTransition.getDestination()+","+toolPickTransition.getDestination();
                //update the data in goneState set
                goneState.add(userPickTransition.getDestination());
                goneState.add(toolPickTransition.getDestination());
                continueFailedGame(next_states,goneState);
                return;
            }
            if (toolPickTransitionPossi.size() > 1){
                System.out.println("The tool can choose the following steps now:");
                for (Transition transition:toolPickTransitionPossi){
                    System.out.println(transition.toString());
                }
                System.out.println("Let's see the following possibilities from states "+states);
                //do each possibility of toolPickTransition
                int i = 0; //counter
                for (Transition toolPickTransition:toolPickTransitionPossi){
                    String next_states = userPickTransition.getDestination()+","+toolPickTransition.getDestination();
                    System.out.println("Situation"+i+" : Tool picks "+toolPickTransition.toString());
                    //update the data in goneState set
                    goneState.add(userPickTransition.getDestination());
                    goneState.add(toolPickTransition.getDestination());
                    continueFailedGame(next_states,goneState);
                    i++;
                }
                return;
            }

        }catch (ArrayIndexOutOfBoundsException aiofbe){
            System.out.println("Please enter a transition in format (x,a,x')!");
            continueFailedGame(states,goneState);
        }
    }

    public Set<Transition> toolPlayFailedGame(String states, Transition userPickTransition){
        states = states.replaceAll(",","");
        String toolSource = states.replace(userPickTransition.getSource(),"");
        Set<String> toolTarget = new HashSet<>();
        Set<Transition> toolTransition = new HashSet<>();
        //get all possible y' from the bisimulation relation set
        for (String relation : relations){
            relation = relation.replaceAll(",","");
            relation = relation.replaceAll(userPickTransition.getDestination(),"");
            toolTarget.add(relation);
        }
        //get all possible x',a,y'
        for (Transition transition: this.getTransitions()){
            for (String target: toolTarget){
                if (transition.getSource().equals(toolSource)&& transition.getAction().equals(userPickTransition.getAction()) &&transition.getDestination().equals(target)){
                    toolTransition.add(transition);
                }
            }

        }
        return toolTransition;
    }

    public void continueWinedGame(String states){
        String[] startStates = states.split(",");
        //check if attacker already lose the game or not
        System.out.println("Pick one transition start from "+ states + " (source,action,target):");
        try {
            Scanner sc2 = new Scanner(System.in);
            String userPick2 = sc2.nextLine();
            String[] userPickElements = userPick2.split(",");
            Transition userPickTransition = new Transition(userPickElements[0],userPickElements[2],userPickElements[1]);
            if (!searchStartStates(userPickElements[0],startStates)){
                System.out.println("The source "+userPickElements[0]+"of transition is not one of the start states on this step!");
                continueWinedGame(states);
                return;
            }
            if (!searchTransition(userPickTransition,this.getTransitions())){
                System.out.println("Transition "+userPick2+" is not the transition of this LTS!");
                continueWinedGame(states);
                return;
            }
            Set<Transition> toolPickTransitionPossi = toolPlayWinedGame(states,userPickTransition);
            if (toolPickTransitionPossi.size() == 0){
                String toolSource = states.replaceAll(",","");
                toolSource = toolSource.replaceAll(userPickTransition.getSource(),"");
                System.out.println("There is no step that tool can do from " + toolSource +"! That's why you win the game!" );
                return;
            }
            System.out.println("The tool can choose the following steps now:");
            for (Transition transition:toolPickTransitionPossi){
                System.out.println(transition.toString());
            }
            System.out.println("Let's see the following possibilities from states "+states);
            //do each possibility of toolPickTransition
            int i = 0; //counter
            for (Transition toolPickTransition:toolPickTransitionPossi){
                String next_states = userPickTransition.getDestination()+","+toolPickTransition.getDestination();
                System.out.println("Situation"+i+" : Tool picks "+toolPickTransition.toString());
                continueWinedGame(next_states);
                i++;
            }


        }catch (ArrayIndexOutOfBoundsException aiofbe){
            System.out.println("Please enter a transition in format (x,a,x')!");
            continueWinedGame(states);
        }
    }

    public Set<Transition> toolPlayWinedGame(String states, Transition userPickTransition){
        states = states.replaceAll(",","");
        String toolSource = states.replace(userPickTransition.getSource(),"");
        //Set<String> toolTarget = new HashSet<>();
        Set<Transition> toolTransition = new HashSet<>();
        for (Transition transition: this.getTransitions()){
            if (transition.getSource().equals(toolSource)&&transition.getAction().equals(userPickTransition)){
                //toolTarget.add(transition.getDestination());
                toolTransition.add(transition);
            }
        }
        return toolTransition;
    }


    public static void main(String[] args){
        Process process = new Process();
        BufferedReader file = null;
        try {
            file = new BufferedReader(new FileReader(args[0]));
        }catch (FileNotFoundException fnfe){
            System.out.println("File cannot be found!");
            System.exit(1);
        }catch (ArrayIndexOutOfBoundsException aiofbe){
            System.out.println("Please input your file name on the command line!");
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
        for (String data : bGame.getRelations()){
            System.out.println(data);
        }
        System.out.println("Processing game...");
        System.out.println("Game Start: Here are the transitions:");
        System.out.println("Source   Action    Target(States labeled with 1 are the starting states.)");
        for (Transition transition : process.getTransitions()){
            System.out.println(transition.toString());
        }
        bGame.startGame(process);
    }
}
