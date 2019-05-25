/**
 * Code written by Adrien Couetoux, acouetoux@ulg.ac.be It is free to use,
 * distribute, and modify. User: adrienctx Date: 12/01/2015
 */
package tracks.singlePlayer.past.adrien;

import core.game.StateObservation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class StateNode {

    private static final double HUGE_NEGATIVE = -10000000.0;

    private final List<StateObservation> encounteredStates;

    public final StateNode[] children;

    final int[] actionNbSimulations;

    private final double[] actionScores;

    private final Random randomGenerator;

    private int numberOfExits;

    private double cumulatedValueOnExit;

    private double passingValue;

    int numberOfSimulations;

    private double maxScore;

    private final double rawScore;

    //Parent objects:
    StateNode parentNode;

    int parentAction;

    private final TreeSearchPlayer parentTree;

    private IntDoubleHashMap[] features;

    private double locationBias;

    private double barycenterBias;
    
    /**
     * Creates a new state node with one single state encountered
     *
     * @param _state , the encountered state
     */
    StateNode(StateObservation _state, Random _random, TreeSearchPlayer _parentTree) {
        encounteredStates = new ArrayList<>();
        encounteredStates.add(_state);
        children = new StateNode[Agent.NUM_ACTIONS];
        actionNbSimulations = new int[Agent.NUM_ACTIONS];
        actionScores = new double[Agent.NUM_ACTIONS];
        randomGenerator = _random;

        parentTree = _parentTree;

        rawScore = _state.getGameScore();

        features = new IntDoubleHashMap[parentTree.nbCategories];
        features = parentTree.getFeaturesFromStateObs(_state);
        locationBias = parentTree.getLocationBias(_state);
        barycenterBias = parentTree.getBarycenterBias(_state);
    }

    private StateNode(StateObservation _state, Random _random, StateNode _parentNode) {
        encounteredStates = new ArrayList<>();
        encounteredStates.add(_state);
        children = new StateNode[Agent.NUM_ACTIONS];
        actionNbSimulations = new int[Agent.NUM_ACTIONS];
        actionScores = new double[Agent.NUM_ACTIONS];
        randomGenerator = _random;

        parentTree = _parentNode.parentTree;
        parentNode = _parentNode;

        rawScore = _state.getGameScore();

        features = new IntDoubleHashMap[parentTree.nbCategories];
        features = parentTree.getFeaturesFromStateObs(_state);
        locationBias = parentTree.getLocationBias(_state);
        barycenterBias = parentTree.getBarycenterBias(_state);
    }

    public IntDoubleHashMap[] getFeatures(){
        return this.features;
    }

    private double getNodeValue() {
        return rawScore + locationBias * parentTree.getLocationBiasWeight() + barycenterBias * parentTree.getBarycenterBiasWeight();
    }

    /**
     * Adds a new action node to the children if this state node
     *
     * @param _currentObservation , the current state observation to use in
     *                            the simulator (i.e. the advance method from Forward Model)
     */
    StateNode addStateNode(StateObservation _currentObservation, int actionIndex) {
        StateNode newStateNode = new StateNode(_currentObservation, randomGenerator, this);

        newStateNode.parentAction = actionIndex;
        children[actionIndex] = newStateNode;
        actionNbSimulations[actionIndex] += 1;

        return newStateNode;
    }
    
    int selectRandomAction() {
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
    
    int selectAction() {
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

    public int getMostVisitedAction() {
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

    int getHighestScoreAction() {
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

    void backPropagateData(StateObservation _state, ArrayList<IntDoubleHashMap[]> _visitedFeatures, ArrayList<Double> _visitedScores) {
        //Updating gameOver count and score
        boolean gameOver = _state.isGameOver();

        if (gameOver) {
            this.numberOfExits += 1;
            cumulatedValueOnExit += parentTree.getValueOfState(_state);
        }

        StateNode currentNode = this;

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
                currentNode.maxScore = bestScore;
            }

            if (currentNode.parentNode != null) {
                currentNode.passingValue = currentNode.getNodeValue() - currentNode.parentNode.getNodeValue() + parentTree.discountFactor * currentNode.maxScore;
                int _actionIndex = currentNode.parentAction;
                currentNode.parentNode.actionScores[_actionIndex] = (currentNode.cumulatedValueOnExit / (double) currentNode.numberOfSimulations) + ((double) (currentNode.numberOfSimulations - currentNode.numberOfExits) / (double) currentNode.numberOfSimulations) * currentNode.passingValue;
            }

            currentNode = currentNode.parentNode;
        }
    }
    
    /**
     * returns true if and only if the
     */
    public boolean notFullyExpanded() {
        for (StateNode children1 : children) {
            if (children1 == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * returns true if and only if the
     */
    private boolean isLeaf() {
        for (StateNode children1 : children) {
            if (children1 != null) {
                return false;
            }
        }
        return true;
    }
}
