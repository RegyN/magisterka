package tracks.singlePlayer.past.Return42.hashing;

import core.game.StateObservation;

public class MovableHasher implements IGameStateHasher {
    public int hash( StateObservation state ) {
        return  ObservationHasher.hash( state.getMovablePositions() );
    }
}