/**
 * Code written by Adrien Couetoux, acouetoux@ulg.ac.be It is free to use,
 * distribute, and modify. User: adrienctx Date: 12/01/2015
 */
package tracks.singlePlayer.advanced.adrienctx;

import core.game.StateObservation;

import java.util.*;

import ontology.Types;
import tools.Vector2d;

class StateNode {

    private static final double HUGE_NEGATIVE = -10000000.0;

    public final List<StateObservation> encounteredStates;

    public final StateNode[] children;

    public final int[] actionNbSimulations;

    private final double[] actionScores;

    private final Random randomGenerator;

    private int numberOfExits;

    private double cumulatedValueOnExit;

    private double passingValue;

    public int numberOfSimulations;

    private double maxScore;

    private final double rawScore;

    //Parent objects:
    public StateNode parentNode;

    public int parentAction;

    private final TreeSearchPlayer parentTree;

    private IntDoubleHashMap[] features;

    private double locationBias;

    private double barycenterBias;
    
    private double valueWhenLastBacktracked;

    private double featureGridBias;

    public boolean[] actionPruned;

    private int gameTick;

    private Vector2d orientation;

//    private double stateIsAlreadyInTheTree;

    // /**
    //  * Creates a new empty state node
    //  */
//    public StateNode(int _parentAction) {
//        parentAction = _parentAction;
//    }

    /**
     * Creates a new state node with one single state encountered
     *
     * @param _state , the encountered state
     */
    public StateNode(StateObservation _state, Random _random, TreeSearchPlayer _parentTree) {
        encounteredStates = new ArrayList<>();
        encounteredStates.add(_state);
        gameTick = _state.getGameTick();
        orientation = _state.getAvatarOrientation();
        children = new StateNode[Agent.NUM_ACTIONS];
        actionNbSimulations = new int[Agent.NUM_ACTIONS];
        actionScores = new double[Agent.NUM_ACTIONS];
        actionPruned = new boolean[Agent.NUM_ACTIONS];
        randomGenerator = _random;

        parentTree = _parentTree;

        rawScore = _state.getGameScore();

        features = new IntDoubleHashMap[parentTree.nbCategories];
        features = parentTree.getFeaturesFromStateObs(_state);
        locationBias = parentTree.getLocationBias(_state);
        barycenterBias = parentTree.getBarycenterBias(_state);
        featureGridBias = parentTree.getFeatureGridBias(features);
    }

    private StateNode(StateObservation _state, Random _random, StateNode _parentNode) {
        encounteredStates = new ArrayList<>();
        encounteredStates.add(_state);
        gameTick = _state.getGameTick();
        orientation = _state.getAvatarOrientation();
        children = new StateNode[Agent.NUM_ACTIONS];
        actionNbSimulations = new int[Agent.NUM_ACTIONS];
        actionScores = new double[Agent.NUM_ACTIONS];
        actionPruned = new boolean[Agent.NUM_ACTIONS];
        randomGenerator = _random;

        parentTree = _parentNode.parentTree;
        parentNode = _parentNode;

        rawScore = _state.getGameScore();

        features = new IntDoubleHashMap[parentTree.nbCategories];
        features = parentTree.getFeaturesFromStateObs(_state);
        locationBias = parentTree.getLocationBias(_state);
        barycenterBias = parentTree.getBarycenterBias(_state);
        featureGridBias = parentTree.getFeatureGridBias(features);
    }

    public IntDoubleHashMap[] getFeatures(){
        return this.features;
    }

    public IntDoubleHashMap[] getCopyOfFeatures(){
        IntDoubleHashMap[] copy = new IntDoubleHashMap[parentTree.nbCategories];

        for (int i=0; i < parentTree.nbCategories; i++){
            copy[i] = new IntDoubleHashMap(features[i]);
        }

        return(copy);
    }

    private double getNodeValue() {
        if(parentTree.useValueApproximation){
            return rawScore + parentTree.V_approximator.getBasisFunctionLinearApproximation(parentTree.V_approximator.getBasisFunctionsFromFeatures(features), parentTree.V_approximator.getWeights()) + locationBias * parentTree.getLocationBiasWeight() + barycenterBias * parentTree.getBarycenterBiasWeight() + featureGridBias * parentTree.getFeatureGridWeight();
        }
        else{
            return rawScore + locationBias * parentTree.getLocationBiasWeight() + barycenterBias * parentTree.getBarycenterBiasWeight();
        }
    }

    /**
     * Adds a new action node to the children if this state node
     *
     * @param _currentObservation , the current state observation to use in
     *                            the simulator (i.e. the advance method from Forward Model)
     */
    public StateNode addStateNode(StateObservation _currentObservation, int actionIndex) {

        StateNode newStateNode = new StateNode(_currentObservation, randomGenerator, this);

        //use the new state to create the action node
        newStateNode.parentAction = actionIndex;
        children[actionIndex] = newStateNode;
        actionNbSimulations[actionIndex] += 1;

        return newStateNode;
    }

    /**
     * returns the selected action node
     */
    public int selectRandomAction() {
        //now, we just select a random action
        int bestActionIndex = 0;
        double bestValue = -1;
        double x;
        for (int i = 0; i < children.length; i++) {
            x = randomGenerator.nextDouble();
            if (x > bestValue && children[i] != null) {
                bestActionIndex = i;
                bestValue = x;
            }
        }
        return bestActionIndex;
    }

    /**
     * returns the selected action node
     */
    public int selectAction() {
        //now, we just select a random action
        int bestActionIndex = 0;
        double bestValue = HUGE_NEGATIVE;
        double x;
        double actionValue;

        for (int i = 0; i < children.length; i++) {
            x = randomGenerator.nextDouble();
            actionValue = this.actionScores[i];
            if ((children[i] != null) && (actionValue + (x / 1000.0) > bestValue)) {
                bestActionIndex = i;
                bestValue = actionValue + (x / 1000.0);
            }
        }
        return bestActionIndex;
    }

    /**
     * Updates the data stored in this state node - TO DO: store more than just
     * state observations; should also store instant values
     */
    public void updateData(StateObservation _state) {
        encounteredStates.add(_state);
    }

    public int getMostVisitedAction() {
        //System.out.format("selecting the best action from TreeSearch : %n");
        int bestActionIndex = 0;
        double bestNumberOfVisits = -1;
        for (int i = 0; i < children.length; i++) {
            if ((double) actionNbSimulations[i] > bestNumberOfVisits && children[i] != null) {
                bestActionIndex = i;
                bestNumberOfVisits = (double) actionNbSimulations[i];
            }
        }
        return bestActionIndex;
    }

    public int getHighestScoreAction() {
        int bestActionIndex = 0;
        double bestScore = HUGE_NEGATIVE;
        for (int i = 0; i < children.length; i++) {
            double x = randomGenerator.nextDouble();
            if (((actionScores[i] + (x / 100000.0)) > bestScore) && (children[i] != null)) {
                bestActionIndex = i;
                bestScore = actionScores[i] + x / 100000.0;
            }
        }
        return bestActionIndex;
    }

    public void backPropagateData(StateObservation _state, ArrayList<IntDoubleHashMap[]> _visitedFeatures, ArrayList<Double> _visitedScores) {
        //Updating gameOver count and score
        boolean gameOver = _state.isGameOver();
        double _rawScore = _state.getGameScore();

        //////
        if (parentTree.useValueApproximation) {
            double cumulatedDiscountedScores = 0.0;
            for (int i = 0; i < _visitedScores.size() - 1; i++) {
                cumulatedDiscountedScores = parentTree.discountFactor * cumulatedDiscountedScores + (_visitedScores.get(i) - _visitedScores.get(i + 1));
                if(false){
                    parentTree.V_approximator.addTrajectory(_visitedFeatures.get(i + 1), _visitedFeatures.get(0), cumulatedDiscountedScores, gameOver);
                }

            }
        }

        //////

        if (gameOver) {
            this.numberOfExits += 1;
            cumulatedValueOnExit += parentTree.getValueOfState(_state);
        }

        StateNode currentNode = this;

        while (currentNode != null) {
            currentNode.numberOfSimulations += 1;
            if (!currentNode.isLeaf()) {
                if (true) {
                    double bestScore = HUGE_NEGATIVE;
                    for (int i = 0; i < currentNode.children.length; i++) {
                        if (currentNode.children[i] != null) {
                            if (currentNode.actionScores[i] > bestScore) {
                                bestScore = currentNode.actionScores[i];
                            }
                        }
                    }
                    currentNode.maxScore = bestScore;
                } else {
                    double cumulatedScore = 0.0;
                    int totalSims = 0;
                    for (int i = 0; i < currentNode.children.length; i++) {
                        if (currentNode.children[i] != null) {
                            cumulatedScore += currentNode.actionNbSimulations[i] * currentNode.actionScores[i];
                            totalSims += currentNode.actionNbSimulations[i];
                        }
                    }
                    currentNode.maxScore = cumulatedScore / (double) totalSims;
                }

            }

            if (currentNode.parentNode != null) {
                currentNode.valueWhenLastBacktracked = currentNode.getNodeValue();
                currentNode.passingValue = currentNode.getNodeValue() - currentNode.parentNode.getNodeValue() + parentTree.discountFactor * currentNode.maxScore;
                int _actionIndex = currentNode.parentAction;
                currentNode.parentNode.actionScores[_actionIndex] = (currentNode.cumulatedValueOnExit / (double) currentNode.numberOfSimulations) + ((double) (currentNode.numberOfSimulations - currentNode.numberOfExits) / (double) currentNode.numberOfSimulations) * currentNode.passingValue;
            }

            currentNode = currentNode.parentNode;
        }
    }

    public void puzzleBackProp() {
        //Updating gameOver count and score
        StateObservation _state = this.encounteredStates.get(0);
        boolean gameOver = _state.isGameOver();
        double _rawScore = _state.getGameScore();
        StateNode currentNode = this;

        if (gameOver) {
            this.numberOfExits += 1;
            cumulatedValueOnExit += parentTree.getValueOfState(_state);
            currentNode.maxScore = parentTree.getValueOfState(_state);
        }
        else{
            currentNode.maxScore = _rawScore;
        }

        while (currentNode != null) {
            currentNode.numberOfSimulations += 1;
            if (!currentNode.isLeaf()) {
                double bestScore = HUGE_NEGATIVE;
                for (int i = 0; i < currentNode.children.length; i++) {
                    if (currentNode.children[i] != null) {
                        if (currentNode.actionScores[i] > bestScore) {
                            bestScore = currentNode.actionScores[i];
                        }
                    }
                }
                if(bestScore > currentNode.maxScore){
                    currentNode.maxScore = 1.0 * bestScore;
                }
            }

            if (currentNode.parentNode != null) {
                int _actionIndex = currentNode.parentAction;
                currentNode.parentNode.actionScores[_actionIndex] = currentNode.maxScore;
                if(!currentNode.notFullyExpanded()){
                    currentNode.parentNode.actionPruned[_actionIndex] = currentNode.allChildrenPruned();
                }
            }

            currentNode = currentNode.parentNode;
        }
    }
    
    public boolean notFullyExpanded() {
        for (StateNode children1 : children) {
            if (children1 == null) {
                return true;
            }
        }
        return false;
    }

    public boolean allChildrenPruned(){
        for (boolean b : actionPruned){
            if(b == false){
                return false;
            }
        }
        return true;
    }

    private boolean isLeaf() {
        for (StateNode children1 : children) {
            if (children1 != null) {
                return false;
            }
        }
        return true;
    }

    public void printTree(int depth) {
        System.out.format("%n ##### Printing tree at depth %d and nbsims %d %n", depth, numberOfSimulations);
        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                System.out.format("%n ~~~~~~~~~~~~ action number %d :", i);
                System.out.format("%n nbSims, score, avgOnExit, PassingV, maxScore, nbExists, nodeValue, tick: %d, %f, %f, %f, %f, %d, %f, %d", actionNbSimulations[i], actionScores[i], children[i].cumulatedValueOnExit / (double) children[i].numberOfExits, children[i].passingValue, children[i].maxScore, children[i].numberOfExits, children[i].getNodeValue(), children[i].encounteredStates.get(0).getGameTick());
            }
        }
    }

    public double getActionScore(int index){
        return actionScores[index];
    }
}
