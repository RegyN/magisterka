package tracks.singlePlayer.advanced.tkomisarczyk;

import core.game.StateObservation;
import ontology.Types;
import tracks.singlePlayer.tools.Heuristics.WinScoreHeuristic;

public class Utilities {
    public static double NormalizeScore(double score, double upperBound, double lowerBound){
        return (score - lowerBound) / (upperBound - lowerBound + 0.000001d);
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
}
