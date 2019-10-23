package tracks.singlePlayer.past.culim.ai.bot;

import java.util.ArrayList;
import java.util.Random;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import core.game.Observation;
import core.game.StateObservation;
import tracks.singlePlayer.past.culim.ai.AIBot;

/**
 * Just a wrapper around the default random move bot provided 
 * in the starter package.
 * 
 * @see controllers.sampleRandom
 * @author culim
 *
 */
public class RandBot extends AIBot
{
	private Random randomGenerator;
	
	public RandBot(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
	{
		super(stateObs, elapsedTimer);
		randomGenerator = new Random();
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
			
			
			ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions();
	        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
	        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
	        ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions();
	        ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions();
	        
	        Vector2d avatarPosition = stateObs.getAvatarPosition();
	        System.out.println(String.format("[culim.Agent], avatarPosition=%s", avatarPosition));

	        long remaining = elapsedTimer.remainingTimeMillis();
	        
	        Types.ACTIONS action = null;
	        StateObservation stCopy = stateObs.copy();

	        int whenToFinish = 10;
	        while(remaining > whenToFinish)
	        {
	        	
	            ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
	            int index = randomGenerator.nextInt(actions.size());
	            action = actions.get(index);

	            stCopy.advance(action);
	            if(stCopy.isGameOver())
	            {
	                stCopy = stateObs.copy();
	            }

	            remaining = elapsedTimer.remainingTimeMillis();
	        }

	        return action;
	}

	@Override
	public void onTearDown()
	{
		// TODO Auto-generated method stub
		
	}

}
