package tracks.singlePlayer.advanced.tkomisarczyk.MonteCarlo;

import core.game.StateObservation;
import tools.Utils;
import tracks.singlePlayer.advanced.tkomisarczyk.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreeNode {
    TreeNode parent;
    String name;    // TODO: Przestać to zapisywać jak już skończę debugowanie.
    int depth;
    int maxDepth;
    List<TreeNode> children;
    int numTests = 0;
    int sumScore = 0;
    int uninitiatedChildren;
    Random generator;
    double K = 1.41;
    
    TreeNode(){
        this.name = "ROOT";
        this.maxDepth = 10;
        this.depth = 0;
        this.parent = null;
        this.generator = new Random();
    }
    
    TreeNode(int maxDepth) {
        // Dla korzenia nie robię symulacji
        this.name = "ROOT";
        this.maxDepth = maxDepth;
        this.depth = 0;
        this.parent = null;
        this.generator = new Random();
    }
    
    TreeNode(TreeNode parent, StateObservation obs) {
        this.maxDepth = parent.maxDepth;
        this.depth = parent.depth + 1;
        this.parent = parent;
        this.generator = parent.generator;
        
        var result = RollSimulation(obs, generator);
        UpdateScoreUpwards(result);
    }
    
    public int RollSimulation(StateObservation obs, Random generator) {
        var actions = obs.getAvailableActions();
        int d = depth;
        for (; d < maxDepth; d++) {
            if (obs.isGameOver())
                break;
            int choice = generator.nextInt(actions.size());
            obs.advance(actions.get(choice));
        }
        return Utilities.EvaluateState(obs, d);
    }
    
    public void Expand(StateObservation obs) {
        if(this.IsRoot()){
            obs = obs.copy();
        }
        var actions = obs.getAvailableActions();
        
        if (children == null) {             // Pierwszy raz rozwijamy to dziecko
            if (actions.size() == 0) {      // Z tego dziecka nie da się wykonać akcji (gra się zakończyła)
                var result = Utilities.EvaluateState(obs);
                UpdateScoreUpwards(result);
            }
            else {
                int choice = generator.nextInt(actions.size());
                obs.advance(actions.get(choice));
                children = new ArrayList<>();
                for(int i=0; i< actions.size(); i++){
                    children.add(null);
                }
                children.set(choice, new TreeNode(this, obs));
                children.get(choice).name = actions.get(choice).name();
                uninitiatedChildren = actions.size() - 1;
            }
        }
        else if (uninitiatedChildren > 0) {    // Ten węzeł ma nierozwinięte dzieci
            int chosen = GetNthUninitialized(generator.nextInt(UninitiatedLeft()));
            obs.advance(actions.get(chosen));
            children.set(chosen, new TreeNode(this, obs));
            children.get(chosen).name = actions.get(chosen).name();
            uninitiatedChildren--;
        }
        else {
            int choice = ChooseChildToExpandUct(obs);
            obs.advance(actions.get(choice));
            children.get(choice).Expand(obs);
        }
    }
    
    /**
     * Zwraca liczbę niezainicjalizowanych dzieci obecnego węzła
     */
    private int UninitiatedLeft(){
        int left = 0;
        for (TreeNode child : children) {
            if (child == null) {
                left++;
            }
        }
        return left;
    }
    
    /**
     * Zwraca indeks n-tego niezainicjalizowanego (==null) dziecka.
     * @param n numer niezainicjalizowanego dziecka
     * @return indeks n-tego niezainicjalizowanego dziecka
     */
    private int GetNthUninitialized(int n){
        int i=0;
        for(; i<children.size(); i++){
            if(children.get(i) == null){
                if(n == 0) {
                    break;
                }
                n--;
            }
        }
        return i;
    }
    
    public int ChooseChildToExpandUct(StateObservation obs) {
        double maxScore = Double.MIN_VALUE;
        double minScore = Double.MAX_VALUE;
        for (TreeNode child : this.children) {
            if (child.sumScore > maxScore)
                maxScore = child.sumScore;
            if (child.sumScore < minScore)
                minScore = child.sumScore;
        }
        int choice = 0;
        double chosenScore = Double.MIN_VALUE;
        for(int i = 0; i < children.size(); i++){
            double normalized = Utilities.NormalizeScore(children.get(i).sumScore, maxScore, minScore);
            double disturbed = Utils.noise(normalized, 0.000001d, generator.nextDouble());
            double score = disturbed / children.get(i).numTests + K * Math.sqrt(Math.log(this.numTests)/Math.log(children.get(i).numTests));
            if(score > chosenScore) {
                choice = i;
                chosenScore = score;
            }
        }
        return choice;
    }
    
    public void UpdateScoreUpwards(int difference) {
        this.numTests++;
        this.sumScore += difference;
        if (!this.IsRoot()) {
            parent.UpdateScoreUpwards(difference);
        }
    }
    
    public Boolean IsRoot() {
        return parent == null;
    }
    
    public int GetBestScoreIndex() {
        int max = Integer.MIN_VALUE;
        int maxIndex = 0;
        for (int i = 0; i < children.size(); i++) {
            if(children.get(i) == null){
                continue;
            }
            if (children.get(i).sumScore > max) {
                max = children.get(i).sumScore;
                maxIndex = i;
            }
        }
        return maxIndex;
    }
    
    public int GetBestAverageIndex() {
        double max = Double.MIN_VALUE;
        int maxIndex = 0;
        for (int i = 0; i < children.size(); i++) {
            if(children.get(i) == null){
                continue;
            }
            double cur = Utils.noise((double) children.get(i).sumScore / (double) children.get(i).numTests, 0.000001d, generator.nextDouble());
            if (children.get(i).numTests != 0 && cur > max) {
                max = cur;
                maxIndex = i;
            }
        }
        return maxIndex;
    }
    
    public int GetMostVisitedIndex() {
        int max = Integer.MIN_VALUE;
        int maxIndex = 0;
        for (int i = 0; i < children.size(); i++) {
            if(children.get(i) == null){
                continue;
            }
            if (children.get(i).numTests > max) {
                max = children.get(i).numTests;
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
