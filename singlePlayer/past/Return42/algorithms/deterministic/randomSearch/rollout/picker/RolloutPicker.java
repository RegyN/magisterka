package tracks.singlePlayer.past.Return42.algorithms.deterministic.randomSearch.rollout.picker;

import tracks.singlePlayer.past.Return42.algorithms.deterministic.randomSearch.rollout.strategy.RollOutStrategy;

public interface RolloutPicker {

	public void iterationFinished( boolean didFindPlan );
	public RollOutStrategy getCurrentRolloutStrategy();
	
}
