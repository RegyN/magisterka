/**
 * 
 */
package tracks.singlePlayer.past.culim;

import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import tracks.singlePlayer.past.culim.ai.AIBot;
import tracks.singlePlayer.past.culim.ai.AIFactory;

/**
 * @author culim
 *
 */
public class Agent extends AbstractPlayer {
	

	public AIBot bot;
	
	public Agent(StateObservation stateObservation, ElapsedCpuTimer elapsedTimer) {
		
		// Uncomment to choose a type of bot to use.
		
		// 1. Random move bot.
		// bot = AIFactory.createRandBot(stateObservation, timer);
		
		// 2. Q-Learning bot
		bot = AIFactory.createQLearningBot(stateObservation, elapsedTimer);
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
	{
		return bot.act(stateObs, elapsedTimer);
	}

	//protected void onTeardown()
	//{
		// TODO Auto-generated method stub
		// super.onTeardown();
	//}


}
