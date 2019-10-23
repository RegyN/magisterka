package tracks.singlePlayer.past.culim.ai.components;

import core.game.StateObservation;
import tracks.singlePlayer.past.culim.ai.AIUtils;
import ontology.Types.WINNER;
import tools.Vector2d;

public class QLearningReward
{
	public static double getReward(QLearningState state)
	{
		float width =  state.worldWidth;
		float height =  state.worldHeight;
		
		float gridWidth = state.gridWidth;
		float gridHeight = state.gridHeight;
		
		double maxGrid = gridHeight+gridWidth;
		double max = (width*width)+(height*height);
		
		// Get the reward for a given q-learning state.
		double reward = 
						+ 1.0 * state.genericReward
						+ 0.5 * (-state.nRemainingNPCs)
//						+ 1.0 * (1-state.meanPortalDistances/max)
//						+ 1.0 * state.winScore
//						+ 1.0 * state.gameScore
//						+ 1.0 * (1-state.meanNpcDistance/max)
//						+ 0.5 * (state.meanMovableDistances/max)
//						+ 1.0 * (state.meanImmovableDistances/max)
						+ 0.75 * (1-state.meanClosestNPCManhattan/maxGrid)
//						+ 0.5 * (state.meanClosestNPCDistance/max)
						;
		
		return reward;
		
	}
	
	public static double getGenericReward(StateObservation stateObs)
	{
		if (stateObs.isGameOver())
		{
			WINNER winner = stateObs.getGameWinner();
			if (winner == WINNER.PLAYER_WINS)
			{
				return 10000;
			}
			else if (winner == WINNER.PLAYER_LOSES)
			{
				return -10000;
			}
			else
			{
				return 0;
			}
		}
		else
		{
			return stateObs.getGameScore();
		}
	}
	
	public static double getGameScore(StateObservation stateObs)
	{
		return stateObs.getGameScore()/100;
	}
	
	public static double getGameOverScore(StateObservation stateObs)
	{
		double gameWinnerScore = 0;
		WINNER gameWinner = stateObs.getGameWinner();
		
		if (gameWinner == WINNER.PLAYER_WINS)
		{
			gameWinnerScore = 5;
		}
		else if (gameWinner == WINNER.NO_WINNER)
		{
			gameWinnerScore = 0;
		}
		else if (gameWinner == WINNER.PLAYER_LOSES)
		{
			gameWinnerScore = -10; 
		}
		else if (gameWinner == WINNER.PLAYER_DISQ)
		{
			gameWinnerScore = -10;
		}
		
		return gameWinnerScore;
	}

}
