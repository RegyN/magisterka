package tracks.singlePlayer.past.Shmokin;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;


import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {
    
    public GameInformation gameInfo;
    
	ArrayList<Node> openLocations = new ArrayList<Node>();
	ArrayList<Node> closedLocations = new ArrayList<Node>();
    
	long time = 0;

	public Path shortestPath;
    
    
	public int playerValue;
	public int currentGoalValue;
    
	ArrayList<Observation> grid[][];
    
	AStar astar;
    
	//Monte Carlo
	public static int NUM_ACTIONS;
	public static int ROLLOUT_DEPTH = 5;
	public static double K = Math.sqrt(2);
	public static Types.ACTIONS[] actions;

	/**
 	* Random generator for the agent.
 	*/
	private SingleMCTSPlayer2 mctsPlayer;

    
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

   	 gameInfo = new GameInformation();
   	 gameInfo.mode = GameInformation.Mode.AStar;
    	//Get the actions in a static array.
    	ArrayList<Types.ACTIONS> act = stateObs.getAvailableActions();
    	actions = new Types.ACTIONS[act.size()];
    	for(int i = 0; i < actions.length; ++i)
    	{
        	actions[i] = act.get(i);
    	}
    	NUM_ACTIONS = actions.length;

    	//Create the player.
    	mctsPlayer = new SingleMCTSPlayer2(new Random());
   	 
   	 
   	 playerValue = 0;
   	 currentGoalValue = 2;
   	 
   	 astar = new AStar(stateObs);
	}

	/**
 	*
 	* Very simple one step lookahead agent.
 	*
 	* @param stateObs Observation of the current state.
 	* @param elapsedTimer Timer when the action returned is due.
 	* @return An action for the current state
 	*/
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

   	 time = time + (gameInfo.getTimeWaited() + elapsedTimer.elapsedMillis());
   	 long test = elapsedTimer.elapsedMillis();
   	 long test2 = gameInfo.getTimeWaited();
   	 
   	 //if(time > gameInfo.getWaitTime())
   	 

		 
   	 if (gameInfo.mode == GameInformation.Mode.MonteCarlo) {
   		 // Set the state observation object as the new root of the tree.
   		 astar.sensorMap();
   		 astar.modeEvaluation(gameInfo);
   		 mctsPlayer.init(stateObs);

   		 // Determine the action using MCTS...
   		 int action = mctsPlayer.run(elapsedTimer);
   		 
   		 //add the amount of time spent in monte carlo mode
   		 gameInfo.setTimeWaited(gameInfo.getTimeWaited() + elapsedTimer.elapsedMillis());
   		 gameInfo.setOldScore(stateObs.getGameScore() + gameInfo.getOldScore());

   		 //SWitches the game back to Astar mode after 500 milliseconds of being in monte carlo mode
   		 //Adapatable to remain in this mode if score is increasing?
   		 if(gameInfo.getTimeWaited() > gameInfo.getWaitTime())
   		 {
   			 gameInfo.mode = GameInformation.Mode.AStar;
   			 gameInfo.setTimeWaited(0);
   			 gameInfo.reset = true;
   		 }
   		 
   		 // ... and return it.
   		 return actions[action];
   	 } else if (gameInfo.mode == GameInformation.Mode.AStar) {
   		 //add the amount of time spent in monte carlo mode



   		 Types.ACTIONS action = astar.explore(stateObs, elapsedTimer,
   				 gameInfo);
   		 
		 //gameInfo.setTimeWaited(gameInfo.getTimeWaited() + elapsedTimer.elapsedMillis());
		 
		 gameInfo.setTimeWaited(gameInfo.getTimeWaited() + 15);
		 gameInfo.setOldScore(stateObs.getGameScore() + gameInfo.getOldScore());
		 
   		 //Switches the game back to MCTS if the game has been in push mode too long
   		 //Adapatable to remain in this mode if score is increasing?
   		 if(gameInfo.getTimeWaited() > gameInfo.getWaitTimePush())
   		 {
   			 gameInfo.mode = GameInformation.Mode.MonteCarlo;
   			 gameInfo.setTimeWaited(0);
   			 //gameInfo.reset = true;
   		 }

   		 return action;
   	 } else {
   		 return null;
   	 }
   	 

	}

}

