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
    int turnNumber;
    GameKnowledge knowledge;
    
    
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        randomGenerator = new Random();
        actions = stateObs.getAvailableActions();
        depth = 10;
        turnNumber = 0;
    
        // TODO: Replace with proper knowledge gathering:
        knowledge = GameKnowledge.getNew();
        knowledge.sprites.put(0, SpriteType.Wall);
        knowledge.sprites.put(2, SpriteType.Floor);
        knowledge.sprites.put(4, SpriteType.Point);
        knowledge.sprites.put(5, SpriteType.Point);
        knowledge.sprites.put(6, SpriteType.Other);
        knowledge.sprites.put(15, SpriteType.Enemy);
        knowledge.sprites.put(18, SpriteType.Enemy);
        knowledge.sprites.put(21, SpriteType.Enemy);
        knowledge.sprites.put(24, SpriteType.Enemy);
        knowledge.sprites.put(27, SpriteType.Other);
        knowledge.type = GameType.Planar2D;
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
        
        root = new TreeNode(depth);
        //String breakReason;
        int remainingLimit = 5;
        while (true) {
            if(remaining <= 2 * avgTimeTaken) {
                //breakReason = "avgTimeTaken";
                break;
            }
            else if(remaining < remainingLimit) {
                //breakReason = "remainingLimit";
                break;
            }
            root.Expand(stateObs);
            UpdateTimers(elapsedTimer);
        }
        //System.out.println(numIters + " " + breakReason);
        
//        PĘTLA DO DEBUGOWANIA BEZ OGRANICZEN CZASOWYCH
//        int iterations = 15;
//        for(int i = 0; i<iterations; i++){
//            root.Expand(stateObs);
//            UpdateTimers(elapsedTimer);
//        }
//        System.out.println(avgTimeTaken);
        
        return ChooseBestAction(root);
    }
}
