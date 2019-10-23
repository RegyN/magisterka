package tracks.singlePlayer.past.culim.ai.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import core.game.StateObservation;
import tracks.singlePlayer.past.culim.ai.AIUtils;
import tracks.singlePlayer.past.culim.ai.bot.QLearningBot;
import tracks.singlePlayer.past.culim.ai.components.LegacyQLearning.QAction;

public class QLearning implements Serializable
{
	public HashMap<QLearningState, HashMap<QLearningAction, Double>> qTable;
	public HashMap<QLearningAction, Integer> actionChoiceFrequency;
	public double gamma;	// discount
	public double alpha;	// step size
	
	public QLearning()
	{
		/**
		 * Algorithm
		 * ---------
		 * 1. Set the gamma parameter
		 * 2. Set environment rewards in matrix R
		 * 3. Initialize qTable to all zeroes
		 */
		
		qTable = new HashMap<QLearningState, HashMap<QLearningAction, Double>>();
		gamma = 1;		// 0: immediate rewards, 1: later rewards
		alpha = 0.7;	// 0: easy to unlearn 1: harder to unlearn
		
		actionChoiceFrequency = new HashMap<QLearningAction, Integer>();
	}
	
	/**
	 * Called directly by the method {@link QLearningBot #act(StateObservation, ElapsedCpuTimer)}.
	 * This is when the {@link QLearningBot} is given a period of time to decide on its next action.
	 * <p>
	 * Here we try to update the {@link QLearning #qTable} as much as possible. We then
	 * try to return the best possible move by looking at the qTable.
	 * <p>
	 * e.g.,
	 * The chosen best action would be done by looking at {@link QLearning #qTable}[state] and
	 * returning the {@link QLearningAction} that gives the highest reward score.
	 * 
	 * @param stateObs the {@link StateObservation} object. 
	 * @param elapsedTimer the {@link ElapsedCpuTimer} object.
	 * @param depth the depth of the search
	 */
	public void run(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int depth, ACTIONS startingAction)
	{
		/**
		 * 1. Select random initial state S.
		 * 2. Do while goal hasn't been reached
		 * 	a) Select on among all possible actions for current state S, an action A.
		 *  b) Simulate A on S to get us to S'
		 *  c) Get maximum Q-value for (S', A'), for all possible actions A' (qMax) 
		 *  d) Compute Q(S,A) = R(S,A) + gamma + Qmax
		 *  e) Set S = S'
		 */
		
		stateObs.getObservationGrid();
		
		QLearningState currentState;
		StateObservation currentStateObs = stateObs.copy();
		
		int d=0;
		for (int i=0; i < depth && isRunPossible(stateObs, elapsedTimer); i++)
		{
			currentState = new QLearningState(currentStateObs);
			
			// 1. Select random action for currentState.
			ArrayList<ACTIONS> candidateActions = currentStateObs.getAvailableActions();
			ACTIONS action = AIUtils.randomElement(candidateActions);
			if (i==0) {
				action = startingAction != null ? startingAction : action;
			}
			
			if (action == null)
			{
				action = ACTIONS.ACTION_NIL;
			}
			
//			AIUtils.log(String.format("randomAction=%s", action));
			
			// 2. Simulate action on currentState to get nextState.
			currentStateObs.advance(action);
			QLearningState nextState = new QLearningState(currentStateObs);
			
			// 3. Get maximum Q-value for nextState 
			double qMax = getQMaxForState(nextState);
			
			// 4. Update q-table entry for <currentState, action>
			double currentValue = getQValueForStateAction(currentState, QLearningAction.fromAction(action));
			double reward = QLearningReward.getReward(nextState);
			
//			double updatedValue = reward + gamma * qMax;
			double updatedValue = currentValue +  alpha * (reward + gamma * qMax - currentValue);
			
			putQValueForStateAction(currentState, QLearningAction.fromAction(action), updatedValue);
			
			AIUtils.log(String.format("[Reward], qState=%s\taction=%s\treward=%s", currentState, action, reward));
			// System.out.println(String.format("QTable[%s]=%s", currentState, updatedValue));
			
			d++;
			
			// 5. Set S = S'
			// Note: stateObs has already been advanced. 
			// currentState = nextState;
		}
		
		AIUtils.log("d="+d);
		
	}
	public void run(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int depth)
	{
		run(stateObs, elapsedTimer, depth, null);
	}
	
	private boolean isRunPossible(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
	{
		return !stateObs.isGameOver() && elapsedTimer.remainingTimeMillis() >= 15;
	}
	
	public HashMap<QLearningAction, Double> getStateActionValueMap(QLearningState state)
	{
		HashMap<QLearningAction, Double> actionValueMapping = qTable.get(state);
		if (actionValueMapping == null)
		{
			// This state doesn't yet exist in the q-table.
			// Create and initialize a new HashMap<Action,Value> for this state.
			actionValueMapping = new HashMap<QLearningAction, Double>();
			ACTIONS[] allActions = ACTIONS.values();
			for (ACTIONS action : allActions)
			{
				actionValueMapping.put(new QLearningAction(action), 0d);
			}
			qTable.put(state, actionValueMapping);
		}
		
		return actionValueMapping;
	}
	
	public double getQValueForStateAction(QLearningState state, QLearningAction action)
	{
		HashMap<QLearningAction, Double> stateQValues = getStateActionValueMap(state);
		return stateQValues.get(action);
	}
	
	public void putQValueForStateAction(QLearningState state, QLearningAction action, double value)
	{
		getStateActionValueMap(state).put(action, value);
	}
	
	public double getQMaxForState(QLearningState state)
	{
		HashMap<QLearningAction, Double> actionValueMap = getStateActionValueMap(state);
		
		ArrayList<QLearningAction> maxActions = new ArrayList<QLearningAction>();
		double  maxValue = -99999999;
		
		Set<QLearningAction> actionSet = actionValueMap.keySet();
		for (QLearningAction action : actionSet)
		{
			// Get max value
			double currentValue = actionValueMap.get(action);
			if (currentValue >= maxValue)
			{
				maxValue = currentValue;
			}
		}
		
		return maxValue;
	}
	
	public double getQMaxForState(QLearningState state, ArrayList<QLearningAction> actionSet)
	{
		HashMap<QLearningAction, Double> actionValueMap = getStateActionValueMap(state);
		
		ArrayList<QLearningAction> maxActions = new ArrayList<QLearningAction>();
		double  maxValue = -99999999;
		
		for (QLearningAction action : actionSet)
		{
			// Get max value
			double currentValue = actionValueMap.get(action);
			if (currentValue >= maxValue)
			{
				maxValue = currentValue;
			}
		}
		
		return maxValue;
	}

//	/**
//	 * Returns the {@link QLearningAction} with the highest value in the {@link QLearning #qTable}.
//	 * 
//	 * @param state the state as the index to the q-table
//	 * @return the action with the higest value for the given state
//	 */
//	public QLearningAction getBestAction(QLearningState state)
//	{
//		ArrayList<QLearningAction> bestQActions = getBestActions(state);
//	
////		if (bestQActions.contains(QLearningAction.NIL))
////		{
////			bestQActions.remove(QLearningAction.NIL);
////		}
//		
//		if (bestQActions.contains(QLearningAction.ESCAPE))
//		{
//			bestQActions.remove(QLearningAction.ESCAPE);
//		}
//		
//		QLearningAction bestAction = QLearningAction.NIL;
//		
//		if (bestQActions.size() > 0)
//		{
//			bestAction =  AIUtils.randomElement(bestQActions);
//		}
//		
//		return bestAction;
//	}
	
	/**
	 * Returns the {@link QLearningAction} with the highest value in the {@link QLearning #qTable}.
	 * 
	 * @param state the state as the index to the q-table
	 * @return the action with the higest value for the given state
	 */
	public QLearningAction getBestAction(QLearningState state, StateObservation stateObs)
	{	ArrayList<QLearningAction> bestQActions = getBestActions(state);
		ArrayList<ACTIONS> availableActions = stateObs.getAvailableActions();
		ArrayList<QLearningAction> availableActions2 = new ArrayList<QLearningAction>();
		
		for (ACTIONS action : availableActions) {
			availableActions2.add(QLearningAction.fromAction(action));
		}
	
//		if (bestQActions.contains(QLearningAction.NIL))
//		{
//			bestQActions.remove(QLearningAction.NIL);
//		}
		
//		if (bestQActions.contains(QLearningAction.ESCAPE))
//		{
//			bestQActions.remove(QLearningAction.ESCAPE);
//		}
		
		ArrayList<QLearningAction> candidates = new ArrayList<QLearningAction>();
		
		for (ACTIONS action : availableActions)
		{
			if (bestQActions.contains(QLearningAction.fromAction(action)))
			{
				candidates.add(QLearningAction.fromAction(action));
			}
		}
		
		QLearningAction bestAction = QLearningAction.NIL;
		
		if (candidates.size() == 0)
		{
			double max = -9999999;
			for (ACTIONS action : availableActions)
			{
				double actionValue = getQValueForStateAction(state, QLearningAction.fromAction(action));
				if (actionValue >= max)
				{
					max = actionValue;
				}
			}
			
			for (ACTIONS action : availableActions)
			{
				double actionValue = getQValueForStateAction(state, QLearningAction.fromAction(action));
				if (actionValue == max)
				{
					candidates.add(QLearningAction.fromAction(action));
				}
			}
		}
		
			
		
		
		// Pick random element from candidates.
//		 bestAction =  AIUtils.randomElement(candidates);
		@SuppressWarnings("unchecked")
		ArrayList<QLearningAction> candidateCopy = (ArrayList<QLearningAction>) candidates.clone();
		
		bestAction = getMostFrequentAction(candidateCopy);
		
		while (bestAction != null && AIUtils.isUnchangingMove(stateObs, bestAction.action)) {
			candidateCopy.remove(bestAction);
			bestAction = getMostFrequentAction(candidateCopy);
		}
		
		if (bestAction == null) {
			bestAction = AIUtils.randomElement(getBestActions(state, availableActions2));
			while (bestAction != null && AIUtils.isUnchangingMove(stateObs, bestAction.action)) {
				availableActions2.remove(bestAction);
				bestAction = AIUtils.randomElement(getBestActions(state, availableActions2));
			}
		}
		
		if (bestAction == null) {
			 bestAction =  AIUtils.randomElement(candidates);
		}
		
		
		return bestAction;
	}
	
	
	
	public QLearningAction getMostFrequentAction(ArrayList<QLearningAction> actions) {
		
		int maxFrequency = -1;
		ArrayList<QLearningAction> candidates = new ArrayList<QLearningAction>();
		for (QLearningAction action : actions) {
			if (!actionChoiceFrequency.containsKey(action)) {
				actionChoiceFrequency.put(action, 0);
			}
			int frequency = actionChoiceFrequency.get(action);
			if (frequency >= maxFrequency) {
				maxFrequency = frequency;
			}
		}
		for (QLearningAction action : actions) {
			int frequency = actionChoiceFrequency.get(action);
			if (frequency == maxFrequency) {
				candidates.add(action);
			}
		}
		
		QLearningAction chosen = AIUtils.randomElement(candidates);
		actionChoiceFrequency.put(chosen, actionChoiceFrequency.get(chosen));
		
		return chosen;
		
	}
	
	
	
	public String printActionMap(QLearningState state)
	{
		String str ="";
		
		HashMap<QLearningAction, Double> actionMap = getStateActionValueMap(state);
		
		for (QLearningAction action : actionMap.keySet())
		{
			double value = actionMap.get(action);
			str += String.format("%s\t%s\t%s\n", state, action, value);
		}
		
		return str;
			
	}
	
	public ArrayList<QLearningAction> getBestActions(QLearningState state)
	{
		HashMap<QLearningAction, Double> stateQValues = getStateActionValueMap(state);
		double qMax = getQMaxForState(state);
		
		ArrayList<QLearningAction> maxActions = new ArrayList<QLearningAction>();
		
		for (QLearningAction action : stateQValues.keySet())
		{
			double currentValue = stateQValues.get(action);
			if (currentValue == qMax)
			{
				maxActions.add(action);
			}
		}
		
		return maxActions;
	}
	
	public ArrayList<QLearningAction> getBestActions(QLearningState state, ArrayList<QLearningAction> actions)
	{
		HashMap<QLearningAction, Double> stateQValues = getStateActionValueMap(state);
		double qMax = getQMaxForState(state, actions);
		
		ArrayList<QLearningAction> maxActions = new ArrayList<QLearningAction>();
		
		for (QLearningAction action : actions)
		{
			double currentValue = stateQValues.get(action);
			if (currentValue == qMax)
			{
				maxActions.add(action);
			}
		}
		
		return maxActions;
	}
	
	
}
