package tracks.singlePlayer.past.Return42.algorithms.deterministic.randomSearch.planning.update;

import tracks.singlePlayer.past.Return42.algorithms.deterministic.randomSearch.planning.Plan;
import core.game.StateObservation;

public interface PlanUpdatePolicy {
	public boolean doesPlanMatchToGameState( Plan plan, StateObservation state );

	public void startCleanup();
}
