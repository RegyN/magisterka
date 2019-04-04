package tracks.singlePlayer.advanced.tkomisarczyk.simpleMC;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.Random;

public class Agent extends AbstractPlayer
{
    private Random randomGenerator;
    private int depth;
    private long remaining;
    private long startTime;
    private double avgTimeTaken;
    private double acumTimeTaken;
    private int numIters;
    private ArrayList<Types.ACTIONS> actions;
    private double[] results;
    private double[] playouts;

    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
        randomGenerator = new Random();
        actions = stateObs.getAvailableActions();
        this.depth = 10;
    }

    private void ResetTimers(ElapsedCpuTimer elapsedTimer){
        numIters = 0;
        avgTimeTaken = 0;
        acumTimeTaken = 0;
        startTime = elapsedTimer.remainingTimeMillis();
        remaining = startTime;
    }

    private void UpdateTimers(ElapsedCpuTimer elapsedTimer){
        acumTimeTaken = elapsedTimer.remainingTimeMillis() - startTime;
        avgTimeTaken = acumTimeTaken/numIters;
        remaining = elapsedTimer.remainingTimeMillis();
    }

    private Types.ACTIONS ChooseBestAction(double[] playouts, double[] results){
        int bestIndex = 0;
        double maxScore = -1000000;
        for(int i=0; i<results.length; i++){
            if(playouts[i] != 0 && results[i] / playouts[i] > maxScore){
                bestIndex = i;
                maxScore = results[i] / playouts[i];
            }
        }
        return actions.get(bestIndex);
    }

    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
        ResetTimers(elapsedTimer);
        ResetResults();
        actions = stateObs.getAvailableActions();

        TreeNode root = new TreeNode(0, depth);
        root.FullyExpand(stateObs);

        int numActions = actions.size();

        int remainingLimit = 5;
        int childToPlay = 0;
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit)
        {
            results[childToPlay] += root.children.get(childToPlay).RollSimulation(stateObs, randomGenerator);;
            playouts[childToPlay]++;
            numIters++;
            childToPlay = numIters % (numActions);
            UpdateTimers(elapsedTimer);
        }
        return ChooseBestAction(playouts, results);
    }

    private void ResetResults() {
        results = new double[actions.size()];
        playouts = new double[actions.size()];
    }
}
