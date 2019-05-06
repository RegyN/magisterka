package tracks.singlePlayer.advanced.tkomisarczyk.MonteCarlo;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.Random;

public class Agent extends AbstractPlayer {
    private Random generator;
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
    PositionHistory history;


    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        generator = new Random();
        actions = stateObs.getAvailableActions();
        depth = 10;
        turnNumber = 0;
        knowledge = GameKnowledge.GetNew();
        history = PositionHistory.GetNew();

        ExtractKnowledge(stateObs, elapsedTimer);
    }

    private void ExtractKnowledge(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        knowledge.GatherStaticInfo(stateObs);
        if(knowledge.type == GameType.Planar2D){
            knowledge.GetherSpriteInfoRandomly(stateObs, elapsedTimer);
        }
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
        return root.GetBestScoreAction(knowledge.type == GameType.Planar2D);
    }

    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        ResetTimers(elapsedTimer);
        history.Add(Position2D.GetAvatarPosition(stateObs));

        root = new TreeNode(depth, stateObs);
        //String breakReason;
        int remainingLimit = 5;
        while (true) {
            if (remaining <= 2 * avgTimeTaken) {
                //breakReason = "avgTimeTaken";
                break;
            } else if (remaining < remainingLimit) {
                //breakReason = "remainingLimit";
                break;
            }
            root.ExpandIntelligently(stateObs);
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
        turnNumber++;
        var chosen = ChooseBestAction(root);
        System.out.println(chosen.name());
        return chosen;
    }
}
