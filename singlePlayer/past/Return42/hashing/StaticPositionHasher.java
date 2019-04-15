package tracks.singlePlayer.past.Return42.hashing;

import core.game.StateObservation;

public class StaticPositionHasher implements IGameStateHasher {
	
	public int hash( StateObservation state ) {
        return  ObservationHasher.hash( state.getImmovablePositions() );
	}
}
