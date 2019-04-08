package tracks.singlePlayer.advanced.tkomisarczyk.MonteCarlo;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.Random;

public class Agent extends AbstractPlayer {
    private Random randomGenerator;
    private int depth;
    private long remaining;
    private long startTime;
    private double avgTimeTaken;
    private double acumTimeTaken;
    private int numIters;
    private ArrayList<Types.ACTIONS> actions;
    TreeNode root;
    
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        randomGenerator = new Random();
        actions = stateObs.getAvailableActions();
        this.depth = 10;
    }
    
    private void ResetTimers(ElapsedCpuTimer elapsedTimer) {
        numIters = 0;
        avgTimeTaken = 0;
        acumTimeTaken = 0;
        startTime = elapsedTimer.remainingTimeMillis();
        remaining = startTime;
    }
    
    private void UpdateTimers(ElapsedCpuTimer elapsedTimer) {
        numIters++;
        acumTimeTaken = elapsedTimer.remainingTimeMillis() - startTime;
        avgTimeTaken = acumTimeTaken / numIters;
        remaining = elapsedTimer.remainingTimeMillis();
    }
    
    private Types.ACTIONS ChooseBestAction(TreeNode root) {
        int bestActionIndex = root.GetBestScoreIndex();
        return actions.get(bestActionIndex);
    }
    
    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        ResetTimers(elapsedTimer);
        actions = stateObs.getAvailableActions();

        root = new TreeNodeWithState(depth, stateObs);
        String breakReason;
        int remainingLimit = 5;
        while (true) {
            if(remaining <= 2 * avgTimeTaken) {
                breakReason = "avgTimeTaken";
                break;
            }
            else if(remaining < remainingLimit) {
                breakReason = "remainingLimit";
                break;
            }
            root.Expand(stateObs);
            UpdateTimers(elapsedTimer);
        }
        System.out.println(numIters + " " + breakReason);
        
        //PĘTLA DO DEBUGOWANIA BEZ OGRANICZEN CZASOWYCH
//        int iterations = 15;
//        for(int i = 0; i<iterations; i++){
//            root.Expand(stateObs);
//            UpdateTimers(elapsedTimer);
//        }
//        System.out.println(avgTimeTaken);
        
        return ChooseBestAction(root);
    }
}
