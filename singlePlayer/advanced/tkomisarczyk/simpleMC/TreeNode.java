package tracks.singlePlayer.advanced.tkomisarczyk.simpleMC;

import core.game.StateObservation;
import tracks.singlePlayer.tools.Heuristics.StateHeuristic;
import tracks.singlePlayer.tools.Heuristics.WinScoreHeuristic;

import java.util.ArrayList;
import java.util.Random;

public class TreeNode {
    TreeNode parent;
    int depth;
    int maxDepth;
    ArrayList<TreeNode> children;
    int numTests = 0;
    double sumScore = 0;
    
    TreeNode(int depth, int maxDepth) {
        this.maxDepth = maxDepth;
        this.depth = depth;
        this.parent = null;
    }
    
    TreeNode(int depth, int maxDepth, TreeNode parent) {
        this.maxDepth = maxDepth;
        this.depth = depth;
        this.parent = parent;
    }
    
    public void FullyExpand(StateObservation obs) {
        children = new ArrayList<>();
        var actions = obs.getAvailableActions();
        for (var a : actions) {
            var child = new TreeNode(depth + 1, maxDepth, this);
            children.add(child);
        }
    }
    
    public double RollSimulation(StateObservation obs, Random generator) {
        double result;
        var stCopy = obs.copy();
        var actions = stCopy.getAvailableActions();
        
        if (children != null) {
            int chosen = generator.nextInt(actions.size());
            stCopy.advance(actions.get(chosen));
            result = children.get(chosen).RollSimulation(stCopy, generator);
            this.numTests++;
            this.sumScore += result;
        }
        else {
            for (int curDepth = depth; curDepth < maxDepth; curDepth++) {
                if (stCopy.isGameOver()) {
                    break;
                }
                var choice = generator.nextInt(actions.size());
                stCopy.advance(actions.get(choice));
            }
            StateHeuristic heuristic = new WinScoreHeuristic();
            result = heuristic.evaluateState(stCopy);
        }
        return result;
    }
    
    public Boolean IsRoot() {
        return parent == null;
    }
}
