package tracks.singlePlayer.advanced.tkomisarczyk.MonteCarlo;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Utils;
import tracks.singlePlayer.advanced.tkomisarczyk.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreeNode {
    TreeNode parent;
    int depth;
    int maxDepth;
    List<TreeNode> children;
    List<Types.ACTIONS> childActions;
    int numTests = 0;
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
        this.maxDepth = maxDepth;
        this.depth = 0;
        this.parent = null;
        this.generator = new Random();
        this.state = obs;
    }
    
    TreeNode(TreeNode parent, StateObservation obs) {
        this.maxDepth = parent.maxDepth;
        this.depth = parent.depth + 1;
        this.parent = parent;
        this.generator = parent.generator;
        int result;
        if(GameKnowledge.getInstance().type == GameType.Planar2D) {
            result = RollSimulationCleverly(obs, generator);
        }
        else{
            result = RollSimulation(obs, generator);
        }
        UpdateScoreUpwards(result);
    }
    
    private int RollSimulation(StateObservation obs, Random generator) {
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

    // Symulacje prowadzone w taki sposób, że jeśli została wylosowana akcja prowadząca w ścianę, to losowane jest jeszcze raz
    // Możliwe jest, że za drugim razem wylosowane będzie to samo
    private int RollSimulationCleverly(StateObservation obs, Random generator){
        var actions = obs.getAvailableActions();
        var knowledge = GameKnowledge.getInstance();
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
        return Utilities.EvaluateState(obs, d);
    }

    private int RollSimulationCleverlyWithHistory(StateObservation obs, Random generator){
        var actions = obs.getAvailableActions();
        var knowledge = GameKnowledge.getInstance();
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
        return Utilities.EvaluateState(obs, d);
    }

    // Symulacje prowadzone w taki sposób, ze niemożliwe jest zrobienie ruchu w ścianę itp.
    private int RollSimulationBetter(StateObservation obs, Random generator){
        var actions = obs.getAvailableActions();
        var knowledge = GameKnowledge.getInstance();
        int d = depth;
        for (; d < maxDepth; d++) {
            if (obs.isGameOver())
                break;
            int[] actionMap = new int[actions.size()]; // indeks to wylosowany numer, wartość to akcja do wykonania
            int choiceSize = 0;
            Position2D avatarPos = Position2D.GetAvatarPosition(obs);
            for(int i=0; i<actions.size(); i++){
                if(actions.get(i) == Types.ACTIONS.ACTION_UP){
                    if(!knowledge.CheckForWalls(Position2D.GetObservations(obs, avatarPos.x, avatarPos.y-knowledge.avatarSpeed))){
                        actionMap[choiceSize] = i;
                        choiceSize++;
                    }
                }
                else if(actions.get(i) == Types.ACTIONS.ACTION_DOWN){
                    if(!knowledge.CheckForWalls(Position2D.GetObservations(obs, avatarPos.x, avatarPos.y+knowledge.avatarSpeed))){
                        actionMap[choiceSize] = i;
                        choiceSize++;
                    }
                }
                else if(actions.get(i) == Types.ACTIONS.ACTION_LEFT){
                    if(!knowledge.CheckForWalls(Position2D.GetObservations(obs, avatarPos.x-knowledge.avatarSpeed, avatarPos.y))){
                        actionMap[choiceSize] = i;
                        choiceSize++;
                    }
                }
                else if(actions.get(i) == Types.ACTIONS.ACTION_RIGHT){
                    if(!knowledge.CheckForWalls(Position2D.GetObservations(obs, avatarPos.x+knowledge.avatarSpeed, avatarPos.y))){
                        actionMap[choiceSize] = i;
                        choiceSize++;
                    }
                }
                else{
                    actionMap[choiceSize] = i;
                    choiceSize++;
                }
            }
            int choice = generator.nextInt(choiceSize);
            choice = actionMap[choice];
            obs.advance(actions.get(choice));
        }
        return Utilities.EvaluateState(obs, d);
    }

    public void Expand(StateObservation obs) {
        if(this.IsRoot()){
            obs = obs.copy();
        }
        var actions = obs.getAvailableActions();
        if(actions.size() <= 0){  // Czasami z powodu niedeterminizmu gra kończy się wcześniej niż by się można spodziewać. Wtedy wracamy do korzenia.
            var result = Utilities.EvaluateState(obs);
            UpdateScoreUpwards(result);
            System.out.print(".");
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
        var actions = obs.getAvailableActions();
        if(actions.size() <= 0){  // Czasami z powodu niedeterminizmu gra kończy się wcześniej niż by się można spodziewać. Wtedy wracamy do korzenia.
            var result = Utilities.EvaluateState(obs);
            UpdateScoreUpwards(result);
            System.out.print(".");
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

    public Types.ACTIONS GetBestScoreAction(boolean useHistory) {
        return childActions.get(GetBestScoreIndex(useHistory));
    }

    public Types.ACTIONS GetBestAverageAction() {
        return childActions.get(GetBestAverageIndex());
    }

    public Types.ACTIONS GetMostVisitedAction() {
        return childActions.get(GetMostVisitedIndex());
    }

    public  int GetBestScoreIndex(boolean useHistory){
        if(!useHistory || !IsRoot()){
            return GetBestScoreIndex();
        }
        var history = PositionHistory.GetInstance();
        var correctionFactor = 0.8;
        var avatarPos = Position2D.GetAvatarPosition(state);
        double max = Double.MIN_VALUE;
        int maxIndex = 0;
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == null) {
                continue;
            }
            var score = Utilities.DisturbScore(children.get(i).sumScore);
            if(score > max){
                var action = childActions.get(i);
                Position2D newPosition = avatarPos;
                //Jeśli nie ma go w historii, to mamy nowy max.
                if(action == Types.ACTIONS.ACTION_UP){
                    newPosition = new Position2D(avatarPos.x, avatarPos.y - 1);
                }
                else if(action == Types.ACTIONS.ACTION_DOWN){
                    newPosition = new Position2D(avatarPos.x, avatarPos.y + 1);
                }
                else if(action == Types.ACTIONS.ACTION_LEFT){
                    newPosition = new Position2D(avatarPos.x - 1, avatarPos.y);
                }
                else if(action == Types.ACTIONS.ACTION_RIGHT){
                    newPosition = new Position2D(avatarPos.x + 1, avatarPos.y);
                }
                if(history.Contains(newPosition)){
                    score = (int)(score * correctionFactor);
                }
                if(score > max){
                    max = score;
                    maxIndex = i;
                }
            }
        }
        return maxIndex;
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
