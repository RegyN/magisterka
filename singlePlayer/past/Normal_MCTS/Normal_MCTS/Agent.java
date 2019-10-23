package tracks.singlePlayer.past.Normal_MCTS.Normal_MCTS;

import tracks.singlePlayer.past.Normal_MCTS.GameTable.GameTable;
import tracks.singlePlayer.past.Normal_MCTS.Mapping.Mapping;
import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: MMbot (version 0.1)
 * Date: 14/07/10
 * Time: 12:24
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    public static int NUM_ACTIONS;
    public static int ROLLOUT_DEPTH = 15; //14
    public static double K = Math.sqrt(2);
    public static Types.ACTIONS[] actions;
    
    public static int cnt =0;
    public static boolean first = true;
    
    public  static boolean Attacking=false;
    public static boolean existR = false;
    public static boolean existL = false;
    public static boolean existU = false;
    public static boolean existD = false;
    
    public static int Right = -1;
    public static int Left = -1;
    public static int Up = -1;
    public static int Down = -1;
    public static int Attack = -1;
    
    public static Vector2d v = null;
    
    private GameTable gt = new GameTable();

    /**
     * Random generator for the agent.
     */
    private SingleMCTSPlayer mctsPlayer;
    private MacroCheck macro = new MacroCheck();

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
    	
    	if( cnt > 4)
        	cnt =0;
        
        if( cnt ==0) {
            macro.resetAll();
            Attacking=false; 
            Attacking=false;
            existR = false;
            existL = false;
            existU = false;
            existD = false;
            
            Right = -1;
            Left = -1;
            Up = -1;
            Down = -1;
            Attack = -1;
        }
        else if(cnt>0){
        	macro.updateState();
        }
        
        cnt++;
        first = true;

        //Get the actions in a static array.
        ArrayList<Types.ACTIONS> act = so.getAvailableActions();
        actions = new Types.ACTIONS[act.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = act.get(i);
            
            if( Types.ACTIONS.ACTION_LEFT == act.get(i)){
            	existL = true;
            	Left = i;
            }
            
            if( Types.ACTIONS.ACTION_RIGHT == act.get(i)){
            	existR = true;
            	Right = i;
            }
            
            if( Types.ACTIONS.ACTION_UP == act.get(i)){
            	existU = true;
            	Up = i;
            }
            
            if( Types.ACTIONS.ACTION_DOWN == act.get(i)){
            	existD = true;
            	Down = i;
            }
            
            if( Types.ACTIONS.ACTION_USE == act.get(i)){
            	Attacking = true;
            	Attack = i;
            }
            
            
        }
        
        NUM_ACTIONS = actions.length;
        
        //Create the player.
        
        
        
//        System.out.println("cnt---->>>> "+cnt);
            
        mctsPlayer = new SingleMCTSPlayer(new Random());
        
        
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {


        //Set the state observation object as the new root of the tree.
        mctsPlayer.init(stateObs);

        //Determine the action using MCTS...

        int	action = mctsPlayer.run(elapsedTimer);
        macro.calculScore(stateObs, action);
    	
        //... and return it.
        return actions[action];
    }

}
