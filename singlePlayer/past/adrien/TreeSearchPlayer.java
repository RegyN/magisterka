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
    final double discountFactor;
    final int nbCategories;
    private double distancesNormalizer;
    //private int nbGridBuckets;
    //private double maxNbFeatures;
    private final int memoryLength;
    private final Vector2d[] pastAvatarPositions;
    private final double[] pastScores;
    private final double[] pastGameTicks;
    private int nbMemorizedPositions;
    private Vector2d barycenter;
    private double locationBiasWeight;
    private double barycenterBiasWeight;
    private int memoryIndex;

    /**
     * Creates the MCTS player with a sampleRandom generator object.
     *
     * @param a_rnd sampleRandom generator object.
     */
    TreeSearchPlayer(Random a_rnd) {
        randomGenerator = a_rnd;
        epsilon = 0.35;
        discountFactor = 0.9;
        nbCategories = 8;

        memoryIndex = 0;
        memoryLength = 500;
        pastAvatarPositions = new Vector2d[memoryLength];
        pastScores = new double[memoryLength];
        pastGameTicks = new double[memoryLength];
        nbMemorizedPositions = 0;
        barycenter = new Vector2d();
        locationBiasWeight = 0.;
        barycenterBiasWeight = 0.;
    }

    public void init(StateObservation a_gameState) {
        distancesNormalizer = Math.sqrt((Math.pow(a_gameState.getWorldDimension().getHeight() + 1.0, 2) + Math.pow(a_gameState.getWorldDimension().getWidth() + 1.0, 2)));

        rootObservation = a_gameState.copy();

        rootNode = new StateNode(rootObservation, randomGenerator, this);

        pastAvatarPositions[memoryIndex] = a_gameState.getAvatarPosition();
        if (nbMemorizedPositions < memoryLength) {
            nbMemorizedPositions += 1;
        }

        if (nbMemorizedPositions > 1) {
            double lambda = 0.15;
            Vector2d pastBarycenter = barycenter;
            Vector2d newPosition = new Vector2d(pastAvatarPositions[memoryIndex]);
            barycenter = new Vector2d(lambda * newPosition.x + (1. - lambda) * pastBarycenter.x, lambda * newPosition.y + (1. - lambda) * pastBarycenter.y);
        } else {
            barycenter = new Vector2d(pastAvatarPositions[memoryIndex]);
        }

        pastScores[memoryIndex] = a_gameState.getGameScore();
        pastGameTicks[memoryIndex] = a_gameState.getGameTick();

        if (nbMemorizedPositions > 1) {
            double lambda = 0.15;
            Vector2d pastBarycenter = barycenter;
            Vector2d newPosition = new Vector2d(pastAvatarPositions[memoryIndex]);
            Vector2d newBarycenter = new Vector2d(lambda * newPosition.x + (1. - lambda) * pastBarycenter.x, lambda * newPosition.y + (1. - lambda) * pastBarycenter.y);
            barycenter = newBarycenter;
        } else {
            barycenter = new Vector2d(pastAvatarPositions[memoryIndex]);
        }

        locationBiasWeight = calculateLocationBiasWeight();

        memoryIndex = ++memoryIndex % memoryLength;
    }

    /**
     * Wyznacza wagę LocationBias. Jest duża (0.25), jeśli rozrzut wyników w ostatnim czasie był duży, w przeciwnym
     * wypadku jest 0.001
     */
    private double calculateLocationBiasWeight() {
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

        return (scoreVariance > 0.001) ? 0.001 : 0.25;
    }

    void iterate() {
        //initialize useful variables
        StateObservation currentState = rootObservation.copy();
        StateNode currentStateNode = rootNode;
        int currentAction = 0;
        boolean stayInTree = true;
        int depth = 0;
        double score1;
        double score2 = 0.0;

        //System.out.format("game tick is %d%n ", currentState.getGameTick());
        //loop navigating through the tree
        while (stayInTree) {
            if (currentStateNode.notFullyExpanded()) {
                //add a new action
                int bestActionIndex = 0;
                if ((currentStateNode.numberOfSimulations < 2) && (currentStateNode.parentNode != null)) {
                    bestActionIndex = currentStateNode.parentAction;
                }
                else {
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

                if (currentState.isGameOver()) {
                    stayInTree = false;
                }
            }
            depth++;
        }

        currentStateNode.backPropagateData(currentState);
    }

    int returnBestAction() {
        //Determine the best action to take and return it.
        return rootNode.getHighestScoreAction();
    }

    double getValueOfState(StateObservation a_gameState) {

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

    double getBarycenterBias(StateObservation _state) {
        Vector2d myLocation = _state.getAvatarPosition();
        double tempBarycenterBias = Math.sqrt(myLocation.sqDist(barycenter)) / distancesNormalizer;
        return tempBarycenterBias;
    }

    double getLocationBias(StateObservation _state) {
        double power = 3.0;
        double tempLocationBias = 0.0;
        double timeDiscountFactor = 0.99;
        double currentTick = _state.getGameTick();
        for (int parsingIndex = 0; parsingIndex < memoryLength; parsingIndex++) {
            if (pastAvatarPositions[parsingIndex] == null)
                break;
            if ((pastAvatarPositions[parsingIndex].equals(_state.getAvatarPosition()))) {
                tempLocationBias += Math.pow(timeDiscountFactor, currentTick - pastGameTicks[parsingIndex]) * 0.01;
            }
        }
        return Math.pow(1.0 - tempLocationBias, power);
    }

    double getLocationBiasWeight() {
        return locationBiasWeight;
    }

    double getBarycenterBiasWeight() {
        return barycenterBiasWeight;
    }
}
