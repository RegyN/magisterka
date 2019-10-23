package tracks.singlePlayer.past.culim.ai.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeSet;

import ontology.Types.WINNER;
import tools.Vector2d;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;
import tracks.singlePlayer.past.culim.ai.AIUtils;
import tracks.singlePlayer.past.culim.ai.bot.QLearningBot;

/**
 * The state for a q-learning system for the GVGAI competition.
 * @author culim
 *
 */
public class QLearningState implements Serializable
{
	public Vector2d avatarPosition;
	public Vector2d avatarOrientation;
	public double meanNpcDistance;
	public double meanResourceDistance;
	public double avatarSpeed;
	public boolean isGameOver;
	public int worldWidth;
	public int worldHeight;
	public int blockSize;
	public int gridWidth;
	public int gridHeight;
	public TreeSet<Event> eventsHistory;
	public double meanMovableDistances;
	public double meanImmovableDistances;
	public double meanPortalDistances;
	public double winScore;
	public double gameScore;
	public double meanClosestNPCDistance;
	public double genericReward;
	public int meanClosestNPCManhattan;
	public Vector2d nearestNPCGridPosition;
	public Object nearestMovableGridPosition;
	public Vector2d avatarGridPosition;
	public int nRemainingNPCs;
	public ArrayList<Observation>[][] observationGrid;
	public WINNER winner;
	public Event firstEvent;
	public Event lastEvent;
	public int nearestNPCId;
	public int nearestMovableId;
	
	public QLearningState(StateObservation stateObs)
	{
		this.isGameOver = stateObs.isGameOver();
		this.winner = stateObs.getGameWinner();
		this.avatarPosition = stateObs.getAvatarPosition();
		this.avatarOrientation = stateObs.getAvatarOrientation();
		this.avatarSpeed = stateObs.getAvatarSpeed();
		this.avatarGridPosition = AIUtils.getGridPosition(stateObs, stateObs.getAvatarPosition());
		this.nearestNPCGridPosition = AIUtils.getNearestNPCGridPosition(stateObs);
		this.nearestNPCId = AIUtils.getNearestNPCId(stateObs);
		this.nearestMovableGridPosition = AIUtils.getNearestMovableGridPosition(stateObs);
		this.nearestMovableId = AIUtils.getNearestMovableId(stateObs);
		this.meanNpcDistance = AIUtils.getMeanNpcSquareDistance(stateObs);
		this.meanResourceDistance = AIUtils.getMeanResourceSquareDistance(stateObs);
		this.meanMovableDistances = AIUtils.getMeanMovableSquareDistance(stateObs);
		this.meanImmovableDistances = AIUtils.getMeanImmovableSquareDistance(stateObs);
		this.meanPortalDistances = AIUtils.getMeanPortalSquareDistance(stateObs);
		this.meanClosestNPCDistance = AIUtils.getMeanClosestNPCDistance(stateObs);
		this.meanClosestNPCManhattan = AIUtils.getNearestNPCManhattanDistance(stateObs);
		this.blockSize = stateObs.getBlockSize();
		this.worldWidth = stateObs.getWorldDimension().width;
		this.worldHeight = stateObs.getWorldDimension().height;
		this.gridWidth = this.worldWidth/this.blockSize;
		this.gridHeight = this.worldHeight/this.blockSize;
		this.winScore = QLearningReward.getGameOverScore(stateObs);
		this.gameScore = QLearningReward.getGameScore(stateObs);
		this.genericReward = QLearningReward.getGenericReward(stateObs);
		this.nRemainingNPCs = AIUtils.getRemainingNPCs(stateObs);
		
		this.firstEvent = stateObs.getEventsHistory().size() > 0 ? stateObs.getEventsHistory().first() : null;
		this.lastEvent = stateObs.getEventsHistory().size() > 0 ? stateObs.getEventsHistory().last() : null;
		
		
		
	}
	
	public boolean isPlayerWon()
	{
		return this.winner == WINNER.PLAYER_WINS;
	}
	
	public boolean isGoal()
	{
		return isGameOver;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		QLearningState state;
		if (obj instanceof QLearningState)
		{
			state = (QLearningState) obj;
			return 	this.avatarPosition.equals(state.avatarPosition) &&
					this.meanNpcDistance == state.meanNpcDistance && 
//					this.meanMovableDistances == state.meanMovableDistances &&
//					this.meanImmovableDistances == state.meanImmovableDistances &&
					this.meanPortalDistances == state.meanPortalDistances && 
//					this.genericReward == state.genericReward &&
//					this.winScore == state.winScore &&
//					this.gameScore == state.gameScore &&
//					this.meanClosestNPCDistance == state.meanClosestNPCDistance &&
//					this.meanClosestNPCManhattan == state.meanClosestNPCManhattan && 
					this.nearestNPCGridPosition.equals(state.nearestNPCGridPosition) && 
					this.nearestNPCId == state.nearestNPCId &&
//					this.nearestMovableGridPosition.equals(state.nearestMovableGridPosition) &&
//					this.nearestMovableId == state.nearestMovableId &&
//					this.avatarOrientation.equals(state.avatarOrientation) &&
//					this.avatarSpeed == state.avatarSpeed &&
					this.nRemainingNPCs == state.nRemainingNPCs; 
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
//		return (int) (this.avatarGridPosition.y*this.gridHeight+this.avatarGridPosition.x);
		return 0;
	}
	
	@Override
	public String toString()
	{
		return String.format("QState A[%s]\tNPC[%s]\tnRemainingNPCs=%s)", this.avatarGridPosition, this.nearestNPCGridPosition, this.nRemainingNPCs );
	}

}
