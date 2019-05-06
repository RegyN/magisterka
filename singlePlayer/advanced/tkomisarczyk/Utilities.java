package tracks.singlePlayer.advanced.tkomisarczyk;

import core.game.StateObservation;
import ontology.Types;
import tracks.singlePlayer.tools.Heuristics.WinScoreHeuristic;

import java.util.Random;

public class Utilities {
    public static Random generator = new Random();
    public static double NormalizeScore(double score, double upperBound, double lowerBound){
        return (score - lowerBound) / (upperBound - lowerBound + 0.000001d);
    }

    public static double DisturbScore(double score){
        return score + generator.nextDouble()/100;
    }
    
    public static int EvaluateState(StateObservation obs) {
        return EvaluateState(obs, 1);
    }
    
    public static int EvaluateState(StateObservation obs, int turns) {
        int largeNumber = 100000;
        boolean gameOver = obs.isGameOver();
        Types.WINNER win = obs.getGameWinner();
        double rawScore = obs.getGameScore();
    
        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            return -largeNumber/turns;
    
        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            return largeNumber;
    
        return (int)rawScore;
    }

    public static boolean AreStatesEqual(StateObservation first, StateObservation second){
        //Co trzeba porównywać?
        //// Avatar
        if(first.getAvatarHealthPoints() != second.getAvatarHealthPoints()
                || first.getAvatarPosition().x != second.getAvatarPosition().x
                || first.getAvatarPosition().y != second.getAvatarPosition().y
                || first.getAvatarOrientation().x != second.getAvatarOrientation().x
                || first.getAvatarOrientation().y != second.getAvatarOrientation().y) {
            return false;
        }
        //// NPC
        var firstNpc = first.getNPCPositions();
        var secondNpc = second.getNPCPositions();
        if(firstNpc.length != secondNpc.length)
            return false;
        for(int i = 0; i < firstNpc.length; i++){
            if(firstNpc[i].size() != secondNpc[i].size()){
                return false;
            }
            for(int j = 0; j < firstNpc[i].size(); j++){
                boolean foundEquivalent = false;
                for(int k = 0; k < firstNpc[i].size(); k++){
                    if(firstNpc[i].get(j).equals(secondNpc[i].get(k))){
                        foundEquivalent = true;
                        break;
                    }
                }
                if(!foundEquivalent){
                    return false;
                }
            }
        }
        //// Movable
        var firstMovable = first.getNPCPositions();
        var secondMovable = second.getNPCPositions();
        if(firstMovable.length != secondMovable.length)
            return false;
        for(int i = 0; i < firstMovable.length; i++){
            if(firstMovable[i].size() != secondMovable[i].size()){
                return false;
            }
            for(int j = 0; j < firstMovable[i].size(); j++){
                boolean foundEquivalent = false;
                for(int k = 0; k < firstMovable[i].size(); k++){
                    if(firstMovable[i].get(j).equals(secondMovable[i].get(k))){
                        foundEquivalent = true;
                        break;
                    }
                }
                if(!foundEquivalent){
                    return false;
                }
            }
        }
        return true;
    }
}
