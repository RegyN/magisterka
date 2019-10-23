package tracks.singlePlayer.past.JinJerry;

import java.util.HashMap;
import java.util.Random;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;
import core.player.AbstractPlayer;

public class Agent extends AbstractPlayer {
	
	private final HashMap<Integer, Types.ACTIONS> action_mapping;
    private final HashMap<Types.ACTIONS, Integer> r_action_mapping;
    protected Random randomGenerator;
    
    private MyStateHeuristic heuristic;
    private int simulationDepth = 7;
    
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		// TODO Auto-generated constructor stub
		action_mapping = new HashMap<Integer, Types.ACTIONS>();
        r_action_mapping = new HashMap<Types.ACTIONS, Integer>();
        randomGenerator = new Random();
        
        int i = 0;
        for (Types.ACTIONS action : stateObs.getAvailableActions()) {
            action_mapping.put(i, action);
            r_action_mapping.put(action, i);
            i++;
        }
        heuristic = new MyStateHeuristic(stateObs);
	}
	
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		// TODO Auto-generated method stub
		Types.ACTIONS bestAction = null;
		int N_Actions = stateObs.getAvailableActions().size();
		double[] scores = new double[N_Actions];
		
        for (int a = 0; a < N_Actions; a++) {
        	// set each available action as first action
        	Types.ACTIONS action = action_mapping.get(a);
            StateObservation stCopy = stateObs.copy();
            heuristic.setBeforeAdvance(stCopy, action);
            stCopy.advance(action);
            scores[a] = heuristic.evaluateState(stCopy);
            
            int iteration = 0;
            for (; iteration < simulationDepth; iteration++) {
            	// random pick action after each first action picked above until reach simulation depth or game over.
            	Types.ACTIONS nextAction = action_mapping.get(randomGenerator.nextInt(N_Actions));
            	heuristic.setBeforeAdvance(stCopy, nextAction);
            	stCopy.advance(nextAction);
            	// if game is over, break out to save time.
            	if (stCopy.isGameOver()) {
            		break;
            	}
            }
            if (iteration == simulationDepth) {
            	double nextScore = heuristic.evaluateState(stCopy);
                if (nextScore > scores[a]) {
                	scores[a] = nextScore;
                }
            }
        }
        
//        for (int s = 0; s < scores.length; s++) {
//        	System.out.println("Action:" + action_mapping.get(s).name() + " score:" + scores[s]);
//        }
        
        // check all scores, pick the action according to the scores, random pick if scores are the same.       
        double bestScore = scores[0];
        boolean allScoresEqualFlag = true;
        int bestActionIndex = 0;
        for (int i = 0; i < scores.length; i++) {
        	if (bestScore != scores[i]) {
        		allScoresEqualFlag = false;
        	}
        	if (bestScore < scores[i]) {
				bestScore = scores[i];
				bestActionIndex = i;
			}
        	else if (bestScore == scores[i]) {
        		if (randomGenerator.nextDouble() < 0.5) {
        			bestActionIndex = i;
        		}
        	}
        }
        
        if (allScoresEqualFlag) {
        	bestAction = action_mapping.get(randomGenerator.nextInt(scores.length));
        }
        else {
			bestAction = action_mapping.get(bestActionIndex);
		}

//    	System.out.printf("action picked: %s\n", bestAction.name());
        heuristic.updateMap(stateObs, bestAction);
        return bestAction;
    }
}
