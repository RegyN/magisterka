/**
 * Code written by Adrien Couetoux, acouetoux@ulg.ac.be It is free to use,
 * distribute, and modify. User: adrienctx Date: 12/01/2015
 */
package tracks.singlePlayer.past.adrien;

import core.game.Observation;
import core.game.StateObservation;

import ontology.Types;
import tools.Vector2d;

import java.util.*;

class TreeSearchPlayer {

    private StateNode rootNode;
    private StateObservation rootObservation;
    private final Random randomGenerator;
    private final double epsilon;
    public final double discountFactor;
    public final int nbCategories;
    private double maxResources;
    private double distancesNormalizer;
    private int nbGridBuckets;
    private IntArrayOfDoubleHashMap[] pastFeaturesGrid;
    private double maxNbFeatures;
    private final int memoryLength;
    private final Vector2d[] pastAvatarPositions;
    private final Vector2d[] pastAvatarOrientations;
    private final double[] pastScores;
    private final double[] pastGameTicks;
    private double sumOfPastX;
    private double sumOfPastY;
    private int nbMemorizedPositions;
    private Vector2d barycenter;
    private double locationBiasWeight;
    private double barycenterBiasWeight;
    private double featureGridWeight;
    private int memoryIndex;
    private boolean frozen;
    public int ticksSinceFrozen;
    public ArrayList<StateObservation> visitedStates;
    /**
     * Creates the MCTS player with a sampleRandom generator object.
     *
     * @param a_rnd sampleRandom generator object.
     */
    public TreeSearchPlayer(Random a_rnd) {
        randomGenerator = a_rnd;
        epsilon = 0.35;
        discountFactor = 0.9;
        nbCategories = 8;
        maxResources = 20.0;
        nbGridBuckets = 50;
        maxNbFeatures = 0.;

        memoryIndex = 0;
        memoryLength = 500;
        pastAvatarPositions = new Vector2d[memoryLength];
        pastAvatarOrientations = new Vector2d[memoryLength];
        pastScores = new double[memoryLength];
        pastGameTicks = new double[memoryLength];
        sumOfPastX = 0.;
        sumOfPastY = 0.;
        nbMemorizedPositions = 0;
        barycenter = new Vector2d();
        locationBiasWeight = 0.;
        barycenterBiasWeight = 0.;
        featureGridWeight = 0.5;

        frozen = false;

        pastFeaturesGrid = new IntArrayOfDoubleHashMap[nbCategories];
        for (int i = 0; i < nbCategories; i++) {
            pastFeaturesGrid[i] = new IntArrayOfDoubleHashMap();
        }

        visitedStates = new ArrayList<>();
    }

    public void init(StateObservation a_gameState) {
        distancesNormalizer = Math.sqrt((Math.pow(a_gameState.getWorldDimension().getHeight() + 1.0, 2) + Math.pow(a_gameState.getWorldDimension().getWidth() + 1.0, 2)));

        rootObservation = a_gameState.copy();
        int rootGameTick = rootObservation.getGameTick();
        IntDoubleHashMap[] rootFeatures = getFeaturesFromStateObs(a_gameState);
        updatePastFeaturesGrid(rootFeatures);

        rootNode = new StateNode(rootObservation, randomGenerator, this);

        //TODO : when I implement a "keep tree when in puzzle", no more need to reinitialize the visitedStates list
        visitedStates = new ArrayList<>();
//        MyStateObservation myRootObservation = new MyStateObservation(rootObservation);
        visitedStates.add(rootObservation);
        ticksSinceFrozen = 0;
        //rootNode.parentTree = this;



//        updateLinearRegressionUsingDatabase();

        if (pastAvatarPositions[memoryIndex] != null) {
            sumOfPastX -= pastAvatarPositions[memoryIndex].x;
            sumOfPastY -= pastAvatarPositions[memoryIndex].y;
        }
        pastAvatarPositions[memoryIndex] = a_gameState.getAvatarPosition();
        sumOfPastX += pastAvatarPositions[memoryIndex].x;
        sumOfPastY += pastAvatarPositions[memoryIndex].y;
        if (nbMemorizedPositions < memoryLength) {
            nbMemorizedPositions += 1;
        }

        if (nbMemorizedPositions > 1) {
            double lambda = 0.15;
            Vector2d pastBarycenter = barycenter;
            Vector2d newPosition = new Vector2d(pastAvatarPositions[memoryIndex]);
            Vector2d newBarycenter = new Vector2d(lambda * newPosition.x + (1. - lambda) * pastBarycenter.x, lambda * newPosition.y + (1. - lambda) * pastBarycenter.y);
//          barycenter.set(sumOfPastX/nbMemorizedPositions, sumOfPastY/nbMemorizedPositions);
            barycenter = newBarycenter;
        } else {
            barycenter = new Vector2d(pastAvatarPositions[memoryIndex]);
        }

        pastScores[memoryIndex] = a_gameState.getGameScore();
        pastGameTicks[memoryIndex] = a_gameState.getGameTick();

        double scoreMean = 0.;
        double nbScores = 0.;
        double scoreVariance = 0.;

        for (double x : pastScores) {
            scoreMean += x;
            nbScores += 1.;
        }
        scoreMean = scoreMean / nbScores;
        for (double x : pastScores) {
            scoreVariance += Math.pow(x - scoreMean, 2.);
        }
        scoreVariance = scoreVariance / nbScores;

//        if(false){
        if (scoreVariance > 0.001) {
            barycenterBiasWeight = 0.005;
            locationBiasWeight = 0.001;
        } else {
            locationBiasWeight = 0.25;
            barycenterBiasWeight = 0.005;
        }

        pastAvatarOrientations[memoryIndex] = a_gameState.getAvatarOrientation();
        if (memoryIndex < memoryLength - 1) {
            memoryIndex += 1;
        } else {
            memoryIndex = 0;
        }


    }
    
    public void iterate() {
        ArrayList<IntDoubleHashMap[]> visitedStatesFeatures = new ArrayList<>();
        ArrayList<Double> visitedStatesScores = new ArrayList<>();
        //initialize useful variables
        StateObservation currentState = rootObservation.copy();
        StateNode currentStateNode = rootNode;
        int currentAction = 0;
        boolean stayInTree = true;
        int depth = 0;
        IntDoubleHashMap[] features1 = new IntDoubleHashMap[nbCategories];
        IntDoubleHashMap[] features2 = new IntDoubleHashMap[nbCategories];
        double score1;
        double score2 = 0.0;
        double instantReward;

        //System.out.format("game tick is %d%n ", currentState.getGameTick());
        //loop navigating through the tree
        while (stayInTree) {
            score1 = getValueOfState(currentState);
            visitedStatesScores.add(0, score1);


            if (currentStateNode.notFullyExpanded()) {
                //add a new action
                int bestActionIndex = 0;
                if ((currentStateNode.numberOfSimulations < 2) && (currentStateNode.parentNode != null)) {
                    bestActionIndex = currentStateNode.parentAction;
                } else {
                    double bestValue = -1;
                    for (int i = 0; i < currentStateNode.children.length; i++) {
                        double x = randomGenerator.nextDouble();
                        if ((x > bestValue) && (currentStateNode.children[i] == null)) {
                            bestActionIndex = i;
                            bestValue = x;
                        }
                    }
                }

                currentState.advance(Agent.actions[bestActionIndex]);
                //test here to see if the new state has extra types per category

                currentStateNode = currentStateNode.addStateNode(currentState, bestActionIndex);  //creates a new action node, with its child state node - It modifies currentState!

//                features2 = currentStateNode.features;
                score2 = getValueOfState(currentState);
                instantReward = score2 - score1;

                stayInTree = false;
            }
            else {
                //select an action
                double x = randomGenerator.nextDouble();
                currentAction = currentStateNode.selectAction();
                if (x < epsilon) {
                    currentAction = currentStateNode.selectRandomAction();
                }
                currentStateNode.actionNbSimulations[currentAction] += 1;
                currentStateNode = currentStateNode.children[currentAction];
    
                //TODO: refine this condition to something that triggers calling the forward model
                currentState.advance(Agent.actions[currentAction]); //updates the current state with the forward model
                //currentStateNode.updateData(currentState.copy());
                score2 = getValueOfState(currentState);
                instantReward = score2 - score1;

                if (currentState.isGameOver()) {
                    stayInTree = false;
                }
            }
            depth++;
        }

        visitedStatesFeatures.add(0, features2);
        visitedStatesScores.add(0, score2);

        currentStateNode.backPropagateData(currentState, visitedStatesFeatures, visitedStatesScores);
    }
    
    public int returnBestAction() {
        //Determine the best action to take and return it.
//        return rootNode.getHighestScoreAction();
        return rootNode.getHighestScoreAction();
    }

    protected double getValueOfState(StateObservation a_gameState) {

        boolean gameOver = a_gameState.isGameOver();
        Types.WINNER win = a_gameState.getGameWinner();
        double rawScore = a_gameState.getGameScore();

        if (gameOver && win == Types.WINNER.PLAYER_LOSES) {
            return (rawScore - 2000.0 * (1.0 + Math.abs(rawScore)));
        }

        if (gameOver && win == Types.WINNER.PLAYER_WINS) {
            return (rawScore + 2.0 * (1.0 + Math.abs(rawScore)));
//            return (rawScore + (double) a_gameState.getGameTick() / 1000.0);
        }

        return rawScore;
    }

    public IntDoubleHashMap[] getFeaturesFromStateObs(StateObservation _state) {
        double power = 3.0;
        IntDoubleHashMap distanceToResources = new IntDoubleHashMap();
        IntDoubleHashMap avatarResources = new IntDoubleHashMap();
        IntDoubleHashMap distanceToNPC = new IntDoubleHashMap();
        IntDoubleHashMap distanceToMovables = new IntDoubleHashMap();
        IntDoubleHashMap distanceToImmovables = new IntDoubleHashMap();
        IntDoubleHashMap distanceToPortals = new IntDoubleHashMap();
        IntDoubleHashMap healthPoints = new IntDoubleHashMap();

        //Computing Deterministic bias values
        Vector2d myLocation = _state.getAvatarPosition();
        int i;
        double featureValue = 0.;
        ArrayList<Observation>[] observationListArray = _state.getResourcesPositions(myLocation);

        if (observationListArray != null) {
            i = 0;
            while (i < observationListArray.length) {
                if (!observationListArray[i].isEmpty()) {
                    featureValue = Math.pow(1.0 - Math.min(1.0, Math.sqrt(observationListArray[i].get(0).sqDist) / distancesNormalizer), power);
                    distanceToResources.put(observationListArray[i].get(0).itype, featureValue);
                }
                i++;
            }
        }

        double tempTotalResources = 0.0;
        if (!_state.getAvatarResources().isEmpty()) {
            for (int key : _state.getAvatarResources().keySet()) {
                if((double) _state.getAvatarResources().get(key) > maxResources){
                    maxResources = (double) _state.getAvatarResources().get(key);
                }
                tempTotalResources = (double) _state.getAvatarResources().get(key) / maxResources;
                avatarResources.put(key, Math.pow(tempTotalResources, power));
            }
        }

        observationListArray = _state.getNPCPositions(myLocation);
        if (observationListArray != null) {
            i = 0;
            while (i < observationListArray.length) {
                if (!observationListArray[i].isEmpty()) {
                    featureValue = Math.pow(1.0 - Math.min(1.0, Math.sqrt(observationListArray[i].get(0).sqDist) / distancesNormalizer), power);
                    distanceToNPC.put(observationListArray[i].get(0).itype, featureValue);
                }
                i++;
            }
        }

        observationListArray = _state.getMovablePositions(myLocation);
        if (observationListArray != null) {
            i = 0;
            while (i < observationListArray.length) {
                if (!observationListArray[i].isEmpty()) {
                    featureValue = Math.pow(1.0 - Math.min(1.0, Math.sqrt(observationListArray[i].get(0).sqDist) / distancesNormalizer), power);
                    distanceToMovables.put(observationListArray[i].get(0).itype, featureValue);
                }
                i++;
            }
        }

//        observationListArray = _state.getImmovablePositions(myLocation);
//        if (observationListArray != null) {
//            i = 0;
//            while (i < observationListArray.length) {
//                if (!observationListArray[i].isEmpty()) {
//                    featureValue = Math.pow(1.0 - Math.min(1.0, Math.sqrt(observationListArray[i].get(0).sqDist) / distancesNormalizer), power);
//                    distanceToImmovables.put(observationListArray[i].get(0).itype, featureValue);
//                }
//                i++;
//            }
//        }

        observationListArray = _state.getPortalsPositions(myLocation);
        if (observationListArray != null) {
            i = 0;
            while (i < observationListArray.length) {
                if (!observationListArray[i].isEmpty()) {
                    featureValue = Math.pow(1.0 - Math.min(1.0, Math.sqrt(observationListArray[i].get(0).sqDist) / distancesNormalizer), power);
                    distanceToPortals.put(observationListArray[i].get(0).itype, featureValue);
                }
                i++;
            }
        }

        if (!_state.isGameOver()) {
            healthPoints.put(0, 1.0 - (double) _state.getAvatarHealthPoints() / _state.getAvatarLimitHealthPoints());
        }

        IntDoubleHashMap[] features = new IntDoubleHashMap[nbCategories];
        for (int j = 0; j < nbCategories; j++) {
            features[j] = new IntDoubleHashMap();
        }

        IntDoubleHashMap offset = new IntDoubleHashMap();
        offset.put(0, 1.0);
        features[0] = offset;
        features[1] = distanceToResources;
        features[2] = avatarResources;
        features[3] = distanceToNPC;
        features[4] = distanceToMovables;
        features[5] = distanceToImmovables;
        features[6] = distanceToPortals;
        features[7] = healthPoints;
        return features;
    }

    public double getBarycenterBias(StateObservation _state) {
        Vector2d myLocation = _state.getAvatarPosition();
        double tempBarycenterBias = Math.sqrt(myLocation.sqDist(barycenter)) / distancesNormalizer;
        return tempBarycenterBias;
    }

    public double getLocationBias(StateObservation _state) {
        double power = 3.0;
        int parsingIndex = 0;
        double tempLocationBias = 0.0;
        double timeDiscountFactor = 0.99;
        double currentTick = _state.getGameTick();
        while ((parsingIndex < memoryLength) && (pastAvatarPositions[parsingIndex] != null)) {
            if ((pastAvatarPositions[parsingIndex].equals(_state.getAvatarPosition()))) {
                tempLocationBias += Math.pow(timeDiscountFactor, currentTick - pastGameTicks[parsingIndex]) * 0.01;
            }
            parsingIndex++;
        }
        return Math.pow(1.0 - tempLocationBias, power);
    }

    double getLocationBiasWeight() {
        return locationBiasWeight;
    }

    double getBarycenterBiasWeight() {
        return barycenterBiasWeight;
    }

    public void updatePastFeaturesGrid(IntDoubleHashMap[] _features) {

        double nbFeatures = 0.;
        for (int i = 0; i < _features.length; i++) {
            for (Integer type : _features[i].keySet()) {
                nbFeatures += 1.;
                int bucketIndex = (int) Math.floor(_features[i].get(type) * nbGridBuckets);

                if (!pastFeaturesGrid[i].containsKey(type)) {
                    double[] newBucketVector = new double[nbGridBuckets + 1];
                    for (int j = 0; j < newBucketVector.length; j++) {
                        newBucketVector[j] = 0.;
                    }
                    newBucketVector[bucketIndex] = 1.;
                    pastFeaturesGrid[i].put(type, newBucketVector);
                }
                else{
                    double[] oldBucketVector = pastFeaturesGrid[i].get(type);
                    oldBucketVector[bucketIndex] = oldBucketVector[bucketIndex] + 1.;
                    pastFeaturesGrid[i].put(type, oldBucketVector);
                }
            }
        }
        if(nbFeatures > maxNbFeatures){
            maxNbFeatures = nbFeatures;
        }
    }

    public double getFeatureGridBias(IntDoubleHashMap[] _features){

        double score = 0.;
        int bucketIndex = 0;
        for (int i=0; i<_features.length; i++){
            for (Integer key : _features[i].keySet()){
                bucketIndex = (int) (Math.max(0.0, Math.min(Math.floor(_features[i].get(key)), 1.0)) * nbGridBuckets);
                if ( !((pastFeaturesGrid[i].containsKey(key)) && pastFeaturesGrid[i].get(key)[bucketIndex] > 0.0) ){
                    score += 1.;
//                    score = 1.;
                }
            }
        }
//        return score;
        return(score/maxNbFeatures);
    }

    public double getFeatureGridWeight(){
        return featureGridWeight;
    }
    
    /*
    public void freezeTree() {this.frozen = true;}

    public void deFrost() {this.frozen = false;}

    public boolean isFrozen() {return this.frozen;}

    public boolean puzzleActionFound() {
        boolean result = false;

        if(this.rootNode.children.length < 1){
            return true;
        }
        else{
            double firstChildScore = this.rootNode.getActionScore(0);
            int i = 1;
            while( (!result) && (i<this.rootNode.children.length) ){
                if(this.rootNode.getActionScore(i) != firstChildScore){
                    result = true;
                }
                i++;
            }
            return result;
        }
    }
    
    public boolean areTwoStatesEqual(StateObservation s1, StateObservation s2) {

        Vector2d pos1 = s1.getAvatarPosition();
        Vector2d pos2 = s2.getAvatarPosition();
        Vector2d orientation1 = s1.getAvatarOrientation();
        Vector2d orientation2 = s2.getAvatarOrientation();

        int t1 = s1.getGameTick();
        int t2 = s2.getGameTick();

        if(!(pos1.equals(pos2))){
            return false;
        }
        if(!(orientation1.equals(orientation2))){
            return false;
        }

        ArrayList<Observation>[] obsList1 = s1.getNPCPositions();
        ArrayList<Observation>[] obsList2 = s2.getNPCPositions();
        if(!areTwoObsListEqual(obsList1, obsList2)){
            return false;
        }
        obsList1 = s1.getMovablePositions();
        obsList2 = s2.getMovablePositions();
        if(!areTwoObsListEqual(obsList1, obsList2)){
            return false;
        }
        obsList1 = s1.getResourcesPositions();
        obsList2 = s2.getResourcesPositions();
        if(!areTwoObsListEqual(obsList1, obsList2)){
            return false;
        }
        return true;
    }

    private boolean areTwoObsListEqual(ArrayList<Observation>[] obsList1, ArrayList<Observation>[] obsList2){
        int i = 0;
        int j = 0;
        if ((obsList1 != null) && (obsList2 != null)) {
            if (obsList1.length != obsList2.length) {
                return false;
            } else {
                while (i < obsList1.length) {
                    if (obsList1[i].size() != obsList2[i].size()) {
                        return false;
                    } else {
                        while (j < obsList1[i].size()) {
                            if (!obsList1[i].get(j).equals(obsList2[i].get(j))) {
                                return false;
                            }
                            j++;
                        }
                    }
                    i++;
                }
            }
        }
        return true;
    }

    private boolean isStateAlreadyVisited(StateObservation _state){
        for (StateObservation s : visitedStates){
            Vector2d pos1 = s.getAvatarPosition();
            Vector2d pos2 = _state.getAvatarPosition();
            Vector2d orientation1 = s.getAvatarOrientation();
            Vector2d orientation2 = _state.getAvatarOrientation();
            if(pos1.equals(pos2) && orientation1.equals(orientation2)){
                if (areTwoStatesEqual(_state, s)){
                    return true;
                }
            }
        }
        return false;
    }

    public double getHighestScoreFromRoot(){
        return rootNode.getActionScore(rootNode.getHighestScoreAction());
    }
    */
}
