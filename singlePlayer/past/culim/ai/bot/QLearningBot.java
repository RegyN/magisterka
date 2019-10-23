package tracks.singlePlayer.past.culim.ai.bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;
import tracks.singlePlayer.past.culim.ai.AIBot;
import tracks.singlePlayer.past.culim.ai.AIUtils;
import tracks.singlePlayer.past.culim.ai.components.LegacyQLearning;
import tracks.singlePlayer.past.culim.ai.components.QLearning;
import tracks.singlePlayer.past.culim.ai.components.QLearningAction;
import tracks.singlePlayer.past.culim.ai.components.QLearningState;

public class QLearningBot extends AIBot
{
	LegacyQLearning legacyLearning;
	QLearning qLearning;
	
	public HashSet<Integer> friendlyTypes;
	public HashSet<Integer> unfriendlyTypes;
	
	public static QLearningBot instance;

	public QLearningBot(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
	{
		super(stateObs, elapsedTimer);
		
		instance = this;
		
		qLearning = new QLearning();
		friendlyTypes = new HashSet<Integer>();
		unfriendlyTypes = new HashSet<Integer>();
		
//		qLearning = AIUtils.read("src/culim/data/qlearning.dat")
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
	{
		double tick = stateObs.getGameTick();
		
//		if (tick == 500)
//		{
//			qLearning = new QLearning();
//		}
		
		qLearning.alpha = Math.max(0.1,1-tick/2000);
			
		// Aim:
		// ----
		// To update the agent's Q-table, for which given a state `s', we are able to
		// select the best action 'a' based on its value in the Q-table.
		// e.g., 	Given state s1:
		//			If Q[s1,a1]=10, Q[s1,a2]=20, Q[s1,a3]=30,
		//			Then select action `a3' in order to maximize chance to reaching goal.
		
		// Actions:
		// --------
		// See ACTIONS
		// 1) UP
		// 2) DOWN
		// 3) LEF 
		// 4) RIGHT
		// 5) USE
		// 6) ESCAPE
		// 7) NIL
		
		QLearningState state = createState(stateObs);
		ArrayList<ACTIONS> candidateActions = stateObs.getAvailableActions();
		Collections.shuffle(candidateActions, new Random(System.nanoTime()));
		
		int i=0;
		while (elapsedTimer.remainingTimeMillis() >= 15)
		{
			ACTIONS action = candidateActions.size() > i ? candidateActions.get(i) : null;
			qLearning.run(stateObs, elapsedTimer, 10, action);
//			System.out.println("remaining="+elapsedTimer.remainingTimeMillis());
			i++;
		}
		AIUtils.log("iterations="+i);
		AIUtils.log(qLearning.printActionMap(state));
		QLearningAction action = qLearning.getBestAction(state, stateObs);
		AIUtils.log((String.format("[BestAction], qState=%s\naction=%s", state, action)));
		
		
		
		
		return getAction(action);
	}
	
	public static ACTIONS getAction(QLearningAction action)
	{
		return action.value();
	}
	
	public static QLearningState createState(StateObservation stateObs)
	{
		return new QLearningState(stateObs);
	}
	
	public void onTearDown()
	{
		AIUtils.write(qLearning, "src/culim/data/qlearning.dat");
	}

}
