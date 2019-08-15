package tracks.singlePlayer.advanced.tkomisarczyk.MonteCarlo;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Utils;
import tracks.singlePlayer.advanced.tkomisarczyk.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreeNode implements ITreeNode{
    TreeNode parent;
    boolean useTurnNumberOnLoss;
    int depth;
    int maxDepth;
    List<TreeNode> children;
    List<Types.ACTIONS> childActions;
    int numTests = 0;
    float localScore = 0;
    /// Ocena stanu w obecnym węźle
    float stateScore = 0;
    int sumScore = 0;
    int uninitiatedChildren;
    Random generator;
    double K = 1.41;
    StateObservation state = null; // NULL dla wszystkich poza rootem

    TreeNode(){
        this.maxDepth = 10;
        this.depth = 0;
        this.parent = null;
        this.generator = new Random();
    }

    TreeNode(int maxDepth, StateObservation obs) {
        // Dla korzenia nie robię symulacji
        this.maxDepth = AgentParameters.GetInstance().maxDepth;
        this.depth = 0;
        this.parent = null;
        this.generator = new Random();
        this.state = obs;
        this.stateScore = Utilities.EvaluateState(obs);
        this.localScore = stateScore;
    }

    TreeNode(TreeNode parent, StateObservation obs) {
        AgentParameters params = AgentParameters.GetInstance();
        this.useTurnNumberOnLoss = params.useTurnsOnLoss;
        this.maxDepth = params.maxDepth;
        this.depth = parent.depth + 1;
        this.parent = parent;
        this.generator = parent.generator;
        PositionHistory pos = PositionHistory.GetInstance();
        this.stateScore = Utilities.EvaluateState(obs);
        int result;
        if(params.rollSimulationCleverly && GameKnowledge.getInstance().type == GameType.Planar2D) {
            result = RollSimulationCleverly(obs, generator, true);
        }
        else{
            result = RollSimulation(obs, generator);
        }
        UpdateScoreUpwards(result);
    }
    
    private int RollSimulation(StateObservation obs, Random generator) {
        ArrayList<Types.ACTIONS> actions = obs.getAvailableActions();
        int d = depth;
        for (; d < maxDepth; d++) {
            if (obs.isGameOver())
                break;
            int choice = generator.nextInt(actions.size());
            obs.advance(actions.get(choice));
        }
        return useTurnNumberOnLoss ? Utilities.EvaluateState(obs, d) : Utilities.EvaluateState(obs);
    }

    // Symulacje prowadzone w taki sposób, że jeśli została wylosowana akcja prowadząca w ścianę, to losowane jest jeszcze raz
    // Możliwe jest, że za drugim razem wylosowane będzie to samo
    private int RollSimulationCleverly(StateObservation obs, Random generator){
        ArrayList<Types.ACTIONS> actions = obs.getAvailableActions();
        GameKnowledge knowledge = GameKnowledge.getInstance();
        int d = depth;
        for (; d < maxDepth; d++) {
            if (obs.isGameOver())
                break;
            int choice = generator.nextInt(actions.size());
            Position2D avatarPos = Position2D.GetAvatarPosition(obs);

            // Sprawdzam, co znajduje się w kierunku, który wybrał generator
            ArrayList<Observation> obstacles = new ArrayList<>();
            if(actions.get(choice) == Types.ACTIONS.ACTION_UP){
                obstacles = Position2D.GetObservations(obs, avatarPos.x, avatarPos.y-knowledge.avatarSpeed);
            }
            else if(actions.get(choice) == Types.ACTIONS.ACTION_DOWN){
                obstacles = Position2D.GetObservations(obs, avatarPos.x, avatarPos.y+knowledge.avatarSpeed);
            }
            else if(actions.get(choice) == Types.ACTIONS.ACTION_LEFT){
                obstacles = Position2D.GetObservations(obs, avatarPos.x-knowledge.avatarSpeed, avatarPos.y);
            }
            else if(actions.get(choice) == Types.ACTIONS.ACTION_RIGHT){
                obstacles = Position2D.GetObservations(obs, avatarPos.x+knowledge.avatarSpeed, avatarPos.y);
            }
            // Jeśli są tam jakieś ściany, to losuję jeszcze raz
            if(knowledge.CheckForWalls(obstacles)){
                choice = generator.nextInt(actions.size());
            }
            obs.advance(actions.get(choice));
        }
        return useTurnNumberOnLoss ? Utilities.EvaluateState(obs, d) : Utilities.EvaluateState(obs);
    }

    private int RollSimulationCleverly(StateObservation obs, Random generator, boolean useHistory){
        int result = RollSimulationCleverly(obs, generator);
        if(useHistory){
            result += PositionHistory.GetInstance().getLocationBias(obs);
        }
        return result;
    }

    public void Expand(StateObservation obs) {
        if(this.IsRoot()){
            obs = obs.copy();
        }
        ArrayList<Types.ACTIONS> actions = obs.getAvailableActions();
        if(actions.size() <= 0){  // Czasami z powodu niedeterminizmu gra kończy się wcześniej niż by się można spodziewać. Wtedy wracamy do korzenia.
            int result = Utilities.EvaluateState(obs);
            UpdateScoreUpwards(result);
            //System.out.print(".");
            return;
        }

        if (children == null) {
            int choice = generator.nextInt(actions.size());
            obs.advance(actions.get(choice));
            children = new ArrayList<>();
            childActions = new ArrayList<>();
            for(int i=0; i< actions.size(); i++){
                children.add(null);
                childActions.add(actions.get(i));
            }
            children.set(choice, new TreeNode(this, obs));
            uninitiatedChildren = actions.size() - 1;
        }
        else if (uninitiatedChildren > 0) {    // Ten węzeł ma nierozwinięte dzieci
            int choice = GetNthUninitialized(generator.nextInt(UninitiatedLeft()));
            obs.advance(actions.get(choice));
            children.set(choice, new TreeNode(this, obs));
            uninitiatedChildren--;
        }
        else {
            int choice = ChooseChildToExpandUct(obs);
            obs.advance(actions.get(choice));
            children.get(choice).Expand(obs);
        }
    }

    // Ekspansja w taki sposób, żeby w ogóle nie brać pod uwagę ruchów idących prosto w ściany
    public void ExpandIntelligently(StateObservation obs) {
        if(this.IsRoot()){
            obs = obs.copy();
        }
        ArrayList<Types.ACTIONS> actions = obs.getAvailableActions();
        if(actions.size() <= 0){  // Czasami z powodu niedeterminizmu gra kończy się wcześniej niż by się można spodziewać. Wtedy wracamy do korzenia.
            int result = Utilities.EvaluateState(obs);
            UpdateScoreUpwards(result);
            //System.out.print(".");
            return;
        }

        GameKnowledge knowledge = GameKnowledge.getInstance();
        Position2D avatarPos = Position2D.GetAvatarPosition(obs);
        if (children == null) {
            children = new ArrayList<>();
            childActions = new ArrayList<>();
            for(int i=0; i< actions.size(); i++){
                if(actions.get(i) == Types.ACTIONS.ACTION_UP){
                    if(knowledge.CheckForWalls(Position2D.GetObservations(obs, avatarPos.x, avatarPos.y-knowledge.avatarSpeed))){
                        continue;
                    }
                }
                else if(actions.get(i) == Types.ACTIONS.ACTION_DOWN){
                    if(knowledge.CheckForWalls(Position2D.GetObservations(obs, avatarPos.x, avatarPos.y+knowledge.avatarSpeed))){
                        continue;
                    }
                }
                else if(actions.get(i) == Types.ACTIONS.ACTION_LEFT){
                    if(knowledge.CheckForWalls(Position2D.GetObservations(obs, avatarPos.x-knowledge.avatarSpeed, avatarPos.y))){
                        continue;
                    }
                }
                else if(actions.get(i) == Types.ACTIONS.ACTION_RIGHT){
                    if(knowledge.CheckForWalls(Position2D.GetObservations(obs, avatarPos.x+knowledge.avatarSpeed, avatarPos.y))){
                        continue;
                    }
                }
                children.add(null);
                childActions.add(actions.get(i));
            }
            if(childActions.size() <= 0){
                int result = Utilities.EvaluateState(obs);
                UpdateScoreUpwards(result);
                //System.out.print(",");
                return;
            }
            int choice = generator.nextInt(childActions.size());
            obs.advance(childActions.get(choice));
            children.set(choice, new TreeNode(this, obs));
            uninitiatedChildren = childActions.size() - 1;
        }
        else if (uninitiatedChildren > 0) {    // Ten węzeł ma nierozwinięte dzieci
            int choice = GetNthUninitialized(generator.nextInt(UninitiatedLeft()));
            obs.advance(childActions.get(choice));
            children.set(choice, new TreeNode(this, obs));
            uninitiatedChildren--;
        }
        else {
            if(childActions.size() <= 0){
                int result = Utilities.EvaluateState(obs);
                UpdateScoreUpwards(result);
                //System.out.print(",");
                return;
            }
            int choice = ChooseChildToExpandUct(obs);
            obs.advance(childActions.get(choice));
            children.get(choice).ExpandIntelligently(obs);
        }
    }
    
    /**
     * Zwraca liczbę niezainicjalizowanych dzieci obecnego węzła
     */
    protected int UninitiatedLeft(){
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
    protected int GetNthUninitialized(int n){
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

    private int GetNodeValue(){
        return sumScore;
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
        // TODO: Sprawdzić, czy odkomentowanie tej sekcji ma sens (raczej nie)
//        Position2D avatarPos = Position2D.GetAvatarPosition(obs);
//        var knowledge = GameKnowledge.getInstance();
//        ArrayList<Observation> obstacles = new ArrayList<>();
//        if(childActions.get(choice) == Types.ACTIONS.ACTION_UP){
//            obstacles = Position2D.GetObservations(obs, avatarPos.x, avatarPos.y-knowledge.avatarSpeed);
//        }
//        else if(childActions.get(choice) == Types.ACTIONS.ACTION_DOWN){
//            obstacles = Position2D.GetObservations(obs, avatarPos.x, avatarPos.y+knowledge.avatarSpeed);
//        }
//        else if(childActions.get(choice) == Types.ACTIONS.ACTION_LEFT){
//            obstacles = Position2D.GetObservations(obs, avatarPos.x-knowledge.avatarSpeed, avatarPos.y);
//        }
//        else if(childActions.get(choice) == Types.ACTIONS.ACTION_RIGHT){
//            obstacles = Position2D.GetObservations(obs, avatarPos.x+knowledge.avatarSpeed, avatarPos.y);
//        }
//        // Jeśli są tam jakieś ściany, to losuję jeszcze raz
//        if(knowledge.CheckForWalls(obstacles)){
//            choice = generator.nextInt(childActions.size());
//        }
        return choice;
    }
    
    public void UpdateScoreUpwards(int difference) {
        this.numTests++;
        this.sumScore += difference;
        if (!this.IsRoot()) {
            parent.UpdateScoreUpwards(difference);
        }
    }

    @Override
    public boolean IsRoot() {
        return parent == null;
    }

    public boolean IsLeaf(){
        return children == null;
    }

    public boolean IsUnfinished(){
        return !IsLeaf() && UninitiatedLeft() != 0;
    }


    @Override
    public Types.ACTIONS GetBestScoreAction(boolean useHistory) {
        return childActions.get(GetBestScoreIndex(useHistory));
    }

    public Types.ACTIONS GetBestAverageAction(boolean useHistory) {
        return childActions.get(GetBestAverageIndex(useHistory));
    }

    @Override
    public Types.ACTIONS GetMostVisitedAction(boolean useHistory) {
        return childActions.get(GetMostVisitedIndex(useHistory));
    }

    @Override
    public  int GetBestScoreIndex(boolean useHistory){
        if(!useHistory || !IsRoot()){
            return GetBestScoreIndex();
        }
        PositionHistory history = PositionHistory.GetInstance();
        double correctionFactor = 0.8;
        Position2D avatarPos = Position2D.GetAvatarPosition(state);
        float max = -Float.MAX_VALUE;
        int maxIndex = 0;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == null) {
                continue;
            }
            float score = (float)Utilities.DisturbScore(children.get(i).GetNodeValue());
            if(score > max){
                Types.ACTIONS action = childActions.get(i);
                Position2D newPosition = Position2D.ModifyPosition(avatarPos, action);
                score = score - history.Count(newPosition);
                if(history.Contains(newPosition)){
                    score = score > 0 ? (int)(score * correctionFactor) : (int)(score / correctionFactor);
                }
                if(score > max){
                    max = score;
                    maxIndex = i;
                }
            }
        }
        return maxIndex;
    }

    @Override
    public int GetBestScoreIndex() {
        float max = -Float.MAX_VALUE;
        int maxIndex = 0;
        for (int i = 0; i < children.size(); i++) {
            if(children.get(i) == null){
                continue;
            }
            float score = (float)Utilities.DisturbScore(children.get(i).GetNodeValue());
            if (score > max) {
                max = score;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    @Override
    public int GetBestAverageIndex(boolean useHistory) {
        if(!useHistory){
            return GetBestAverageIndex();
        }
        PositionHistory history = PositionHistory.GetInstance();
        Position2D avatarPos = Position2D.GetAvatarPosition(state);
        double max = Double.MIN_VALUE;
        int maxIndex = 0;
        for (int i = 0; i < children.size(); i++) {
            if(children.get(i) == null){
                continue;
            }
            double score =  Utils.noise((double) children.get(i).sumScore / (double) children.get(i).numTests, 0.000001d, generator.nextDouble());
            if (children.get(i).numTests != 0 && score > max) {
                Types.ACTIONS action = childActions.get(i);
                Position2D newPosition = Position2D.ModifyPosition(avatarPos, action);
                score = score - history.Count(newPosition);
                if(score > max){
                    max = score;
                    maxIndex = i;
                }
            }
        }
        return maxIndex;
    }

    public int GetBestAverageIndex() {
        double max = -Double.MAX_VALUE;
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

    @Override
    public int GetMostVisitedIndex(boolean useHistory){
        if(!useHistory || !IsRoot()){
            return GetBestScoreIndex();
        }
        PositionHistory history = PositionHistory.GetInstance();
        Position2D avatarPos = Position2D.GetAvatarPosition(state);
        double max = -Double.MAX_VALUE;
        int maxIndex = 0;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == null) {
                continue;
            }
            double score = Utilities.DisturbScore(children.get(i).numTests);
            if(score > max){
                Types.ACTIONS action = childActions.get(i);
                Position2D newPosition = Position2D.ModifyPosition(avatarPos, action);
                score = score - history.Count(newPosition);
                if(score > max){
                    max = score;
                    maxIndex = i;
                }
            }
        }
        return maxIndex;
    }

    @Override
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
