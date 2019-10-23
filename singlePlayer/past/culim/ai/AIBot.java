package tracks.singlePlayer.past.culim.ai;

import java.util.ArrayList;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;

public abstract class AIBot
{
	public AIBot(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
	{
	}

	/**
	 * Main method of the AI-bot. 
	 * Implement main logic of AI calculations here and choose an action to return
	 * given a given state of the game.
	 * 
	 * @param stateObs the state observation of the game.
	 * @param elapsedTimer elapsed timer object.
	 * @return action to take by the agent.
	 */
	public abstract ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer);

	public abstract void onTearDown();
	

}
