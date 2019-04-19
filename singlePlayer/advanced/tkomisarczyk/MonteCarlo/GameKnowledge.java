package tracks.singlePlayer.advanced.tkomisarczyk.MonteCarlo;

import ontology.Types;

import java.util.HashMap;
import java.util.Map;

enum GameType{
    Planar2D,
    Other
}

enum SpriteType{
    Wall,
    Floor,
    Enemy,
    Point,
    Other
}

public class GameKnowledge{
    GameType type;
    Map<Integer, SpriteType> sprites = new HashMap<>();
    private static GameKnowledge instance;
    
    private GameKnowledge(){}
    
    public static GameKnowledge getInstance() {
        if(instance == null)
            instance = new GameKnowledge();
        return instance;
    }
    
    public static GameKnowledge getNew(){
        instance = new GameKnowledge();
        return instance;
    }
}
