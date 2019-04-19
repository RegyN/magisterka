package tracks.singlePlayer.advanced.tkomisarczyk;

import core.game.StateObservation;

public class Position2D {
    int x;
    int y;
    
    public static Position2D getAvatarPosition(StateObservation obs){
        var res = new Position2D();
        res.x = (int)obs.getAvatarPosition().x / obs.getBlockSize();
        res.y = (int)obs.getAvatarPosition().y / obs.getBlockSize();
        return res;
    }
}
